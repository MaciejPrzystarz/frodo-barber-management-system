package pl.frodo.barber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyWeekDto {

    private LocalDate weekStart;
    private LocalDate weekEnd;
    private LocalDate previousWeek;
    private LocalDate nextWeek;
    private LocalDate today;
    private LocalDate bestDayDate;
    private List<MyWeekDayDto> days;
    private int doneAppointmentsCount;
    private String workedHoursFormatted;
    private String realizedIncomeFormatted;
    private String bestDayLabel;
    private String bestDayDetails;

}