package pl.frodo.barber.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VacationFormDto {

    @NotNull(message = "Wybierz datę rozpoczęcia urlopu.")
    private LocalDate startDate;

    @NotNull(message = "Wybierz datę zakończenia urlopu.")
    private LocalDate endDate;

}
