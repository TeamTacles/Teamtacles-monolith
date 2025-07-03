package com.teamtacles.teamtacles_api.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.teamtacles.teamtacles_api.model.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequestDTO {
    @Schema(description = "The title of the project.", example = "My Awesome Project", maxLength = 50, required = true)
    @NotBlank(message = "The title must not be blank")
    @Size(max = 50, message = "The title must not exceed 50 characters")
    private String title;

    @Schema(description = "A brief description of the project.", example = "This project aims to develop a new task management system.", maxLength = 50)
    @Size(max = 50, message = "The description must not exceed 50 characters")
    private String description;

    @Schema(description = "A list of user IDs (Long) who are part of the project team. Must contain at least one user.", example = "[1, 2, 3]", type = "array", minContains = 1, required = true)
    @NotEmpty(message = "The team must have at least one user")
    private List<Long> team = new ArrayList<>();
}