package pl.frodo.barber.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "vacations")
public class Vacation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "barber_id", nullable = false)
    private User barber;

    @Column(name = "start_time", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_time", nullable = false)
    private LocalDate endDate;

}
