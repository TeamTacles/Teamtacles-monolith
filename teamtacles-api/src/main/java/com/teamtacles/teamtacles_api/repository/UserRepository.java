package com.teamtacles.teamtacles_api.repository;

import com.teamtacles.teamtacles_api.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing User entities in the TeamTacles application.
 * Extends JpaRepository to provide standard CRUD operations.
 *
 * This interface defines custom query methods to retrieve and verify user information.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-024
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUserNameIgnoreCase(String userName);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
}