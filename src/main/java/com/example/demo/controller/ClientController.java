package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserRepository userRepository;
    private final BookingService bookingService;

    public ClientController(UserRepository userRepository, BookingService bookingService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String date, Model model) {

        LocalDate selectedDate = (date == null) ? LocalDate.now() : LocalDate.parse(date);

        List<User> allUsers = userRepository.findAll();
        User barber = new User();

        for (User allUser : allUsers) {
            if (allUser.getRole().name().equals("BARBER")) {
                barber = allUser;
            }
        }


        model.addAttribute("selectedDate", selectedDate);

        List<LocalTime> availableSlots = bookingService.getAvailableSlotsForTheWholeDay(barber, selectedDate);

        model.addAttribute("barberName", barber.getFullName());
        model.addAttribute("availableSlots", availableSlots);

        return "client/dashboard";
    }
 }
