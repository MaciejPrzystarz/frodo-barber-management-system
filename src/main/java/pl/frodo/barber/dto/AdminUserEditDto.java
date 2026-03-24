package pl.frodo.barber.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserEditDto {

    private Long id;

    @NotBlank(message = "Pole nie może byc puste")
    @Size(min = 3, max = 40, message = "Pole musi zawierać 3-40 znaków")
    private String fullName;

    @NotBlank(message = "Pole nie może byc puste")
    @Pattern(regexp = "^[0-9]{9}$", message = "Numer telefonu musi składać się z 9 cyfr")
    private String phoneNumber;

    @NotBlank(message = "Pole nie może byc puste")
    @Email(message = "Podaj poprawny adres email")
    private String email;

    private String newPassword;
}
