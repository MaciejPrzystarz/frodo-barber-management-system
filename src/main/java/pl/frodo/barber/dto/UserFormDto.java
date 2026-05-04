package pl.frodo.barber.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFormDto {

    @NotBlank(message = "Pole nie może być puste")
    @Size(min = 3, max = 40, message = "Pole musi zawierać 3-40 znaków")
    private String fullName;

    @NotBlank(message = "Pole nie może być puste")
    @Pattern(regexp = "^[0-9]{9}$", message = "Numer telefonu musi składać się z 9 cyfr")
    private String phoneNumber;

    @NotBlank(message = "Pole nie może być puste")
    @Email(message = "Podaj poprawny adres email")
    private String email;

    @NotBlank(message = "Pole nie może być puste")
    @Size(min = 8, message = "Hasło musi zawierać co najmniej 8 znaków")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
            message = "Hasło musi zawierać: 1 wielką literę, 1 małą literę, 1 cyfrę i 1 znak specjalny")
    private String password;

    @NotBlank(message = "Pole nie może być puste")
    private String confirmPassword;

}
