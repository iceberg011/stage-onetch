package com.example.demo.Service;

import com.example.demo.Entity.payload;
import com.example.demo.Entity.employees;
import com.example.demo.Repository.PayloadRepository;
import com.example.demo.Repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalTime;

@Service
public class PayloadService {

    @Autowired
    private PayloadRepository payloadRepository;

    @Autowired
    private EmployeesRepository employeeRepository;

    // ===== CREATE / UPDATE =====
    @Transactional
    public payload save(payload payload) {
        if (payload.getId() == null) {
            payload.setId(UUID.randomUUID());
        }
        return payloadRepository.save(payload);
    }

    @Transactional
    public List<payload> saveAll(List<payload> payloads) {
        for (payload p : payloads) {
            if (p.getId() == null) {
                p.setId(UUID.randomUUID());
            }
        }
        return payloadRepository.saveAll(payloads);
    }

    // ===== READ =====
    public Optional<payload> findById(UUID id) {
        return payloadRepository.findById(id);
    }

    public List<payload> findAll() {
        return payloadRepository.findAll();
    }

    public List<payload> findByEmpid(Integer empid) {
        return payloadRepository.findByEmpid(empid);
    }

    public List<payload> findByAttdate(LocalDate attdate) {
        return payloadRepository.findByAttdate(attdate);
    }

    public List<payload> findByEmpidAndAttdate(Integer empid, LocalDate attdate) {
        return payloadRepository.findByEmpidAndAttdate(empid, attdate);
    }

    public List<payload> findByEmpidAndDateRange(Integer empid, LocalDate startDate, LocalDate endDate) {
        return payloadRepository.findByEmpidAndDateRange(empid, startDate, endDate);
    }

    public List<payload> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return payloadRepository.findByDateRange(startDate, endDate);
    }

    public List<payload> findByPunchstate(String punchstate) {
        return payloadRepository.findByPunchstate(punchstate);
    }

    public List<payload> findByWorkcode(String workcode) {
        return payloadRepository.findByWorkcode(workcode);
    }

    public List<payload> findByEmpidAndPunchstate(Integer empid, String punchstate) {
        return payloadRepository.findByEmpidAndPunchstate(empid, punchstate);
    }

    public List<payload> findByTimecardid(UUID timecardid) {
        return payloadRepository.findByTimecardid(timecardid);
    }

    public List<payload> findByPunchdatetimeBetween(LocalDateTime start, LocalDateTime end) {
        return payloadRepository.findByPunchdatetimeBetween(start, end);
    }

    public List<payload> findLatestByEmpid(Integer empid) {
        return payloadRepository.findLatestByEmpid(empid);
    }

    public List<payload> findTodayByEmpid(Integer empid) {
        return payloadRepository.findTodayByEmpid(empid);
    }

    // ===== DELETE =====
    @Transactional
    public void deleteById(UUID id) {
        payloadRepository.deleteById(id);
    }

    @Transactional
    public void deleteByEmpid(Integer empid) {
        List<payload> records = payloadRepository.findByEmpid(empid);
        if (records != null && !records.isEmpty()) {
            payloadRepository.deleteAll(records);
        }
    }

    @Transactional
    public void deleteAll() {
        payloadRepository.deleteAll();
    }

    // ===== HELPERS =====
    public boolean existsById(UUID id) {
        return payloadRepository.existsById(id);
    }

    public long countByEmpid(Integer empid) {
        List<payload> records = payloadRepository.findByEmpid(empid);
        return records != null ? records.size() : 0;
    }

    public long count() {
        return payloadRepository.count();
    }

    public payload getLatestPunch(Integer empid) {
        List<payload> latest = payloadRepository.findLatestByEmpid(empid);
        return (latest != null && !latest.isEmpty()) ? latest.get(0) : null;
    }

    public boolean isPunchedIn(Integer empid) {
        List<payload> todayPunches = payloadRepository.findTodayByEmpid(empid);
        if (todayPunches == null || todayPunches.isEmpty()) return false;
        payload lastPunch = todayPunches.get(todayPunches.size() - 1);
        return lastPunch != null && lastPunch.isPunchIn();
    }

    // ===== GET PUNCHES WITH EMPLOYEE DATA =====
    public List<Object[]> findAllWithEmployee() {
        return payloadRepository.findAllWithEmployee();
    }

    public List<Object[]> findAllWithEmployeeByDateRange(LocalDate startDate, LocalDate endDate) {
        return payloadRepository.findAllWithEmployeeByDateRange(startDate, endDate);
    }

    public List<Object[]> findAllWithEmployeeByEmpid(Integer empid) {
        return payloadRepository.findAllWithEmployeeByEmpid(empid);
    }

    public List<Object[]> searchWithEmployee(String search) {
        return payloadRepository.searchWithEmployee(search);
    }

    // ===== DTO Helper =====
    public PunchWithEmployeeDTO getPunchWithEmployee(Object[] result) {
        if (result == null || result.length < 2) {
            return null;
        }
        payload punch = (payload) result[0];
        employees employee = (employees) result[1];
        return new PunchWithEmployeeDTO(punch, employee);
    }

    // ===== DTO Class =====
    public static class PunchWithEmployeeDTO {
        private payload punch;
        private employees employee;
        private String employeeName;
        private String employeeCode;

        public PunchWithEmployeeDTO(payload punch, employees employee) {
            this.punch = punch;
            this.employee = employee;
            this.employeeName = employee.getFirst_name() + " " + employee.getLast_name();
            this.employeeCode = employee.getEmp_code();
        }

        public payload getPunch() { return punch; }
        public employees getEmployee() { return employee; }
        public String getEmployeeName() { return employeeName; }
        public String getEmployeeCode() { return employeeCode; }
        public UUID getId() { return punch != null ? punch.getId() : null; }
        public LocalDate getAttdate() { return punch != null ? punch.getAttdate() : null; }
        public Short getWeek() { return punch != null ? punch.getWeek() : null; }
        public Short getWeekday() { return punch != null ? punch.getWeekday() : null; }
        public String getWorkcode() { return punch != null ? punch.getWorkcode() : null; }

        // FIXED: Returns the punchstate string directly
        public String getPunchstate() {
            return punch != null ? punch.getPunchstate() : null;
        }

        public LocalDate getPunchdate() { return punch != null ? punch.getPunchdate() : null; }
        public LocalTime getPunchtime() { return punch != null ? punch.getPunchtime() : null; }
        public LocalDateTime getPunchdatetime() { return punch != null ? punch.getPunchdatetime() : null; }
        public String getAdjuststate() { return punch != null ? punch.getAdjuststate() : null; }
        public Integer getEmpid() { return punch != null ? punch.getEmpid() : null; }
        public Integer getTransid() { return punch != null ? punch.getTransid() : null; }
        public UUID getTimecardid() { return punch != null ? punch.getTimecardid() : null; }

        public String getFormattedPunchDateTime() {
            return punch != null ? punch.getFormattedPunchDateTime() : "N/A";
        }

        public String getWeekdayName() {
            return punch != null ? punch.getWeekdayName() : "N/A";
        }

        // FIXED: Uses the punch.getPunchType() method
        public String getPunchType() {
            if (punch == null) return "N/A";
            return punch.getPunchType();
        }
    }
}