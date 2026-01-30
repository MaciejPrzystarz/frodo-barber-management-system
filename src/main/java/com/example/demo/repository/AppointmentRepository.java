package com.example.demo.repository;

import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBarberAndStatusAndStartTimeBetween(
            User barber, AppointmentStatus status, LocalDateTime startTimeAfter, LocalDateTime startTimeBefore);

    boolean existsByBarberAndStatusAndStartTime(User barber, AppointmentStatus status, LocalDateTime startTime);

}
