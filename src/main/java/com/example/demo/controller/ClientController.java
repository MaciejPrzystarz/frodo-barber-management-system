package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;

    public ClientController(UserRepository userRepository, BookingService bookingService,
                            AppointmentRepository appointmentRepository, ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
    }

    @PostMapping("/book")
    public String book(@RequestParam String date, @RequestParam String time, @RequestParam Long serviceId,
                       Authentication authentication, RedirectAttributes redirectAttributes) {
        LocalDate selectedDate = LocalDate.parse(date);
        LocalTime selectedTime = LocalTime.parse(time);
        LocalDateTime startTime = selectedDate.atTime(selectedTime);

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie znaleziono takiego klienta."));
        User barber = userRepository.findByRole(Role.BARBER);

        ServiceItem serviceItem = serviceRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Nie znaleziono takiej usługi"));

        if (selectedTime.isBefore(LocalTime.now()) && selectedDate.equals(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie ma możliwości umówić wizyty z przeszłości:P");
            return "redirect:/client/dashboard";
        }

        bookingService.saveAppointment(barber, client, startTime, serviceItem);

        return "redirect:/client/dashboard?date=" + selectedDate;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            @RequestParam(required = false) Long serviceId, Model model, Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego użytkownika"));
        List<Appointment> appointments = appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client);

        LocalDate selectedDate = (date == null) ? LocalDate.now() : date;

        if (date != null) {
            for (Appointment appointment : appointments) {
                LocalDate appointmentDate = appointment.getStartTime().toLocalDate();

                if (!selectedDate.isBefore(appointmentDate.minusDays(10))
                        && !selectedDate.isAfter(appointmentDate.plusDays((10)))) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Możesz mieć maksymalnie jedną wizytę w ciagu 10 dni");
                    return "redirect:/client/dashboard";
                }
            }

            LocalDate localDateNowPlus45Days = LocalDate.now().plusDays(45);
            if (selectedDate.isAfter(localDateNowPlus45Days)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Możesz umówić wizytę maksymalnie do 45 dni od dzisiaj");
                return "redirect:/client/dashboard";
            }
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie ma możliwości umówić wizyty z przeszłości");
            return "redirect:/client/dashboard";
        }

        User barber = userRepository.findByRole(Role.BARBER);
        List<ServiceItem> services = serviceRepository.findAll();

        ServiceItem selectedService = null;
        List<LocalTime> availableSlots = List.of();

        if (serviceId != null) {
            selectedService = serviceRepository.findById(serviceId).orElseThrow(
                    () -> new IllegalArgumentException("Nie ma takiej usługi"));

            availableSlots = bookingService.getAvailableSlotsForTheWholeDay
                    (barber, selectedDate, selectedService.getDurationMinutes());
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("barberName", barber.getFullName());

        model.addAttribute("services", services);
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("availableSlots", availableSlots);

        return "client/dashboard";
    }
}
