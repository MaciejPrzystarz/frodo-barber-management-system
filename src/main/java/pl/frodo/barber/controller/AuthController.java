package pl.frodo.barber.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.frodo.barber.dto.UserFormDto;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.UserRepository;
import pl.frodo.barber.service.AuthService;
import pl.frodo.barber.service.PhoneNumberService;

@Controller
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PhoneNumberService phoneNumberService;

    public AuthController(AuthService authService, UserRepository userRepository, PhoneNumberService phoneNumberService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.phoneNumberService = phoneNumberService;
    }

    @GetMapping("/login")
    String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    String register(Model model) {
        model.addAttribute("userForm", new UserFormDto());
        return "auth/register";
    }

    @PostMapping("/register")
    String registerForm(@Valid @ModelAttribute("userForm") UserFormDto userFormDto,
                        BindingResult bindingResult) {

        if (!userFormDto.getPassword().equals(userFormDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Hasła nie są takie same");
        }

        if (userRepository.findByEmail(userFormDto.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "email.taken", "Ten adres email jest już zajęty");
        }

        if (!phoneNumberService.isValid(userFormDto.getPhoneNumber())) {
            bindingResult.rejectValue("phoneNumber", "phone.invalid", "Nieprawidłowy numer telefonu");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        User user = authService.dtoToUser(userFormDto);
        userRepository.save(user);
        return "redirect:/login";
    }
}
