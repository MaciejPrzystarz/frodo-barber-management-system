package pl.frodo.barber.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.frodo.barber.model.User;
import pl.frodo.barber.model.Vacation;
import pl.frodo.barber.repository.AppointmentRepository;
import pl.frodo.barber.repository.VacationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationRepository vacationRepository;
    private final AppointmentRepository appointmentRepository;

    public List<Vacation> getVacationsForBarber(User barber) {
        return vacationRepository.findByBarberOrderByStartDateAsc(barber);
    }

    public List<Vacation> getCurrentAndFutureVacations() {
        return vacationRepository.findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate.now());
    }

    public boolean isBarberOnVacation(User barber, LocalDate date) {
        return vacationRepository.existsByBarberAndStartDateLessThanEqualAndEndDateGreaterThanEqual(barber, date, date);
    }

    public void addVacation(User barber, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Wybierz datę rozpoczęcia i zakończenia urlopu.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data rozpoczęcia nie może być późniejsza niż data zakończenia.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Nie możesz dodać urlopu w przeszłości.");
        }

        if (hasOverlappingVacation(barber, startDate, endDate)) {
            throw new IllegalArgumentException("Masz już urlop w tym terminie.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        boolean hasAppointments = appointmentRepository.existsByBarberAndStartTimeBetween(barber, startDateTime, endDateTime);

        if (hasAppointments) {
            throw new IllegalArgumentException("Nie możesz dodać urlopu, bo masz już wizyty w tym terminie.");
        }

        Vacation vacation = new Vacation();
        vacation.setBarber(barber);
        vacation.setStartDate(startDate);
        vacation.setEndDate(endDate);

        vacationRepository.save(vacation);
    }

    public void deleteVacation(Long vacationId, User barber) {
        Vacation vacation = vacationRepository.findByIdAndBarber(vacationId, barber)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono urlopu."));

        vacationRepository.delete(vacation);
    }

    private boolean hasOverlappingVacation(User barber, LocalDate startDate, LocalDate endDate) {
        List<Vacation> vacations = vacationRepository.findByBarberOrderByStartDateAsc(barber);

        for (Vacation vacation : vacations) {
            boolean overlaps = !startDate.isAfter(vacation.getEndDate())
                    && !endDate.isBefore(vacation.getStartDate());

            if (overlaps) {
                return true;
            }
        }

        return false;
    }
}