package com.teamtacles.teamtacles_api.dto.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDTO {
    @Schema(description = "The username of the user.", example = "john.doe", required = true)
    @NotBlank(message = "Enter your Username")
    private String userName;

    @Schema(description = "The password of the user.", example = "mySecurePassword123", required = true)
    @NotBlank(message = "Enter your password")
    private String password;
}
