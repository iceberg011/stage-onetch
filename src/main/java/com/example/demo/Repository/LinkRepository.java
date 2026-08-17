package com.example.demo.Repository;

import com.example.demo.Entity.link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LinkRepository extends JpaRepository<link, Integer> {
    
    // Find by user ID
    @Query(value = "SELECT * FROM auth_user_auth_dept WHERE myuser_id = :myuserId", nativeQuery = true)
    List<link> findByMyuser_id(@Param("myuserId") Integer myuserId);
    
    // Find by leave ID
    @Query(value = "SELECT * FROM auth_user_auth_dept WHERE leave_id = :leaveId", nativeQuery = true)
    List<link> findByLeave_id(@Param("leaveId") Integer leaveId);
    
    // Delete by user ID
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM auth_user_auth_dept WHERE myuser_id = :myuserId", nativeQuery = true)
    void deleteByMyuser_id(@Param("myuserId") Integer myuserId);
    
    // Delete by leave ID
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM auth_user_auth_dept WHERE leave_id = :leaveId", nativeQuery = true)
    void deleteByLeave_id(@Param("leaveId") Integer leaveId);
    
    // Find by user ID and leave ID
    @Query(value = "SELECT * FROM auth_user_auth_dept WHERE myuser_id = :myuserId AND leave_id = :leaveId", nativeQuery = true)
    link findByMyuser_idAndLeave_id(@Param("myuserId") Integer myuserId, @Param("leaveId") Integer leaveId);
}