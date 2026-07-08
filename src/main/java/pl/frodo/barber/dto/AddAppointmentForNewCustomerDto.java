package pl.frodo.barber.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AddAppointmentForNewCustomerDto {

    @NotBlank(message = "Pole nie może być puste")
    @Size(min = 3, max = 40, message = "Pole musi zawierać 3-40 znaków")
    private String fullName;

    @NotBlank(message = "Pole nie może być puste")
    private String phoneNumber;

    private Long serviceId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;

}
