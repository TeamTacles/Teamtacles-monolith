package com.teamtacles.teamtacles_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamtacles.teamtacles_api.model.enums.ERole;
import com.teamtacles.teamtacles_api.model.Role;
import java.util.Optional;

/**
 * Repository interface for managing {@link Role} entities in the TeamTacles application.
 * Extends {@link JpaRepository} to provide standard CRUD operations.
 *
 * This interface defines custom query methods to retrieve roles based on specific criteria.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-22
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(ERole roleName);
}