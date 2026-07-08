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
import pl.frodo.barber.model.Customer;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.CustomerRepository;
import pl.frodo.barber.repository.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VacationService vacationService;

    @Mock
    private PhoneNumberService phoneNumberService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User barber;
    private Customer customer;
    private ServiceItem service;

    @BeforeEach
    void setUp() {
        barber = createUser(1L, "Frodo Barber", Role.BARBER);
        customer = createCustomer(10L, "Jan Nowak", "123456789");
        service = createServiceItem(1L, 30);
    }

    @Test
    void shouldChangeAppointmentStatus_whenStatusIsValid() {
        AppointmentStatus result = appointmentService.changeStatus("booked");

        assertThat(result).isEqualTo(AppointmentStatus.BOOKED);
    }

    @Test
    void shouldThrowException_whenStatusIsInvalid() {
        assertThatThrownBy(() -> appointmentService.changeStatus("NOT_A_STATUS"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nieprawidłowy status");
    }

    @Test
    void shouldSaveAppointment_whenSlotIsFree() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(12, 0);

        when(vacationService.isBarberOnVacation(barber, date)).thenReturn(false);
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(appointmentRepository.findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
                eq(barber), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        appointmentService.saveAppointmentForExistingCustomer(
                barber, customer.getId(), service.getId(), date, time);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        Appointment saved = captor.getValue();
        assertThat(saved.getBarber()).isEqualTo(barber);
        assertThat(saved.getCustomer()).isEqualTo(customer);
        assertThat(saved.getService()).isEqualTo(service);
        assertThat(saved.getStartTime()).isEqualTo(date.atTime(time));
        assertThat(saved.getEndTime()).isEqualTo(date.atTime(time).plusMinutes(30));
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    }

    @Test
    void shouldNotSaveAppointment_whenSlotIsBlocked() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(12, 0);

        Appointment existing = new Appointment();
        existing.setStartTime(date.atTime(11, 50));
        existing.setEndTime(date.atTime(12, 20));
        existing.setStatus(AppointmentStatus.BOOKED);

        when(vacationService.isBarberOnVacation(barber, date)).thenReturn(false);
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(appointmentRepository.findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
                eq(barber), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> appointmentService.saveAppointmentForExistingCustomer(
                barber, customer.getId(), service.getId(), date, time))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Termin zajęty");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldThrowException_whenBarberIsOnVacation() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(12, 0);

        when(vacationService.isBarberOnVacation(barber, date)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.saveAppointmentForExistingCustomer(
                barber, customer.getId(), service.getId(), date, time))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("urlop");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldCreateNewCustomer_whenPhoneNumberNotFound() {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(12, 0);
        String fullName = "Anna Kowalska";
        String phoneNumber = "987654321";

        when(vacationService.isBarberOnVacation(barber, date)).thenReturn(false);
        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(appointmentRepository.findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
                eq(barber), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(phoneNumberService.normalize(phoneNumber)).thenReturn(phoneNumber);
        when(customerRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        appointmentService.saveAppointmentForNewCustomer(
                barber, fullName, phoneNumber, service.getId(), date, time);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getFullName()).isEqualTo(fullName);
        assertThat(customerCaptor.getValue().getPhoneNumber()).isEqualTo(phoneNumber);

        verify(appointmentRepository).save(any(Appointment.class));
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

    private Customer createCustomer(Long id, String fullName, String phoneNumber) {
        Customer c = new Customer();
        c.setId(id);
        c.setFullName(fullName);
        c.setPhoneNumber(phoneNumber);
        return c;
    }

    private ServiceItem createServiceItem(Long id, int durationMinutes) {
        ServiceItem item = new ServiceItem();
        item.setId(id);
        item.setName("Strzyżenie");
        item.setPrice(50);
        item.setDurationMinutes(durationMinutes);
        return item;
    }
}
