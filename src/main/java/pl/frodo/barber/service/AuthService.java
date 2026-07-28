package pl.frodo.barber.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.frodo.barber.dto.AdminUserEditDto;
import pl.frodo.barber.dto.UserFormDto;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.UserRepository;

import java.util.Optional;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PhoneNumberService phoneNumberService;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, PhoneNumberService phoneNumberService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.phoneNumberService = phoneNumberService;
    }

    public User dtoToUser(UserFormDto userFormDto) {
        User user = new User();
        user.setFullName(userFormDto.getFullName());
        user.setPhoneNumber(phoneNumberService.normalize(userFormDto.getPhoneNumber()));
        user.setEmail(userFormDto.getEmail());
        user.setPassword(passwordEncoder.encode(userFormDto.getPassword()));
        user.setRole(Role.CLIENT);
        return user;
    }

    public AdminUserEditDto adminUserToDto(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Nie ma użytkownika o takim id"));

        AdminUserEditDto dto = new AdminUserEditDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        return dto;
    }

    @Transactional
    public void updateUser(AdminUserEditDto dto) {
        User user = userRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Nie ma użytkownika o takim id"));

        Optional<User> userByEmail = userRepository.findByEmail(dto.getEmail());

        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(dto.getId())) {
            throw new IllegalStateException("Podany email jest już zajęty");
        }

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(phoneNumberService.normalize(dto.getPhoneNumber()));
        user.setEmail(dto.getEmail());
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        userRepository.save(user);
    }
}
