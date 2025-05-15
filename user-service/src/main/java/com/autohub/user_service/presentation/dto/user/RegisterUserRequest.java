package com.autohub.user_service.presentation.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for user registration requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotBlank(message = "validation.email.required")
    @Email(message = "validation.email.invalid")
    private String email;

    @NotBlank(message = "validation.password.current.required")
    @Size(min = 8, max = 100, message = "validation.password.requirements")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "validation.password.weak"
    )
    private String password;

    @NotBlank(message = "validation.firstname.required")
    @Size(max = 50, message = "validation.firstname.length")
    private String firstName;

    @Size(max = 50, message = "validation.secondname.length")
    private String secondName;

    @NotBlank(message = "validation.lastname.required")
    @Size(max = 50, message = "validation.lastname.length")
    private String lastName;

    @Size(max = 15, message = "validation.phone.length")
    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "validation.phone.invalid")
    private String phone;

    private LocalDate birthDate;
}

