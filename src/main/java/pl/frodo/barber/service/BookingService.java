package pl.frodo.barber.service;

import org.springframework.stereotype.Service;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.User;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.model.Customer;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.CustomerRepository;
import pl.frodo.barber.repository.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final VacationService vacationService;

    public BookingService(AppointmentRepository appointmentRepository, ServiceRepository serviceRepository, CustomerRepository customerRepository, VacationService vacationService) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.customerRepository = customerRepository;
        this.vacationService = vacationService;
    }

    public AppointmentStatus changeStatus(String status) {
        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalStateException("Nieprawidłowy status: " + status);
        }
    }

    public Optional<String> validateClientBooking(LocalDate date, LocalTime time, User client) {
        List<Appointment> appointments = appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client);

        List<Appointment> activeAppointments = appointments.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.BOOKED
                        || appointment.getStatus() == AppointmentStatus.PENDING)
                .toList();

        if (activeAppointments.size() >= 3) {
            return Optional.of("Możesz mieć maksymalnie umówione 3 wizyty na raz.");
        }

        for (Appointment appointment : activeAppointments) {
            LocalDate appointmentDate = appointment.getStartTime().toLocalDate();

            if (appointment.getStatus() == AppointmentStatus.PENDING) {
                return Optional.of("Poczekaj na zatwierdzenie pierwszej wizyty, zanim umówisz kolejną.");
            }

            if (!date.isBefore(appointmentDate.minusDays(10))
                    && !date.isAfter(appointmentDate.plusDays(10))) {
                return Optional.of("Możesz mieć maksymalnie jedną wizytę w ciągu 10 dni.");
            }
        }

        LocalDate maximumAllowedDate = LocalDate.now().plusDays(45);
        if (date.isAfter(maximumAllowedDate)) {
            return Optional.of("Możesz umówić wizytę maksymalnie do 45 dni od dzisiaj.");
        }

        if (date.isBefore(LocalDate.now())) {
            return Optional.of("Nie ma możliwości umówienia wizyty z przeszłości.");
        }

        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            return Optional.of("Nie ma możliwości umówienia wizyty z przeszłości.");
        }

        LocalDateTime requestedDateTime = date.atTime(time);
        LocalDateTime minimumAllowedDateTime = LocalDateTime.now().plusMinutes(60);

        if (requestedDateTime.isBefore(minimumAllowedDateTime)) {
            return Optional.of("Możesz umówić wizytę najwcześniej 60 minut od teraz.");
        }

        return Optional.empty();
    }

    public void saveAppointmentForNewCustomer(User barber, String fullName, String phoneNumber, Long serviceId, LocalDate date, LocalTime time) {

        validateBarberIsNotOnVacation(barber, date);

        ServiceItem service = serviceRepository.findById(serviceId).orElseThrow(
                () -> new RuntimeException("Nie ma takiej usługi."));

        LocalDateTime startTime = date.atTime(time);
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        validateAppointmentSlot(barber, date, startTime, endTime);

        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setFullName(fullName);
                    newCustomer.setPhoneNumber(phoneNumber);
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

    public List<LocalTime> getAvailableSlotsForTheWholeDay(User barber, LocalDate date, int durationMinutes) {

        if (vacationService.isBarberOnVacation(barber, date)) {
            return List.of();
        }

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        int slotMinutes = 10;

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<LocalTime> allSlots = new ArrayList<>();
        List<Appointment> takenSlots = appointmentRepository.findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
                        barber, from, to)
                .stream()
                .filter(appointment ->
                        appointment.getStatus() == AppointmentStatus.BOOKED || appointment.getStatus() == AppointmentStatus.PENDING)
                .toList();

        LocalDateTime earliestAllowed = LocalDateTime.now().plusMinutes(60);

        for (LocalTime time = workStart; time.isBefore(workEnd); time = time.plusMinutes(slotMinutes)) {
            LocalDateTime start = date.atTime(time);
            LocalDateTime end = start.plusMinutes(durationMinutes);

            if (end.toLocalTime().isAfter(workEnd)) {
                continue;
            }

            if (start.isBefore(earliestAllowed)) {
                continue;
            }

            boolean overlaps = isOverlapping(takenSlots, start, end);

            if (!overlaps) {
                allSlots.add(time);
            }
        }

        return allSlots;
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

    public void saveAppointment(User barber, User client, LocalDateTime startTime, ServiceItem service) {

        validateBarberIsNotOnVacation(barber, startTime.toLocalDate());

        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.BOOKED);

        List<Appointment> clientAppointments = appointmentRepository.findAppointmentByClient(client);

        if (clientAppointments.isEmpty()) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        appointmentRepository.save(appointment);
    }

    private static boolean isOverlapping(List<Appointment> takenSlots, LocalDateTime start, LocalDateTime end) {
        for (Appointment takenSlot : takenSlots) {
            if (start.isBefore(takenSlot.getEndTime()) && end.isAfter(takenSlot.getStartTime())) {
                return true;
            }
        }
        return false;
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
}