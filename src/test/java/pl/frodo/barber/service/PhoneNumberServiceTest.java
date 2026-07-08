package pl.frodo.barber.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhoneNumberServiceTest {

    private final PhoneNumberService phoneNumberService = new PhoneNumberService();

    @ParameterizedTest
    @ValueSource(strings = {
            "123456789",
            "123 456 789",
            "48123456789",
            "+48123456789",
            "+48 123 456 789"
    })
    void normalize_returnsE164_forVariousValidFormats(String input) {
        assertThat(phoneNumberService.normalize(input))
                .isEqualTo("+48123456789");
    }

    @Test
    void normalize_throws_whenNumberIsInvalid() {
        assertThatThrownBy(() -> phoneNumberService.normalize("111111111"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nieprawidłowy numer");
    }

    @Test
    void normalize_throws_whenInputIsNotParseable() {
        assertThatThrownBy(() -> phoneNumberService.normalize("abcdef"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isValid_returnsTrue_forValidNumber() {
        assertThat(phoneNumberService.isValid("+48 123 456 789")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"111111111", "12345", "abcdef"})
    void isValid_returnsFalse_forInvalidInput(String input) {
        assertThat(phoneNumberService.isValid(input)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void isValid_returnsFalse_forNullOrBlank(String input) {
        assertThat(phoneNumberService.isValid(input)).isFalse();
    }
}
