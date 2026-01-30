package com.example.demo.service;

import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final AppointmentRepository appointmentRepository;

    public BookingService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<LocalTime> getAvailableSlotsForTheWholeDay(User barber, LocalDate date) {

        // 1. Ustawienie pracy 8:00-18:00
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        int slotMinutes = 60;
        LocalTime time;

        // 2. Wygenerowanie wszysktich slotow
        List<LocalTime> allSlots = new ArrayList<>();
        for (time = workStart; // time = 8:00
             time.isBefore(workEnd); // dopoki nie będzie 18:00
             time = time.plusMinutes(slotMinutes)) // 1 slot = 60min na poczatek przynajmniej wiec tyle dodaje
        {
            allSlots.add(time); // czyli allSlots = {8:00, 9:00, 10:00..., 17:00}
        }

        // 3. Pobieram wszystkie zajete sloty
        LocalDateTime from = date.atStartOfDay(); // aktualny dzień np. 2026-01-29 00:00
        LocalDateTime to = date.plusDays(1).atStartOfDay(); // 2026-01-30 00:00 - czyli biore przedzial 24 godzin calego dnia
        List<LocalTime> takenSlots = appointmentRepository.findByBarberAndStatusAndStartTimeBetween
                        (barber, AppointmentStatus.BOOKED, from, to)
                .stream()
                .map(appointment -> appointment.getStartTime().toLocalTime())
                .toList();

        allSlots.removeAll(takenSlots);
        return allSlots;
    }

    public boolean isSlotFree(User barber, LocalDateTime startTime) {
        return !appointmentRepository.existsByBarberAndStatusAndStartTime(barber, AppointmentStatus.BOOKED, startTime);
    }

}

















