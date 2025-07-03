package com.teamtacles.teamtacles_api.dto.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {
    @Schema(description = "The desired unique username for the new user. Must be between 3 and 50 characters long.", example = "jane.doe", minLength = 3, maxLength = 50, required = true)
    @Size(min = 3, max = 50, message = "The username must be between 3 and 50 characters long")
    @NotBlank(message = "Username must not be blank")
    private String userName;

    @Schema(description = "The user's email address. Must be a valid email format and between 8 and 50 characters long.", example = "jane.doe@example.com", minLength = 8, maxLength = 50, required = true)
    @Email(message = "Email must be valid")
    @Size(min = 8, max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @Schema(description = "The user's password. Must be between 5 and 100 characters long.", example = "StrongPassword123!", minLength = 5, maxLength = 100, required = true)
    @Size(min = 5, max = 100, message = "Password must be between 5 and 100 characters long") 
    @NotBlank(message = "Password must not be blank")
    private String password;

    @Schema(description = "Confirmation of the user's password. Must match the 'password' field exactly.", example = "StrongPassword123!", required = true)
    @NotBlank(message = "Password confirmation must not be blank")
    private String passwordConfirm;
}
