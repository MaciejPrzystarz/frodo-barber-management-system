package pl.frodo.barber.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberService {

    private static final String DEFAULT_REGION = "PL";

    private final PhoneNumberUtil phone = PhoneNumberUtil.getInstance();

    public boolean isValid(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        try {
            Phonenumber.PhoneNumber parsed = phone.parse(raw, DEFAULT_REGION);
            return phone.isValidNumber(parsed);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public String normalize(String raw) {
        try {
            Phonenumber.PhoneNumber parsed = phone.parse(raw, DEFAULT_REGION);
            if (!phone.isValidNumber(parsed)) {
                throw new IllegalArgumentException("Nieprawidłowy numer telefonu.");
            }
            return phone.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Nieprawidłowy numer telefonu.");
        }
    }
}
