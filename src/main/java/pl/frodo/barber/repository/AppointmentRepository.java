package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAppointmentByClientOrderByStartTimeAsc(User client);

    List<Appointment> findAppointmentByClientOrderByStartTimeDesc(User client);

    List<Appointment> findAppointmentByClientAndStatusOrderByStartTimeDesc(User client, AppointmentStatus appointmentStatus, AppointmentStatus secondAppointmentStatus, AppointmentStatus thirdAppointmentStatus);

    List<Appointment> findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
            User barber, LocalDateTime startTime, LocalDateTime end);

    List<Appointment> findAppointmentByBarberAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
            User barber, AppointmentStatus appointmentStatus, LocalDateTime startTime, LocalDateTime end);

    List<Appointment> findAppointmentByClient(User client);

    List<Appointment> findAppointmentByStatus(AppointmentStatus status);

    boolean existsByBarberAndStartTimeBetween(User barber, LocalDateTime start, LocalDateTime end);
}
