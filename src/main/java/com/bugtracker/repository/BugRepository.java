package com.bugtracker.repository;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Priority;
import com.bugtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {
    
    List<Bug> findByProjectId(Long projectId);
    
    List<Bug> findByCreatedBy(User createdBy);
    
    List<Bug> findByAssignedTo(User assignedTo);
    
    List<Bug> findByStatus(BugStatus status);
    
    List<Bug> findBySeverity(Severity severity);
    
    List<Bug> findByPriority(Priority priority);
    
    @Query("SELECT b FROM Bug b WHERE b.assignedTo.id = :developerId AND b.status IN :statuses")
    List<Bug> findByDeveloperAndStatuses(@Param("developerId") Long developerId, 
                                         @Param("statuses") List<BugStatus> statuses);
    
    @Query("SELECT COUNT(b) FROM Bug b WHERE b.status = :status")
    long countByStatus(@Param("status") BugStatus status);
    
    @Query("SELECT COUNT(b) FROM Bug b WHERE b.severity = :severity")
    long countBySeverity(@Param("severity") Severity severity);
    
    @Query("SELECT COUNT(b) FROM Bug b WHERE b.assignedTo.id = :developerId")
    long countByDeveloper(@Param("developerId") Long developerId);
    
    @Query("SELECT b FROM Bug b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:severity IS NULL OR b.severity = :severity) AND " +
           "(:projectId IS NULL OR b.project.id = :projectId)")
    List<Bug> searchBugs(@Param("title") String title,
                         @Param("status") BugStatus status,
                         @Param("severity") Severity severity,
                         @Param("projectId") Long projectId);
}
