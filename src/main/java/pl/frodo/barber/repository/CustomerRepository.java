package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.frodo.barber.model.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneNumber(String phoneNumber);
}
