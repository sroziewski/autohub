package com.autohub.user_service.domain.validation;

import com.autohub.user_service.domain.entity.UserDomain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validator for UserDomain objects ensuring various business rules are met.
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Component
public class UserValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+[0-9]{1,4})?[0-9]{6,14}$");

    // Minimum required age for users
    private static final int MINIMUM_AGE = 18;

    // Email validator from Apache Commons
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance(false, false);

    private final MessageSource messageSource;

    /**
     * Gets a localized message from the message source
     *
     * @param key  Message key
     * @param args Message arguments
     * @return Localized message
     */
    private String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, locale);
    }

    /**
     * Validates user email
     *
     * @param email Email to validate
     * @return true if email is valid
     */
    public boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && EMAIL_VALIDATOR.isValid(email);
    }

    /**
     * Validates phone number format
     *
     * @param phone Phone number to validate
     * @return true if phone number is in a valid format
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return true; // Phone is optional
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        
        return hasDigit && hasLower && hasUpper && hasSpecial;
    }

    /**
     * Checks if a user's age meets the minimum age requirement
     *
     * @param birthDate Birth date to check
     * @return true if the user is at least the minimum required age
     */
    public boolean isValidAge(LocalDate birthDate) {
        if (birthDate == null) {
            return true; // Birth date is optional
        }

        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthDate.getYear();

        // Adjust age if the birthday hasn't occurred yet this year
        if (now.getMonthValue() < birthDate.getMonthValue() ||
                (now.getMonthValue() == birthDate.getMonthValue() && now.getDayOfMonth() < birthDate.getDayOfMonth())) {
            age--;
        }

        return age >= MINIMUM_AGE;
    }

    /**
     * Performs comprehensive validation of a user domain object
     *
     * @param userDomain User domain object to validate
     * @return List of validation errors, empty if no errors
     */
    public List<String> validate(UserDomain userDomain) {
        List<String> errors = new ArrayList<>();

        if (userDomain == null) {
            errors.add(getMessage("validation.user.null"));
            return errors;
        }

        // Validate email
        if (!isValidEmail(userDomain.getEmail())) {
            errors.add(getMessage("validation.email.invalid"));
        }

        // Validate phone if provided
        if (!isValidPhone(userDomain.getPhone())) {
            errors.add(getMessage("validation.phone.invalid"));
        }

        // Validate birth date if provided
        if (!isValidAge(userDomain.getBirthDate())) {
            errors.add(getMessage("validation.age.minimum", MINIMUM_AGE));
        }

        // Validate name fields
        if (userDomain.getFirstName() != null && userDomain.getFirstName().length() > 50) {
            errors.add(getMessage("validation.firstname.length"));
        }

        if (userDomain.getSecondName() != null && userDomain.getSecondName().length() > 50) {
            errors.add(getMessage("validation.secondname.length"));
        }

        if (userDomain.getLastName() != null && userDomain.getLastName().length() > 50) {
            errors.add(getMessage("validation.lastname.length"));
        }

        return errors;
    }

    /**
     * Validates a new user creation request
     *
     * @param email    User's email
     * @param password User's password
     * @return List of validation errors, empty if no errors
     */
    public List<String> validateNewUser(String email, String password) {
        List<String> errors = new ArrayList<>();

        if (!isValidEmail(email)) {
            errors.add(getMessage("validation.email.invalid"));
        }

        if (!isValidPassword(password)) {
            errors.add(getMessage("validation.password.requirements"));
        }

        return errors;
    }

    /**
     * Validates a password change request
     *
     * @param currentPassword Current password
     * @param newPassword     New password
     * @param confirmPassword Confirmation of new password
     * @return List of validation errors, empty if no errors
     */
    public List<String> validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        List<String> errors = new ArrayList<>();

        if (currentPassword == null || currentPassword.isBlank()) {
            errors.add(getMessage("validation.password.current.required"));
        }

        if (!isValidPassword(newPassword)) {
            errors.add(getMessage("validation.password.requirements"));
        }

        if (!newPassword.equals(confirmPassword)) {
            errors.add(getMessage("validation.password.mismatch"));
        }

        if (currentPassword != null && currentPassword.equals(newPassword)) {
            errors.add(getMessage("validation.password.different"));
        }

        return errors;
    }

    /**
     * Validates a profile update request
     *
     * @param firstName  First name (can be null)
     * @param secondName Second name (can be null)
     * @param lastName   Last name (can be null)
     * @param phone      Phone number (can be null)
     * @param birthDate  Birth date (can be null)
     * @return List of validation errors, empty if no errors
     */
    public List<String> validateProfileUpdate(String firstName, String secondName, String lastName,
                                              String phone, LocalDate birthDate) {
        List<String> errors = new ArrayList<>();

        // Validate phone if provided
        if (phone != null && !phone.isBlank() && !isValidPhone(phone)) {
            errors.add(getMessage("validation.phone.invalid"));
        }

        // Validate birth date if provided
        if (birthDate != null && !isValidAge(birthDate)) {
            errors.add(getMessage("validation.age.minimum", MINIMUM_AGE));
        }

        // Validate name fields
        if (firstName != null && firstName.length() > 50) {
            errors.add(getMessage("validation.firstname.length"));
        }

        if (secondName != null && secondName.length() > 50) {
            errors.add(getMessage("validation.secondname.length"));
        }

        if (lastName != null && lastName.length() > 50) {
            errors.add(getMessage("validation.lastname.length"));
        }

        return errors;
    }
}
