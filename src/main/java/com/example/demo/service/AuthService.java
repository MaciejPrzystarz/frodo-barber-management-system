package com.example.demo.service;

import com.example.demo.dto.UserFormDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toUser(UserFormDto userFormDto) {
        User user = new User();
        user.setFullName(userFormDto.getFullName());
        user.setPhoneNumber(userFormDto.getPhoneNumber());
        user.setEmail(userFormDto.getEmail());
        user.setPassword(passwordEncoder.encode(userFormDto.getPassword()));
        user.setRole(Role.CLIENT);
        return user;
    }

}
