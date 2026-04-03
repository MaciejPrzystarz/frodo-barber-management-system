package pl.frodo.barber.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.frodo.barber.model.*;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.CustomerRepository;
import pl.frodo.barber.repository.ServiceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;

    public BookingService(AppointmentRepository appointmentRepository, ServiceRepository serviceRepository, CustomerRepository customerRepository) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.customerRepository = customerRepository;
    }

    public AppointmentStatus changeStatus(String status) {
        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalStateException("Nieprawidłowy status: " + status);
        }
    }

    public String checkAppointmentValidation(LocalDate date, LocalTime time, User client, RedirectAttributes redirectAttributes) {
        List<Appointment> appointments = appointmentRepository.findAppointmentByClientOrderByStartTimeAsc(client);

        if (appointments.size() >= 3) {
            redirectAttributes.addFlashAttribute("errorMessage", "Możesz mieć maksymalnie umówione 3 wizyty na raz.");
            return "redirect:/client/dashboard";
        }

        for (Appointment appointment : appointments) {
            LocalDate appointmentDate = appointment.getStartTime().toLocalDate();

            if (appointment.getStatus().equals(AppointmentStatus.PENDING)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Poczekaj na zawtwierdzenie pierwszej wizyty, zanim umówisz kolejną.");
                return "redirect:/client/dashboard";
            }

            if (!date.isBefore(appointmentDate.minusDays(10))
                    && !date.isAfter(appointmentDate.plusDays(10))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Możesz mieć maksymalnie jedną wizytę w ciagu 10 dni.");
                return "redirect:/client/dashboard";
            }
        }

        LocalDate maximumAllowedDate = LocalDate.now().plusDays(45);
        if (date.isAfter(maximumAllowedDate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Możesz umówić wizytę maksymalnie do 45 dni od dzisiaj.");
            return "redirect:/client/dashboard";
        }

        if (date.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie ma możliwości umówienia wizyty z przeszłości.");
            return "redirect:/client/dashboard";
        }

        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie ma możliwości umówienia wizyty z przeszłości.");
            return "redirect:/client/dashboard";
        }

        LocalDateTime requestedDateTime = date.atTime(time);
        LocalDateTime minimumAllowedDateTime = LocalDateTime.now().plusMinutes(60);

        if (requestedDateTime.isBefore(minimumAllowedDateTime)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Możesz umówić wizytę najwcześniej 60 minut od teraz.");
            return "redirect:/client/dashboard";
        }

        return null;
    }

    public void saveAppointmentForExistingCustomer(User barber, Long customerId, Long serviceId,
                                                   LocalDate date, LocalTime time) {

        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new RuntimeException("Nie ma takiego klienta."));
        ServiceItem service = serviceRepository.findById(serviceId).orElseThrow(
                () -> new RuntimeException("Nie ma takiej usługi."));

        LocalDateTime startTime = date.atTime(time);
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        List<Appointment> barberAppointments = appointmentRepository.findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(
                        barber, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .filter(appointment ->
                        appointment.getStatus() == AppointmentStatus.BOOKED || appointment.getStatus() == AppointmentStatus.PENDING)
                .toList();

        boolean overlapping = isOverlapping(barberAppointments, startTime, endTime);

        if (overlapping) {
            throw new RuntimeException("Termin zajęty.");
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.BOOKED);

        appointmentRepository.save(appointment);

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

        List<Appointment> clientAppointments = appointmentRepository.findAppointmentByClient(client);

        if (clientAppointments.isEmpty()) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        appointmentRepository.save(appointment);
    }

    public List<LocalTime> getAvailableSlotsForTheWholeDay(User barber, LocalDate date, int durationMinutes) {
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

        for (LocalTime time = workStart; time.isBefore(workEnd); time = time.plusMinutes(slotMinutes)) {
            LocalDateTime start = date.atTime(time);
            LocalDateTime end = start.plusMinutes(durationMinutes);

            if (end.toLocalTime().isAfter(workEnd)) {
                continue;
            }

            boolean overlaps = isOverlapping(takenSlots, start, end);

            if (!overlaps) {
                allSlots.add(time);
            }
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