package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.frodo.barber.model.User;
import pl.frodo.barber.model.Vacation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacationRepository extends JpaRepository<Vacation, Long> {

    List<Vacation> findByBarberOrderByStartDateAsc(User barber);

    Optional<Vacation> findByIdAndBarber(Long id, User barber);

    boolean existsByBarberAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User barber, LocalDate startDate, LocalDate endDate);

    List<Vacation> findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate today);
}
