package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.frodo.barber.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
