package pl.frodo.barber.model;

import lombok.Getter;

@Getter
public enum AppointmentStatus {
    PENDING("Oczekuje na potwierdzenie"),
    REJECTED("Odrzucona"),
    BOOKED("Zarezerwowana"),
    DIDNT_SHOW_UP("Nie pojawił się"),
    CANCELLED("Anulowana"),
    DONE("Zrealizowana");

    private String plTranslation;

    AppointmentStatus(String plTranslation) {
        this.plTranslation = plTranslation;
    }
}
