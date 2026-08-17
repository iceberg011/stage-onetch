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
        return payloadRepository.save(payload);
    }

    @Transactional
    public List<payload> saveAll(List<payload> payloads) {
        return payloadRepository.saveAll(payloads);
    }

    // ===== READ =====
    public Optional<payload> findById(Integer id) {
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

    public List<payload> findByPunchstateTrue() {
        return payloadRepository.findByPunchstateTrue();
    }

    public List<payload> findByWorkcode(String workcode) {
        return payloadRepository.findByWorkcode(workcode);
    }

    public List<payload> findByEmpidAndPunchstateTrue(Integer empid) {
        return payloadRepository.findByEmpidAndPunchstateTrue(empid);
    }

    public List<payload> findByTimecardid(Integer timecardid) {
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
    public void deleteById(Integer id) {
        payloadRepository.deleteById(id);
    }

    @Transactional
    public void deleteByEmpid(Integer empid) {
        List<payload> records = payloadRepository.findByEmpid(empid);
        payloadRepository.deleteAll(records);
    }

    @Transactional
    public void deleteAll() {
        payloadRepository.deleteAll();
    }

    // ===== HELPERS =====
    public boolean existsById(Integer id) {
        return payloadRepository.existsById(id);
    }

    public long countByEmpid(Integer empid) {
        return payloadRepository.findByEmpid(empid).size();
    }

    public long count() {
        return payloadRepository.count();
    }

    public payload getLatestPunch(Integer empid) {
        List<payload> latest = payloadRepository.findLatestByEmpid(empid);
        return latest.isEmpty() ? null : latest.get(0);
    }

    public boolean isPunchedIn(Integer empid) {
        List<payload> todayPunches = payloadRepository.findTodayByEmpid(empid);
        if (todayPunches.isEmpty()) return false;
        payload lastPunch = todayPunches.get(todayPunches.size() - 1);
        return lastPunch.isPunchIn();
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

    // ===== DTO Helper - Get punch with employee details =====
    public PunchWithEmployeeDTO getPunchWithEmployee(Object[] result) {
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
        public Integer getId() { return punch.getId(); }
        public LocalDate getAttdate() { return punch.getAttdate(); }
        public Short getWeek() { return punch.getWeek(); }
        public Short getWeekday() { return punch.getWeekday(); }
        public String getWorkcode() { return punch.getWorkcode(); }
        public boolean isPunchstate() { return punch.isPunchstate(); }
        public LocalDate getPunchdate() { return punch.getPunchdate(); }
        public LocalTime getPunchtime() { return punch.getPunchtime(); }
        public LocalDateTime getPunchdatetime() { return punch.getPunchdatetime(); }
        public String getAdjuststate() { return punch.getAdjuststate(); }
        public Integer getEmpid() { return punch.getEmpid(); }
        public Integer getTransid() { return punch.getTransid(); }
        public Integer getTimecardid() { return punch.getTimecardid(); }
        public String getFormattedPunchDateTime() { return punch.getFormattedPunchDateTime(); }
        public String getWeekdayName() { return punch.getWeekdayName(); }
        public String getPunchType() { 
            return punch.isPunchIn() ? "IN" : (punch.isPunchOut() ? "OUT" : "N/A");
        }
    }
}