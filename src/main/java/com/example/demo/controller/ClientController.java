package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final AppointmentRepository appointmentRepository;

    public ClientController(UserRepository userRepository, BookingService bookingService, AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.appointmentRepository = appointmentRepository;
    }

    @PostMapping("/book")
    public String book(@RequestParam String date, @RequestParam String time, Authentication authentication) {
        LocalDate selectedDate = LocalDate.parse(date);
        LocalTime selectedTime = LocalTime.parse(time);

        LocalDateTime startTime = selectedDate.atTime(selectedTime);

        // klient
        String email = authentication.getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie znaleziono takiego klienta."));

        // jedyny barber FRODO - aplikacja tylko dla brata wiec nie bedzie nigdy wiecej barberów
        User barber = userRepository.findByRole(Role.BARBER);

        // na wszelki wypadek jakby 2 razy praktycznie w tym samym czasie ktos kliklnal
        if (!bookingService.isSlotFree(barber, startTime)) {
            return "redirect:/client/dashboard?date=" + selectedDate + "&error=TAKEN";
        }

        // zapis wizyty
        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setStartTime(startTime);
        appointment.setStatus(AppointmentStatus.BOOKED);

        appointmentRepository.save(appointment);

        return "redirect:/client/dashboard?date=" + selectedDate;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String date, Model model) {

        LocalDate selectedDate = (date == null) ? LocalDate.now() : LocalDate.parse(date);

        // jedyny barber FRODO, zmieniam tutaj tez w takim razie
        User barber = userRepository.findByRole(Role.BARBER);

        model.addAttribute("selectedDate", selectedDate);

        List<LocalTime> availableSlots = bookingService.getAvailableSlotsForTheWholeDay(barber, selectedDate);

        model.addAttribute("barberName", barber.getFullName());
        model.addAttribute("availableSlots", availableSlots);

        return "client/dashboard";
    }
 }
