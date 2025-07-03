package com.teamtacles.teamtacles_api.model;

import com.teamtacles.teamtacles_api.model.enums.ERole;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;

/**
 * Represents a user role in the TeamTacles application, defining distinct levels
 * of permissions and access within the system. This entity maps to the 'roles'
 * database table.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false, unique = true, name="role_name") 
    private ERole roleName;
}
