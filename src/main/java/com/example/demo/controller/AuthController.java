package com.example.demo.controller;

import com.example.demo.dto.UserFormDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminService;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    String login() {
        return "/auth/login";
    }

    @GetMapping("/register")
    String register(Model model) {
        model.addAttribute("userForm", new UserFormDto());
        return "/auth/register";
    }

    @PostMapping("/register")
    String registerForm(@Valid @ModelAttribute("userForm") UserFormDto userFormDto, BindingResult bindingResult, Model model) {

        if (!userFormDto.getPassword().equals(userFormDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Hasła nie są takie same");
        }

        if (bindingResult.hasErrors()) {
            return "/auth/register";
        }

        User user = authService.toUser(userFormDto);
        userRepository.save(user);
        return "redirect:/login";
    }
}
