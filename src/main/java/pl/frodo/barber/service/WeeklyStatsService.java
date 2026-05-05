package pl.frodo.barber.service;

import org.springframework.stereotype.Service;
import pl.frodo.barber.dto.MyWeekDayDto;
import pl.frodo.barber.dto.MyWeekDto;
import pl.frodo.barber.model.Appointment;
import pl.frodo.barber.model.AppointmentStatus;
import pl.frodo.barber.model.User;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class WeeklyStatsService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public WeeklyStatsService(UserRepository userRepository, AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public MyWeekDto getMyWeek(String barberEmail, LocalDate selectedDate) {
        User barber = userRepository.findByEmail(barberEmail)
                .orElseThrow(() -> new RuntimeException("Nie ma takiego barbera"));

        LocalDate today = LocalDate.now();

        if (selectedDate == null) {
            selectedDate = today;
        }

        LocalDate weekStart = selectedDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = selectedDate.with(DayOfWeek.SUNDAY);

        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekEnd.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository
                .findAppointmentByBarberAndStartTimeBetweenOrderByStartTimeAsc(barber, start, end);

        List<MyWeekDayDto> days = new ArrayList<>();

        int totalDoneAppointments = 0;
        int totalRevenue = 0;
        long totalMinutes = 0;

        LocalDate bestDay = null;
        int bestDayRevenue = 0;
        long bestDayDoneCount = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<Appointment> appointmentsForDay = new ArrayList<>();

            for (Appointment appointment : appointments) {
                if (appointment.getStartTime().toLocalDate().equals(day)) {
                    appointmentsForDay.add(appointment);
                }
            }

            int dayRevenue = 0;
            long dayDoneCount = 0;

            for (Appointment appointment : appointmentsForDay) {
                if (appointment.getStatus() == AppointmentStatus.DONE) {
                    dayDoneCount++;
                    totalDoneAppointments++;

                    int price = appointment.getService().getPrice();
                    dayRevenue += price;
                    totalRevenue += price;

                    long minutes = ChronoUnit.MINUTES.between(
                            appointment.getStartTime(),
                            appointment.getEndTime()
                    );
                    totalMinutes += minutes;
                }
            }

            if (bestDay == null || dayRevenue > bestDayRevenue) {
                bestDay = day;
                bestDayRevenue = dayRevenue;
                bestDayDoneCount = dayDoneCount;
            }

            MyWeekDayDto dayDto = new MyWeekDayDto(
                    day,
                    appointmentsForDay,
                    appointmentsForDay.size(),
                    dayDoneCount,
                    dayRevenue + " zł",
                    false
            );

            days.add(dayDto);
        }

        for (MyWeekDayDto day : days) {
            if (bestDayRevenue > 0 && day.getDate().equals(bestDay)) {
                day.setBestDay(true);
            }
        }

        String bestDayLabel = "Brak danych";
        String bestDayDetails = "0 zrealizowane • 0 zł";
        LocalDate bestDayDate = null;

        if (bestDayRevenue > 0) {
            bestDayDate = bestDay;

            bestDayLabel = bestDay.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("pl", "PL"));

            bestDayDetails = bestDayDoneCount + " zrealizowane • " + bestDayRevenue + " zł";
        }

        return new MyWeekDto(
                weekStart,
                weekEnd,
                weekStart.minusWeeks(1),
                weekStart.plusWeeks(1),
                today,
                bestDayDate,
                days,
                totalDoneAppointments,
                formatMinutes(totalMinutes),
                totalRevenue + " zł",
                bestDayLabel,
                bestDayDetails
        );
    }

    private String formatMinutes(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return hours + " h " + minutes + " min";
    }
}
