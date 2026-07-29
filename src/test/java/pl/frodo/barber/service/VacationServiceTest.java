package pl.frodo.barber.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.User;
import pl.frodo.barber.model.Vacation;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.VacationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacationServiceTest {

    @Mock
    private VacationRepository vacationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private VacationService vacationService;

    private User barber;

    @BeforeEach
    void setUp() {
        barber = createUser(1L, "Frodo Barber", Role.BARBER);
    }

    @Test
    void shouldReturnTrue_whenBarberIsOnVacation() {
        LocalDate date = LocalDate.now().plusDays(5);
        when(vacationRepository
                .existsByBarberAndStartDateLessThanEqualAndEndDateGreaterThanEqual(barber, date, date))
                .thenReturn(true);

        boolean result = vacationService.isBarberOnVacation(barber, date);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenBarberIsNotOnVacation() {
        LocalDate date = LocalDate.now().plusDays(5);
        when(vacationRepository
                .existsByBarberAndStartDateLessThanEqualAndEndDateGreaterThanEqual(barber, date, date))
                .thenReturn(false);

        boolean result = vacationService.isBarberOnVacation(barber, date);

        assertThat(result).isFalse();
    }

    @Test
    void shouldSaveVacation_whenDatesAreValid() {
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(15);

        when(vacationRepository.findByBarberOrderByStartDateAsc(barber)).thenReturn(List.of());
        when(appointmentRepository.existsByBarberAndStatusInAndStartTimeBetween(
                eq(barber), anyCollection(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        vacationService.addVacation(barber, startDate, endDate);

        ArgumentCaptor<Vacation> captor = ArgumentCaptor.forClass(Vacation.class);
        verify(vacationRepository).save(captor.capture());

        Vacation saved = captor.getValue();
        assertThat(saved.getBarber()).isEqualTo(barber);
        assertThat(saved.getStartDate()).isEqualTo(startDate);
        assertThat(saved.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void shouldThrowException_whenEndDateIsBeforeStartDate() {
        LocalDate startDate = LocalDate.now().plusDays(15);
        LocalDate endDate = LocalDate.now().plusDays(10);

        assertThatThrownBy(() -> vacationService.addVacation(barber, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data rozpoczęcia nie może być późniejsza");

        verify(vacationRepository, never()).save(any(Vacation.class));
    }

    @Test
    void shouldThrowException_whenBarberHasAppointmentsInRange() {
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(15);

        when(vacationRepository.findByBarberOrderByStartDateAsc(barber)).thenReturn(List.of());
        when(appointmentRepository.existsByBarberAndStatusInAndStartTimeBetween(
                eq(barber), anyCollection(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> vacationService.addVacation(barber, startDate, endDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("masz już wizyty");

        verify(vacationRepository, never()).save(any(Vacation.class));
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
}
