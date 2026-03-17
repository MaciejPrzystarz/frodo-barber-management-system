package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import org.springframework.data.domain.Sort;
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
                       Authentication authentication) {
        LocalDate selectedDate = LocalDate.parse(date);
        LocalTime selectedTime = LocalTime.parse(time);
        LocalDateTime startTime = selectedDate.atTime(selectedTime);

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie znaleziono takiego klienta."));
        User barber = userRepository.findByRole(Role.BARBER);

        ServiceItem serviceItem = serviceRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Nie znaleziono takiej usługi"));

        bookingService.saveAppointment(barber, client, startTime, serviceItem);

        return "redirect:/client/dashboard?date=" + selectedDate;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String date, @RequestParam(required = false) Long serviceId,
                            Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego użytkownika"));
        List<Appointment> appointments = appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client);

        LocalDate selectedDate = (date == null) ? LocalDate.now() : LocalDate.parse(date); //04.05
        LocalDate nowLocalDatePlus45Days = LocalDate.now().plusDays(45); //17.03

        if (selectedDate.isAfter(nowLocalDatePlus45Days)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Możesz umówić wizytę maksymalnie do 45 dni od teraz.");
            return "redirect:/client/dashboard";
        }

        User barber = userRepository.findByRole(Role.BARBER);

        List<ServiceItem> services = serviceRepository.findAll();

        ServiceItem selectedService = (serviceId == null) ? services.getFirst()
                : serviceRepository.findById(serviceId).orElse(services.getFirst());

        List<LocalTime> availableSlots = bookingService.getAvailableSlotsForTheWholeDay
                (barber, selectedDate, selectedService.getDurationMinutes());

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("barberName", barber.getFullName());

        model.addAttribute("services", services);
        model.addAttribute("selectedServiceId", selectedService.getId());
        model.addAttribute("availableSlots", availableSlots);

        return "client/dashboard";
    }
}
