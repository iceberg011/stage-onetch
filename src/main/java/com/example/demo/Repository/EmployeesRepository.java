package com.example.demo.Repository;

import com.example.demo.Entity.employees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeesRepository extends JpaRepository<employees, Long> {
    
    // ===== FIND BY FIELDS =====
    Optional<employees> findByEmail(String email);
    Optional<employees> findByNickname(String nickname);
    Optional<employees> findByMobile(String mobile);
    
    @Query(value = "SELECT * FROM personnel_employee WHERE emp_code = :empCode", nativeQuery = true)
    Optional<employees> findByEmpCode(@Param("empCode") String empCode);
    
    @Query(value = "SELECT * FROM personnel_employee WHERE session_key = :sessionKey", nativeQuery = true)
    Optional<employees> findBySessionKey(@Param("sessionKey") String sessionKey);

    @Query(value = "SELECT * FROM personnel_employee WHERE session_key LIKE CONCAT(:token, '_%')", nativeQuery = true)
    Optional<employees> findBySessionKeyStartingWith(@Param("token") String token);
    
    // ===== EXISTENCE CHECKS =====
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByMobile(String mobile);
    
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM personnel_employee WHERE emp_code = :empCode", nativeQuery = true)
    boolean existsByEmpCode(@Param("empCode") String empCode);
    
    // ===== FIND BY DEPARTMENT =====
    @Query(value = "SELECT * FROM personnel_employee WHERE department_id = :departmentId", nativeQuery = true)
    List<employees> findByDepartmentId(@Param("departmentId") Integer departmentId);
    
    // ===== FIND BY STATUS =====
    @Query(value = "SELECT * FROM personnel_employee WHERE status = :status", nativeQuery = true)
    List<employees> findByStatus(@Param("status") Short status);
    
    // ===== FIND BY ACTIVE STATUS - ADD THESE =====
    @Query(value = "SELECT * FROM personnel_employee WHERE is_active = :isActive", nativeQuery = true)
    List<employees> findByIsActive(@Param("isActive") boolean isActive);
    
    // ===== COUNT BY ACTIVE STATUS - ADD THIS =====
    @Query(value = "SELECT COUNT(*) FROM personnel_employee WHERE is_active = :isActive", nativeQuery = true)
    long countByIsActive(@Param("isActive") boolean isActive);
    
    // ===== SEARCH METHODS =====
    @Query(value = "SELECT * FROM personnel_employee WHERE " +
                   "LOWER(first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "LOWER(last_name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "LOWER(email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "LOWER(nickname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "LOWER(emp_code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "LOWER(mobile) LIKE LOWER(CONCAT('%', :search, '%'))", 
           nativeQuery = true)
    List<employees> searchEmployees(@Param("search") String search);
    
    // ===== FIND BY MULTIPLE CRITERIA (FOR LOGIN) =====
    @Query(value = "SELECT * FROM personnel_employee WHERE " +
                   "LOWER(email) = LOWER(:username) OR " +
                   "LOWER(nickname) = LOWER(:username) OR " +
                   "LOWER(emp_code) = LOWER(:username)", 
           nativeQuery = true)
    Optional<employees> findByEmailOrNicknameOrEmpCode(@Param("username") String username);

    public Optional<employees> getEmployeeById(Long id);

    
}