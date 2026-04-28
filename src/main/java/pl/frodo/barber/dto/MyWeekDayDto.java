package pl.frodo.barber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.frodo.barber.model.Appointment;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyWeekDayDto {

    private LocalDate date;
    private List<Appointment> appointments;
    private int appointmentsCount;
    private long doneCount;
    private String realizedIncomeFormatted;

}