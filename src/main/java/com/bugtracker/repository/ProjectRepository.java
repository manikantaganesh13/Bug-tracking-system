package com.bugtracker.repository;

import com.bugtracker.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    boolean existsByName(String name);
    
    @Query("SELECT p FROM Project p ORDER BY p.createdDate DESC")
    List<Project> findAllOrderByCreatedDateDesc();
    
    @Query("SELECT COUNT(b) FROM Bug b WHERE b.project.id = :projectId")
    long countBugsByProjectId(Long projectId);
}
