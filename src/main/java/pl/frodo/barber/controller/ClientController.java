package pl.frodo.barber.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.frodo.barber.model.*;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.ServiceRepository;
import pl.frodo.barber.repository.UserRepository;
import pl.frodo.barber.service.BookingService;
import pl.frodo.barber.service.VacationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final VacationService vacationService;

    public ClientController(UserRepository userRepository, BookingService bookingService,
                            AppointmentRepository appointmentRepository, ServiceRepository serviceRepository, VacationService vacationService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.vacationService = vacationService;
    }

    @PostMapping("/book")
    public String book(@RequestParam LocalDate date, @RequestParam LocalTime time, @RequestParam Long serviceId,
                       Authentication authentication, RedirectAttributes redirectAttributes) {
        LocalDateTime startTime = date.atTime(time);

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie znaleziono takiego klienta."));
        User barber = userRepository.findByRole(Role.BARBER);

        ServiceItem serviceItem = serviceRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Nie znaleziono takiej usługi."));

        Optional<String> validationError = bookingService.validateClientBooking(date, time, client);

        if (validationError.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError.get());
            return "redirect:/client/dashboard";
        }

        bookingService.saveAppointment(barber, client, startTime, serviceItem);

        return "redirect:/client/dashboard?date=" + date;
    }

    @GetMapping("/my-profile")
    public String profile(Authentication authentication, Model model) {

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego użytkownika"));

        List<Appointment> allAppointments = appointmentRepository.findAppointmentByClientOrderByStartTimeDesc(client);
        List<Appointment> upcomingAppointments = bookingService.getUpcomingAppointments(allAppointments);

        List<Appointment> appointmentsHistory = appointmentRepository.findAppointmentByClientAndStatusOrderByStartTimeDesc(client,
                AppointmentStatus.DONE, AppointmentStatus.CANCELLED, AppointmentStatus.DIDNT_SHOW_UP);

        model.addAttribute("appointmentsHistory", appointmentsHistory);
        model.addAttribute("upcomingAppointments", upcomingAppointments);

        return "client/my-profile";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            @RequestParam(required = false) Long serviceId, Model model, Authentication authentication) {

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego użytkownika."));

        List<Appointment> allAppointments = appointmentRepository.findAppointmentByClientOrderByStartTimeDesc(client);

        List<Appointment> upcomingAppointments = bookingService.getUpcomingAppointments(allAppointments);

        LocalDate selectedDate = (date == null) ? LocalDate.now() : date;

        User barber = userRepository.findByRole(Role.BARBER);
        List<ServiceItem> services = serviceRepository.findAll();

        ServiceItem selectedService = null;
        List<LocalTime> availableSlots = List.of();

        if (serviceId != null) {
            selectedService = serviceRepository.findById(serviceId).orElseThrow(
                    () -> new IllegalArgumentException("Nie ma takiej usługi."));

            availableSlots = bookingService.getAvailableSlotsForTheWholeDay
                    (barber, selectedDate, selectedService.getDurationMinutes());
        }

        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("barberName", barber.getFullName());

        model.addAttribute("services", services);
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("availableSlots", availableSlots);

        model.addAttribute("barberVacations", vacationService.getCurrentAndFutureVacations());

        return "client/dashboard";
    }
}
