package com.teamtacles.teamtacles_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @Schema(description = "The unique username for the user.", example = "john.doe", maxLength = 50, required = true)
    @Size(max = 50, message = "The username must not exceed 50 characters")
    @NotBlank(message = "The name cannot be blank!")
    private String userName;

    @Schema(description = "The unique email address for the user. Must be a valid email format.", example = "john.doe@example.com", maxLength = 250, required = true)
    @Size(max = 250)
    @Email(message = "The email must be valid!")
    private String email;

    @Schema(description = "The user's password. Must be between 5 and 100 characters.", example = "SecurePass123!", minLength = 5, maxLength = 100, required = true)
    @Size(min = 5, max = 100, message = "The password must contain between 5 and 100 characters") 
    @NotBlank(message = "The password cannot be blank!")
    private String password;
    
    @Schema(description = "Confirmation of the user's password. Must match the 'password' field.", example = "SecurePass123!", required = true)
    @NotBlank(message = "The password confirmation cannot be blank!")
    private String passwordConfirm;
}
