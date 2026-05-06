package pl.frodo.barber.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.frodo.barber.dto.MyWeekDto;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyStatsServiceTest {

    private static final String BARBER_EMAIL = "frodo.barber@test.local";
    private static final LocalDate MONDAY = LocalDate.of(2026, 5, 4);

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private WeeklyStatsService weeklyStatsService;

    private User barber;

    @BeforeEach
    void setUp() {
        barber = createUser("Frodo Barber", Role.BARBER);
        when(userRepository.findByEmail(BARBER_EMAIL)).thenReturn(Optional.of(barber));
    }

    @Test
    void shouldCalculateWeeklyStats_forDoneAppointments() {
        Appointment a1 = createAppointment(MONDAY.atTime(10, 0), 30, 50, AppointmentStatus.DONE);
        Appointment a2 = createAppointment(MONDAY.plusDays(1).atTime(12, 0), 60, 80, AppointmentStatus.DONE);

        when(appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(eq(barber), any(), any()))
                .thenReturn(List.of(a1, a2));

        MyWeekDto result = weeklyStatsService.getMyWeek(BARBER_EMAIL, MONDAY);

        assertThat(result.getDoneAppointmentsCount()).isEqualTo(2);
        assertThat(result.getRealizedIncomeFormatted()).isEqualTo("130 zł");
        assertThat(result.getWorkedHoursFormatted()).isEqualTo("1 h 30 min");
        assertThat(result.getDays()).hasSize(7);
    }

    @Test
    void shouldReturnZeroStats_whenNoAppointments() {
        when(appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(eq(barber), any(), any()))
                .thenReturn(List.of());

        MyWeekDto result = weeklyStatsService.getMyWeek(BARBER_EMAIL, MONDAY);

        assertThat(result.getDoneAppointmentsCount()).isZero();
        assertThat(result.getRealizedIncomeFormatted()).isEqualTo("0 zł");
        assertThat(result.getWorkedHoursFormatted()).isEqualTo("0 h 0 min");
        assertThat(result.getBestDayDate()).isNull();
        assertThat(result.getBestDayLabel()).isEqualTo("Brak danych");
    }

    @Test
    void shouldIgnoreAppointmentsWithNonDoneStatuses() {
        Appointment done = createAppointment(MONDAY.atTime(10, 0), 30, 50, AppointmentStatus.DONE);
        Appointment booked = createAppointment(MONDAY.atTime(12, 0), 30, 50, AppointmentStatus.BOOKED);
        Appointment cancelled = createAppointment(MONDAY.atTime(14, 0), 30, 50, AppointmentStatus.CANCELLED);
        Appointment pending = createAppointment(MONDAY.atTime(16, 0), 30, 50, AppointmentStatus.PENDING);

        when(appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(eq(barber), any(), any()))
                .thenReturn(List.of(done, booked, cancelled, pending));

        MyWeekDto result = weeklyStatsService.getMyWeek(BARBER_EMAIL, MONDAY);

        assertThat(result.getDoneAppointmentsCount()).isEqualTo(1);
        assertThat(result.getRealizedIncomeFormatted()).isEqualTo("50 zł");
        assertThat(result.getWorkedHoursFormatted()).isEqualTo("0 h 30 min");
    }

    @Test
    void shouldCalculateTotalRevenue() {
        Appointment a1 = createAppointment(MONDAY.atTime(10, 0), 30, 40, AppointmentStatus.DONE);
        Appointment a2 = createAppointment(MONDAY.plusDays(2).atTime(11, 0), 30, 60, AppointmentStatus.DONE);
        Appointment a3 = createAppointment(MONDAY.plusDays(4).atTime(13, 0), 30, 100, AppointmentStatus.DONE);

        when(appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(eq(barber), any(), any()))
                .thenReturn(List.of(a1, a2, a3));

        MyWeekDto result = weeklyStatsService.getMyWeek(BARBER_EMAIL, MONDAY);

        assertThat(result.getRealizedIncomeFormatted()).isEqualTo("200 zł");
    }

    @Test
    void shouldCalculateTotalHours() {
        Appointment a1 = createAppointment(MONDAY.atTime(9, 0), 45, 50, AppointmentStatus.DONE);
        Appointment a2 = createAppointment(MONDAY.plusDays(1).atTime(10, 0), 75, 50, AppointmentStatus.DONE);

        when(appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(eq(barber), any(), any()))
                .thenReturn(List.of(a1, a2));

        MyWeekDto result = weeklyStatsService.getMyWeek(BARBER_EMAIL, MONDAY);

        assertThat(result.getWorkedHoursFormatted()).isEqualTo("2 h 0 min");
    }

    private User createUser(String fullName, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(BARBER_EMAIL);
        user.setPassword("password");
        user.setPhoneNumber("123456789");
        user.setRole(role);
        return user;
    }

    private Appointment createAppointment(LocalDateTime startTime, int durationMinutes, int price, AppointmentStatus status) {
        ServiceItem service = new ServiceItem();
        service.setName("Strzyżenie");
        service.setPrice(price);
        service.setDurationMinutes(durationMinutes);

        Appointment appointment = new Appointment();
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartTime(startTime);
        appointment.setEndTime(startTime.plusMinutes(durationMinutes));
        appointment.setStatus(status);
        return appointment;
    }
}
