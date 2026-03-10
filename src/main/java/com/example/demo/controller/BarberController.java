package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/appointments/{id}/{status}")
    String changeStatus(@PathVariable Long id, @PathVariable String status,
                        Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Nie ma takiej wizyty"));

        if (!appointment.getBarber().getId().equals(barber.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Brak dostępu do tej wizyty.");
            return "redirect:/barber/dashboard";
        }

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Można zmieniać tylko wizyty ze statusem BOOKED.");
            return "redirect:/barber/dashboard";
        }

        AppointmentStatus appointmentStatus = bookingService.changeStatus(status);

        appointment.setStatus(appointmentStatus);
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("successMessage", "Status wizyty został zmieniony na " + status + ".");

        return "redirect:/barber/dashboard";
    }


    @GetMapping("/dashboard")
    public String home(Authentication authentication, Model model) {
        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));
        List<Appointment> appointments = appointmentRepository.findAppointmentByBarberOrderByStartTimeAsc(barber);

        model.addAttribute("appointments", appointments);

        return "barber/dashboard";
    }
}
