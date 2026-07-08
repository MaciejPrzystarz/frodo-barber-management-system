package pl.frodo.barber.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.frodo.barber.dto.AdminUserEditDto;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PhoneNumberService phoneNumberService;

    @InjectMocks
    private AuthService authService;

    private User existing;

    @BeforeEach
    void setUp() {
        existing = new User();
        existing.setId(1L);
        existing.setFullName("Jan Kowalski");
        existing.setPhoneNumber("123456789");
        existing.setEmail("jan@demo.pl");
        existing.setPassword("STARY_HASH");
        existing.setRole(Role.CLIENT);
    }

    @Test
    void updateUser_changesPassword_whenNewPasswordProvided() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("jan@demo.pl")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("NoweHaslo1!")).thenReturn("NOWY_HASH");

        AdminUserEditDto dto = dtoFor("jan@demo.pl", "NoweHaslo1!");

        authService.updateUser(dto);

        assertThat(existing.getPassword()).isEqualTo("NOWY_HASH");
    }

    @Test
    void updateUser_throws_whenEmailTakenByAnotherUser() {
        User other = new User();
        other.setId(2L);
        other.setEmail("zajety@demo.pl");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("zajety@demo.pl")).thenReturn(Optional.of(other));

        AdminUserEditDto dto = dtoFor("zajety@demo.pl", "");

        assertThatThrownBy(() -> authService.updateUser(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("zajęty");

        verify(userRepository, never()).save(any());
    }

    private AdminUserEditDto dtoFor(String email, String newPassword) {
        AdminUserEditDto dto = new AdminUserEditDto();
        dto.setId(1L);
        dto.setFullName("Jan Kowalski");
        dto.setPhoneNumber("123456789");
        dto.setEmail(email);
        dto.setNewPassword(newPassword);
        return dto;
    }

}
