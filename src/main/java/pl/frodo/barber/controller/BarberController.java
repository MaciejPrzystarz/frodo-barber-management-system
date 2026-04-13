package pl.frodo.barber.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import pl.frodo.barber.dto.AddAppointmentForExistingCustomerDto;
import pl.frodo.barber.dto.AddAppointmentForNewCustomerDto;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.Customer;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.CustomerRepository;
import pl.frodo.barber.repository.ServiceRepository;
import pl.frodo.barber.repository.UserRepository;
import pl.frodo.barber.service.BookingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/barber")
public class BarberController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final BookingService bookingService;

    public BarberController(AppointmentRepository appointmentRepository, UserRepository userRepository,
                            CustomerRepository customerRepository, ServiceRepository serviceRepository, BookingService bookingService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/vacation")
    public String vacation() {

        return "barber/vacation";
    }

    @GetMapping("/my-week")
    public String myWeek() {

        return "barber/my-week";
    }

    @GetMapping("/add-appointment")
    public String addAppointment(@RequestParam(required = false) String query, Model model) {

        List<Customer> allCustomers = customerRepository.findAll();

        if (query != null && !query.isEmpty()) {

            List<Customer> customers = allCustomers.stream()
                    .filter(customer -> customer.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                            customer.getPhoneNumber().contains(query))
                    .toList();

            model.addAttribute("customers", customers);
        }

        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("existingCustomer", new AddAppointmentForExistingCustomerDto());
        model.addAttribute("newCustomer", new AddAppointmentForNewCustomerDto());

        return "barber/add-appointment";
    }

    @PostMapping("/add-appointment/existing")
    public String addAppointmentToExistingCustomer(@ModelAttribute("existingCustomer") AddAppointmentForExistingCustomerDto form,
                                                   Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera."));

            bookingService.saveAppointmentForExistingCustomer(barber, form.getCustomerId(), form.getServiceId(), form.getDate(), form.getTime());

            redirectAttributes.addFlashAttribute("successMessage", "Wizyta została dodana.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie udało się zapisać wizyty.");
            return "redirect:/barber/add-appointment";
        }

        return "redirect:/barber/add-appointment";
    }

    @PostMapping("/add-appointment/new")
    public String addAppointmentToNewCustomer(@ModelAttribute("newCustomer") AddAppointmentForNewCustomerDto form,
                                              Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera."));

            bookingService.saveAppointmentForNewCustomer(barber, form.getFullName(), form.getPhoneNumber(), form.getServiceId(),
                    form.getDate(), form.getTime());

            redirectAttributes.addFlashAttribute("successMessage", "Wizyta została dodana.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie udało się zapisać wizyty.");
            return "redirect:/barber/add-appointment";
        }
        return "redirect:/barber/add-appointment";
    }

    @GetMapping("/pending-appointments")
    public String pendingAppointments(Model model) {
        List<Appointment> pendingAppointments = appointmentRepository.findAppointmentByStatus(AppointmentStatus.PENDING);

        model.addAttribute("pendingAppointments", pendingAppointments);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());

        return "barber/pending-appointments";
    }

    @PostMapping("/appointments/{id}")
    String changeStatus(@PathVariable Long id, @RequestParam String status,
                        Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Nie ma takiej wizyty"));

        if (!appointment.getBarber().getId().equals(barber.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Brak dostępu do tej wizyty.");
            return "redirect:/barber/dashboard";
        }
        if (appointment.getStatus() == AppointmentStatus.DONE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie można zmienić statusu zakończonej wizyty.");
            return "redirect:/barber/dashboard";
        }

        try {
            AppointmentStatus appointmentStatus = bookingService.changeStatus(status);

            appointment.setStatus(appointmentStatus);
            appointmentRepository.save(appointment);

            redirectAttributes.addFlashAttribute("successMessage", "Status wizyty został zmieniony na " + status + ".");
            List<Appointment> pendingAppointmentsList = appointmentRepository.findAppointmentByStatus(AppointmentStatus.PENDING);

            if (!pendingAppointmentsList.isEmpty()) {
                return "redirect:/barber/pending-appointments";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie udało się zmienić statusu wizyty");
            return "redirect:/barber/dashboard";
        }

        return "redirect:/barber/dashboard";
    }

    @GetMapping("/dashboard")
    public String home(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date,
                       Authentication authentication, Model model) {
        String email = authentication.getName();
        User barber = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));

        LocalDate selectedDate = (date == null) ? LocalDate.now() : date;
        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findAppointmentByBarberAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
                barber, AppointmentStatus.BOOKED, start, end);

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("appointments", appointments);
        model.addAttribute("appointmentStatuses", AppointmentStatus.values());

        return "barber/dashboard";
    }
}
