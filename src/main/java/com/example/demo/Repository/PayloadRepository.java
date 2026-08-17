package com.example.demo.Repository;

import com.example.demo.Entity.payload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayloadRepository extends JpaRepository<payload, UUID> {

    // ===== FIND BY EMPLOYEE =====
    List<payload> findByEmpid(Integer empid);

    // ===== FIND BY DATE =====
    List<payload> findByAttdate(LocalDate attdate);

    // ===== FIND BY EMPLOYEE AND DATE =====
    List<payload> findByEmpidAndAttdate(Integer empid, LocalDate attdate);

    // ===== FIND BY EMPLOYEE AND DATE RANGE =====
    @Query("SELECT p FROM payload p WHERE p.empid = :empid AND p.attdate BETWEEN :startDate AND :endDate")
    List<payload> findByEmpidAndDateRange(@Param("empid") Integer empid,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // ===== FIND BY DATE RANGE =====
    @Query("SELECT p FROM payload p WHERE p.attdate BETWEEN :startDate AND :endDate")
    List<payload> findByDateRange(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    // ===== FIND BY PUNCH STATE (String) =====
    List<payload> findByPunchstate(String punchstate);

    // ===== FIND BY WORK CODE =====
    List<payload> findByWorkcode(String workcode);

    // ===== FIND BY EMPLOYEE AND PUNCH STATE =====
    List<payload> findByEmpidAndPunchstate(Integer empid, String punchstate);

    // ===== FIND BY TIME CARD =====
    List<payload> findByTimecardid(UUID timecardid);

    // ===== FIND BY PUNCH DATETIME =====
    List<payload> findByPunchdatetimeBetween(LocalDateTime start, LocalDateTime end);

    // ===== FIND BY WEEK =====
    List<payload> findByWeekAndEmpid(Short week, Integer empid);

    // ===== FIND BY WEEK AND DATE =====
    @Query("SELECT p FROM payload p WHERE p.week = :week AND p.attdate = :attdate")
    List<payload> findByWeekAndAttdate(@Param("week") Short week, @Param("attdate") LocalDate attdate);

    // ===== GET LATEST PAYLOAD FOR EMPLOYEE =====
    @Query("SELECT p FROM payload p WHERE p.empid = :empid ORDER BY p.punchdatetime DESC")
    List<payload> findLatestByEmpid(@Param("empid") Integer empid);

    // ===== GET TODAY'S PAYLOADS FOR EMPLOYEE =====
    @Query("SELECT p FROM payload p WHERE p.empid = :empid AND p.attdate = CURRENT_DATE")
    List<payload> findTodayByEmpid(@Param("empid") Integer empid);

    // ===== GET ALL PUNCHES WITH EMPLOYEE NAMES =====
    @Query("SELECT p, e FROM payload p JOIN employees e ON p.empid = e.id")
    List<Object[]> findAllWithEmployee();

    // ===== GET PUNCHES WITH EMPLOYEE NAMES BY DATE RANGE =====
    @Query("SELECT p, e FROM payload p JOIN employees e ON p.empid = e.id WHERE p.attdate BETWEEN :startDate AND :endDate")
    List<Object[]> findAllWithEmployeeByDateRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // ===== GET PUNCHES WITH EMPLOYEE NAMES BY EMPLOYEE =====
    @Query("SELECT p, e FROM payload p JOIN employees e ON p.empid = e.id WHERE p.empid = :empid")
    List<Object[]> findAllWithEmployeeByEmpid(@Param("empid") Integer empid);

    // ===== SEARCH PUNCHES WITH EMPLOYEE NAMES =====
    @Query("SELECT p, e FROM payload p JOIN employees e ON p.empid = e.id " +
            "WHERE LOWER(e.first_name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.last_name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Object[]> searchWithEmployee(@Param("search") String search);
}