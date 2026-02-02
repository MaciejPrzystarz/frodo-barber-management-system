package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/barber")
public class BarberController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public BarberController(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String home(Authentication authentication, Model model) {

        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));
        List<Appointment> appointments = appointmentRepository.findAppointmentByBarber(barber);

        model.addAttribute("appointments", appointments);

        return "barber/dashboard";
    }
}
