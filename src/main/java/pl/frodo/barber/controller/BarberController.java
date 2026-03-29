package pl.frodo.barber.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.UserRepository;
import pl.frodo.barber.service.BookingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/barber")
public class BarberController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public BarberController(AppointmentRepository appointmentRepository, UserRepository userRepository, BookingService bookingService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/pending-appointments")
    public String pendingAppointments(Model model) {
        List<Appointment> pendingAppointments = appointmentRepository.findAppointmentByStatus(AppointmentStatus.PENDING);

        model.addAttribute("pendingAppointments", pendingAppointments);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());

        return "barber/pending-appointments";
    }

    @PostMapping("/appointments/{id}")
    String changeStatus(@PathVariable Long id, @RequestParam String status,
                        Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Nie ma takiej wizyty"));

        if (!appointment.getBarber().getId().equals(barber.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Brak dostępu do tej wizyty.");
            return "redirect:/barber/dashboard";
        }
        if (appointment.getStatus() == AppointmentStatus.DONE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można zmienić statusu zakończonej wizyty.");
            return "redirect:/barber/dashboard";
        }

        try {
            AppointmentStatus appointmentStatus = bookingService.changeStatus(status);

            appointment.setStatus(appointmentStatus);
            appointmentRepository.save(appointment);

            redirectAttributes.addFlashAttribute("successMessage", "Status wizyty został zmieniony na " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie udało się zmienić statusu wizyty");
            return "redirect:/barber/dashboard";
        }

        return "redirect:/barber/pending-appointments";
    }

    @GetMapping("/dashboard")
    public String home(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date,
                       Authentication authentication, Model model) {
        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));

        LocalDate selectedDate = (date == null) ? LocalDate.now() : date;
        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findAppointmentByBarberAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
                barber, AppointmentStatus.BOOKED, start, end);

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("appointments", appointments);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());

        return "barber/dashboard";
    }
}
