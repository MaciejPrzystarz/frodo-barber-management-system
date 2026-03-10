package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.ServiceItem;
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

    public AppointmentStatus changeStatus(String status) {
        if ((status).equalsIgnoreCase("DONE")) {
            return AppointmentStatus.DONE;
        } else if ((status).equalsIgnoreCase("CANCELLED")){
            return AppointmentStatus.CANCELLED;
        }

        throw new IllegalStateException("Nieprawidłowy status: " + status);
    }

    public void saveAppointment(User barber, User client, LocalDateTime startTime, ServiceItem service) {

        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.BOOKED);

        appointmentRepository.save(appointment);
    }

    public List<LocalTime> getAvailableSlotsForTheWholeDay(User barber, LocalDate date, int durationMinutes) {

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        int slotMinutes = 10;

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<LocalTime> allSlots = new ArrayList<>();
        List<Appointment> takenSlots = appointmentRepository.findByBarberAndStatusAndStartTimeBetween
                (barber, AppointmentStatus.BOOKED, from, to);

        for (LocalTime time = workStart; time.isBefore(workEnd); time = time.plusMinutes(slotMinutes)) {

            LocalDateTime start = date.atTime(time);
            LocalDateTime end = start.plusMinutes(durationMinutes);

            if (end.toLocalTime().isAfter(workEnd))
                continue;

            boolean overlaps = isOverlapping(takenSlots, start, end);

            if (!overlaps)
                allSlots.add(time);
        }

        return allSlots;
    }

    private static boolean isOverlapping(List<Appointment> takenSlots, LocalDateTime start, LocalDateTime end) {
        for (Appointment takenSlot : takenSlots) {
            if (start.isBefore(takenSlot.getEndTime()) && end.isAfter(takenSlot.getStartTime())) {
                return true;
            }
        }
        return false;
    }

}

















