package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.frodo.barber.model.Role;
import pl.frodo.barber.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findFirstByRole(Role role);

}
