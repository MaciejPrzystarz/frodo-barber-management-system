package pl.frodo.barber.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private VacationService vacationService;

    @InjectMocks
    private BookingService bookingService;

    private User client;
    private User barber;
    private ServiceItem service;

    @BeforeEach
    void setUp() {
        client = createUser(1L, "Jan Kowalski", Role.CLIENT);
        barber = createUser(2L, "Frodo Barber", Role.BARBER);
        service = createServiceItem(30);
    }

    @Test
    void shouldReturnEmpty_whenBookingIsValid() {
        when(appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client))
                .thenReturn(List.of());

        LocalDate date = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(12, 0);

        Optional<String> result = bookingService.validateClientBooking(date, time, client);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnError_whenClientHas3ActiveAppointments() {
        Appointment a1 = createAppointment(LocalDateTime.now().plusDays(20), AppointmentStatus.BOOKED);
        Appointment a2 = createAppointment(LocalDateTime.now().plusDays(35), AppointmentStatus.BOOKED);
        Appointment a3 = createAppointment(LocalDateTime.now().plusDays(40), AppointmentStatus.BOOKED);

        when(appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client))
                .thenReturn(List.of(a1, a2, a3));

        Optional<String> result = bookingService.validateClientBooking(
                LocalDate.now().plusDays(5), LocalTime.of(12, 0), client);

        assertThat(result).contains("Możesz mieć maksymalnie umówione 3 wizyty na raz.");
    }

    @Test
    void shouldReturnError_whenClientHasPendingAppointment() {
        Appointment pending = createAppointment(LocalDateTime.now().plusDays(30), AppointmentStatus.PENDING);

        when(appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client))
                .thenReturn(List.of(pending));

        Optional<String> result = bookingService.validateClientBooking(
                LocalDate.now().plusDays(5), LocalTime.of(12, 0), client);

        assertThat(result).contains("Poczekaj na zatwierdzenie pierwszej wizyty, zanim umówisz kolejną.");
    }

    @Test
    void shouldReturnError_whenWithin10DaysOfActiveAppointment() {
        LocalDateTime existingStart = LocalDate.now().plusDays(20).atTime(12, 0);
        Appointment booked = createAppointment(existingStart, AppointmentStatus.BOOKED);

        when(appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client))
                .thenReturn(List.of(booked));

        Optional<String> result = bookingService.validateClientBooking(
                LocalDate.now().plusDays(25), LocalTime.of(12, 0), client);

        assertThat(result).contains("Możesz mieć maksymalnie jedną wizytę w ciągu 10 dni.");
    }

    @Test
    void shouldSavePending_whenClientHasNoPreviousAppointments() {
        LocalDateTime startTime = LocalDate.now().plusDays(5).atTime(12, 0);
        when(vacationService.isBarberOnVacation(barber, startTime.toLocalDate())).thenReturn(false);
        when(appointmentRepository.findAppointmentByClient(client)).thenReturn(List.of());

        bookingService.saveAppointment(barber, client, startTime, service);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        Appointment saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(saved.getClient()).isEqualTo(client);
        assertThat(saved.getBarber()).isEqualTo(barber);
        assertThat(saved.getStartTime()).isEqualTo(startTime);
        assertThat(saved.getEndTime()).isEqualTo(startTime.plusMinutes(30));
    }

    @Test
    void shouldThrow_whenBarberIsOnVacation() {
        LocalDateTime startTime = LocalDate.now().plusDays(5).atTime(12, 0);
        when(vacationService.isBarberOnVacation(barber, startTime.toLocalDate())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.saveAppointment(barber, client, startTime, service))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("urlop");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    private User createUser(Long id, String fullName, Role role) {
        User user = new User();
        user.setId(id);
        user.setFullName(fullName);
        user.setEmail(fullName.replace(" ", ".").toLowerCase() + "@test.local");
        user.setPassword("password");
        user.setPhoneNumber("123456789");
        user.setRole(role);
        return user;
    }

    private ServiceItem createServiceItem(int durationMinutes) {
        ServiceItem item = new ServiceItem();
        item.setName("Strzyżenie");
        item.setPrice(50);
        item.setDurationMinutes(durationMinutes);
        return item;
    }

    private Appointment createAppointment(LocalDateTime startTime, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setStartTime(startTime);
        appointment.setEndTime(startTime.plusMinutes(30));
        appointment.setStatus(status);
        return appointment;
    }
}
