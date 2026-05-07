package pl.frodo.barber.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.frodo.barber.dto.AdminUserEditDto;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.ServiceRepository;
import pl.frodo.barber.repository.UserRepository;
import pl.frodo.barber.service.AuthService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;

    public AdminController(AuthService authService, UserRepository userRepository, AppointmentRepository appointmentRepository, ServiceRepository serviceRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();
        List<ServiceItem> allServices = serviceRepository.findAll();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allAppointments", allAppointments);
        model.addAttribute("allServices", allServices);
        return "admin/dashboard";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        AdminUserEditDto userEditDto = authService.adminUserToDto(id);
        model.addAttribute("userForm", userEditDto);
        return "admin/user-edit";
    }

    @PostMapping("/users/edit")
    public String editUserForm(@Valid @ModelAttribute("userForm") AdminUserEditDto adminUserEditDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "admin/user-edit";
        }
        try {
            authService.updateUser(adminUserEditDto);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "email.taken", e.getMessage());
            return "admin/user-edit";
        }

        return "redirect:/admin/dashboard";
    }
}
