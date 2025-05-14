package com.autohub.user_service.domain.validation;

import com.autohub.user_service.domain.entity.UserDomain;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validator for UserDomain objects ensuring various business rules are met.
 */
@RequiredArgsConstructor
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
    private boolean isValidEmail(String email) {
        return email != null && !email.isBlank() && EMAIL_VALIDATOR.isValid(email);
    }

    /**
     * Validates phone number format
     *
     * @param phone Phone number to validate
     * @return true if phone number is in a valid format
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return true; // Phone is optional
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates password complexity requirements
     *
     * @param password Password to validate
     * @return true if password meets complexity requirements
     */
    private boolean isValidPassword(String password) {
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
    private boolean isValidAge(LocalDate birthDate) {
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
     * Creates a validation error for a field
     *
     * @param field   Field name
     * @param code    Error code
     * @param message Error message
     * @return ValidationError object
     */
    private ValidationError createError(String field, String code, String message) {
        return ValidationError.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Creates a validation error with context
     *
     * @param field   Field name
     * @param code    Error code
     * @param message Error message
     * @param context Context information
     * @return ValidationError object
     */
    private ValidationError createError(String field, String code, String message, Map<String, Object> context) {
        return ValidationError.builder()
                .field(field)
                .code(code)
                .message(message)
                .context(context)
                .build();
    }

    /**
     * Performs comprehensive validation of a user domain object
     *
     * @param userDomain User domain object to validate
     * @return ValidationResult containing validation status and errors
     */
    public ValidationResult validate(UserDomain userDomain) {
        if (userDomain == null) {
            ValidationError error = createError(null, "USER_NULL", "validation.user.null");
            return ValidationResult.invalid(error);
        }

        List<ValidationError> errors = new ArrayList<>();

        // Validate email
        if (!isValidEmail(userDomain.getEmail())) {
            errors.add(createError("email", "EMAIL_INVALID", "validation.email.invalid"));
        }

        // Validate phone if provided
        if (!isValidPhone(userDomain.getPhone())) {
            errors.add(createError("phone", "PHONE_INVALID", "validation.phone.invalid"));
        }

        // Validate birth date if provided
        if (!isValidAge(userDomain.getBirthDate())) {
            errors.add(createError(
                    "birthDate",
                    "AGE_MINIMUM",
                    "validation.age.minimum",
                    Map.of("minimumAge", MINIMUM_AGE, "birthDate", userDomain.getBirthDate())
            ));
        }

        // Validate name fields
        if (userDomain.getFirstName() != null && userDomain.getFirstName().length() > 50) {
            errors.add(createError(
                    "firstName",
                    "FIRSTNAME_LENGTH",
                    "validation.firstname.length",
                    Map.of("maxLength", 50, "actualLength", userDomain.getFirstName().length())
            ));
        }

        if (userDomain.getSecondName() != null && userDomain.getSecondName().length() > 50) {
            errors.add(createError(
                    "secondName",
                    "SECONDNAME_LENGTH",
                    "validation.secondname.length",
                    Map.of("maxLength", 50, "actualLength", userDomain.getSecondName().length())
            ));
        }

        if (userDomain.getLastName() != null && userDomain.getLastName().length() > 50) {
            errors.add(createError(
                    "lastName",
                    "LASTNAME_LENGTH",
                    "validation.lastname.length",
                    Map.of("maxLength", 50, "actualLength", userDomain.getLastName().length())
            ));
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }

    /**
     * Validates a new user creation request
     *
     * @param email    User's email
     * @param password User's password
     * @return ValidationResult containing validation status and errors
     */
    public ValidationResult validateNewUser(String email, String password) {
        List<ValidationError> errors = new ArrayList<>();

        if (!isValidEmail(email)) {
            errors.add(createError("email", "EMAIL_INVALID", "validation.email.invalid"));
        }

        if (!isValidPassword(password)) {
            errors.add(createError("password", "PASSWORD_REQUIREMENTS", "validation.password.requirements"));
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }

    /**
     * Validates a password change request
     *
     * @param currentPassword Current password
     * @param newPassword     New password
     * @param confirmPassword Confirmation of new password
     * @return ValidationResult containing validation status and errors
     */
    public ValidationResult validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        List<ValidationError> errors = new ArrayList<>();

        if (currentPassword == null || currentPassword.isBlank()) {
            errors.add(createError("currentPassword", "CURRENT_PASSWORD_REQUIRED", "validation.password.current.required"));
        }

        if (!isValidPassword(newPassword)) {
            errors.add(createError("newPassword", "PASSWORD_REQUIREMENTS", "validation.password.requirements"));
        }

        if (!newPassword.equals(confirmPassword)) {
            errors.add(createError("confirmPassword", "PASSWORD_MISMATCH", "validation.password.mismatch"));
        }

        if (currentPassword != null && currentPassword.equals(newPassword)) {
            ValidationError warning = ValidationError.builder()
                    .field("newPassword")
                    .code("PASSWORD_SAME")
                    .message("validation.password.different")
                    .severity(ValidationError.Severity.WARNING)
                    .build();
            errors.add(warning);
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }

    /**
     * Validates a profile update request
     *
     * @param firstName  First name (can be null)
     * @param secondName Second name (can be null)
     * @param lastName   Last name (can be null)
     * @param phone      Phone number (can be null)
     * @param birthDate  Birthdate (can be null)
     * @return ValidationResult containing validation status and errors
     */
    public ValidationResult validateProfileUpdate(String firstName, String secondName, String lastName,
                                                  String phone, LocalDate birthDate) {
        List<ValidationError> errors = new ArrayList<>();

        // Validate phone if provided
        if (phone != null && !phone.isBlank() && !isValidPhone(phone)) {
            errors.add(createError("phone", "PHONE_INVALID", "validation.phone.invalid"));
        }

        // Validate birth date if provided
        if (birthDate != null && !isValidAge(birthDate)) {
            errors.add(createError(
                    "birthDate",
                    "AGE_MINIMUM",
                    "validation.age.minimum",
                    Map.of("minimumAge", MINIMUM_AGE, "birthDate", birthDate)
            ));
        }

        // Validate name fields
        if (firstName != null && firstName.length() > 50) {
            errors.add(createError(
                    "firstName",
                    "FIRSTNAME_LENGTH",
                    "validation.firstname.length",
                    Map.of("maxLength", 50, "actualLength", firstName.length())
            ));
        }

        if (secondName != null && secondName.length() > 50) {
            errors.add(createError(
                    "secondName",
                    "SECONDNAME_LENGTH",
                    "validation.secondname.length",
                    Map.of("maxLength", 50, "actualLength", secondName.length())
            ));
        }

        if (lastName != null && lastName.length() > 50) {
            errors.add(createError(
                    "lastName",
                    "LASTNAME_LENGTH",
                    "validation.lastname.length",
                    Map.of("maxLength", 50, "actualLength", lastName.length())
            ));
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }
}
