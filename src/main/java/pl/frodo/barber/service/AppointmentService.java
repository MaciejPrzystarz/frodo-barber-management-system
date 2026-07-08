package pl.frodo.barber.service;

import org.springframework.stereotype.Service;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.Customer;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.CustomerRepository;
import pl.frodo.barber.repository.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final VacationService vacationService;
    private final PhoneNumberService phoneNumberService;

    public AppointmentService(AppointmentRepository appointmentRepository, ServiceRepository serviceRepository,
                              CustomerRepository customerRepository, VacationService vacationService, PhoneNumberService phoneNumberService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.customerRepository = customerRepository;
        this.vacationService = vacationService;
        this.phoneNumberService = phoneNumberService;
    }

    public AppointmentStatus changeStatus(String status) {
        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalStateException("Nieprawidłowy status: " + status);
        }
    }

    public void saveAppointmentForNewCustomer(User barber, String fullName, String phoneNumber,
                                              Long serviceId, LocalDate date, LocalTime time) {

        validateBarberIsNotOnVacation(barber, date);

        String normalizedPhone = phoneNumberService.normalize(phoneNumber);

        ServiceItem service = serviceRepository.findById(serviceId).orElseThrow(
                () -> new RuntimeException("Nie ma takiej usługi."));

        LocalDateTime startTime = date.atTime(time);
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        validateAppointmentSlot(barber, date, startTime, endTime);

        Customer customer = customerRepository.findByPhoneNumber(normalizedPhone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setFullName(fullName);
                    newCustomer.setPhoneNumber(normalizedPhone);
                    return customerRepository.save(newCustomer);
                });

        addNewAppointmentForCustomer(barber, customer, service, startTime, endTime);
    }

    public void saveAppointmentForExistingCustomer(User barber, Long customerId, Long serviceId, LocalDate date, LocalTime time) {

        validateBarberIsNotOnVacation(barber, date);

        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new RuntimeException("Nie ma takiego klienta."));

        ServiceItem service = serviceRepository.findById(serviceId).orElseThrow(
                () -> new RuntimeException("Nie ma takiej usługi."));

        LocalDateTime startTime = date.atTime(time);
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        validateAppointmentSlot(barber, date, startTime, endTime);

        addNewAppointmentForCustomer(barber, customer, service, startTime, endTime);
    }

    private void validateAppointmentSlot(User barber, LocalDate date, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> barberAppointments = appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
                        barber,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                )
                .stream()
                .filter(appointment ->
                        appointment.getStatus() == AppointmentStatus.BOOKED
                                || appointment.getStatus() == AppointmentStatus.PENDING)
                .toList();

        boolean overlapping = isOverlapping(barberAppointments, startTime, endTime);

        if (overlapping) {
            throw new RuntimeException("Termin zajęty.");
        }
    }

    private void addNewAppointmentForCustomer(User barber, Customer customer, ServiceItem service, LocalDateTime startTime, LocalDateTime endTime) {
        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.BOOKED);

        appointmentRepository.save(appointment);
    }

    private void validateBarberIsNotOnVacation(User barber, LocalDate date) {
        if (vacationService.isBarberOnVacation(barber, date)) {
            throw new IllegalArgumentException("Ten barber ma urlop w wybranym dniu. Wybierz inny termin.");
        }
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
