package com.example.demo.controller;

import com.example.demo.dto.AdminUserEditDto;
import com.example.demo.model.Appointment;
import com.example.demo.model.ServiceItem;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        return "/admin/user-edit";
    }

    @PostMapping("/users/edit")
    public String editUserForm(@Valid @ModelAttribute("userForm") AdminUserEditDto adminUserEditDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/admin/user-edit";
        }
        try {
            authService.updateUser(adminUserEditDto);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "email.taken", e.getMessage());
            return "/admin/user-edit";
        }

        return "redirect:/admin/dashboard";
    }
}
