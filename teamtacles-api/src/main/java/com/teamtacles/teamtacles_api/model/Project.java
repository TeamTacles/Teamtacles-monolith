package com.teamtacles.teamtacles_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a project within the TeamTacles application.
 * Each project has a unique identifier, a title, an optional description,
 * associated tasks, a designated creator, and a team of participating users.
 *
 * This entity is mapped to a database table to persist project information.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Project{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="The title cannot be blank!")
    @Size(max = 50)
    private String title;

    @Size(max = 50)
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "project-task")
    private List<Task> tasks;

    // Criador do Projeto - Owner
    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonBackReference(value = "user-project")
    private User creator;

    // Usuarios que participam do projeto - Equipe
    @ManyToMany
    @JoinTable(
        name = "team",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "userId")
        )
    private List<User> team;
}