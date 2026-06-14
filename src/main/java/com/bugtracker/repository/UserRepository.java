package com.bugtracker.repository;

import com.bugtracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.role = 'DEVELOPER'")
    java.util.List<User> findAllDevelopers();
    
    @Query("SELECT u FROM User u WHERE u.role = 'TESTER'")
    java.util.List<User> findAllTesters();
}
