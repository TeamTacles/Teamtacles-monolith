package com.teamtacles.teamtacles_api.repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.teamtacles.teamtacles_api.model.Project;
import com.teamtacles.teamtacles_api.model.User;
import com.teamtacles.teamtacles_api.model.enums.Status;

/**
 * Repository interface for managing {@link Project} entities in the TeamTacles application.
 * Extends {@link JpaRepository} to provide standard CRUD operations and pagination capabilities.
 *
 * This interface defines custom query methods to retrieve projects based on various criteria.
 *
 * @author TeamTacles 
 * @version 1.0
 * @since 2025-05-22
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    Page<Project> findByCreator(User creator, Pageable pageable);
    Page<Project> findById(Long id, Pageable pageable);
    Page<Project> findByTeam(User user, Pageable pageable);
}