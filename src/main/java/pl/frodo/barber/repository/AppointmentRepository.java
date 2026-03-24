package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBarberAndStatusAndStartTimeBetween(
            User barber, AppointmentStatus status, LocalDateTime startTimeAfter, LocalDateTime startTimeBefore);

    List<Appointment> findAppointmentByClientOrderByStartTimeAsc(User client);

    List<Appointment> findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
            User barber, LocalDateTime start, LocalDateTime end);

    List<Appointment> findAppointmentByClient(User client);

}
