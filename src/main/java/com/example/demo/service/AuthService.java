package com.example.demo.service;

import com.example.demo.dto.AdminUserEditDto;
import com.example.demo.dto.UserFormDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User dtoToUser(UserFormDto userFormDto) {
        User user = new User();
        user.setFullName(userFormDto.getFullName());
        user.setPhoneNumber(userFormDto.getPhoneNumber());
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
        dto.setNewPassword(user.getPassword());
        return dto;
    }

    public void updateUser(AdminUserEditDto dto){
        User user = userRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Nie ma użytkownika o takim id"));

        Optional<User> userByEmail = userRepository.findByEmail(dto.getEmail());

            if (userByEmail.isPresent() && !userByEmail.get().getId().equals(dto.getId())) {
                throw new IllegalStateException("Podany email jest już zajęty");
        }

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        userRepository.save(user);
    }
}
