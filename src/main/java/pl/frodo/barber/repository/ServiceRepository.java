package pl.frodo.barber.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.frodo.barber.model.ServiceItem;

public interface ServiceRepository extends JpaRepository<ServiceItem, Long> {

}
