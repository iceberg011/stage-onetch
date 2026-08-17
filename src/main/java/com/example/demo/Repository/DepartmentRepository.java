package com.example.demo.Repository;

import com.example.demo.Entity.departments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<departments, Long> {
    // No custom methods needed - just use findAll()
    @Query(value = "SELECT COUNT(*) FROM auth_user_auth_dept WHERE department_id = :departmentId", nativeQuery = true)
    int countByDepartmentId(@Param("departmentId") Long departmentId);

    
}