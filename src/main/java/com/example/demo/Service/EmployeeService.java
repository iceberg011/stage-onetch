package com.example.demo.Service;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.SignupRequest;
import com.example.demo.Entity.WorkflowInstance;
import com.example.demo.Entity.employees;
import com.example.demo.Repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.DTO.EmployeeDetailsDTO;
import com.example.demo.DTO.LeaveHistoryDTO;
import com.example.demo.DTO.LeaveWithEmployeeDTO;
import com.example.demo.Entity.employees;
import com.example.demo.Entity.leave;
import com.example.demo.Repository.EmployeesRepository;
import com.example.demo.Repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import com.example.demo.Repository.WorkflowRepository;
import com.example.demo.Repository.LeaveRepository;





@Service
public class EmployeeService implements UserDetailsService {

    @Autowired
    private EmployeesRepository employeesRepository;

     @Autowired
    private WorkflowRepository workflowRepository;
     @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LocationService locationService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ===== USER DETAILS SERVICE =====
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        employees employee = employeesRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        if (employee.getApp_role() != null) {
            switch (employee.getApp_role()) {
                case 1:
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
                case 2:
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
                case 3:
                    authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
                default:
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(
                employee.getEmail(),
                employee.getSelf_password() != null ? employee.getSelf_password() : "",
                employee.isIs_active(),
                true,
                true,
                true,
                authorities
        );
    }

    // ===== REGISTER EMPLOYEE =====
    public employees registerEmployee(SignupRequest request, HttpServletRequest httpRequest) {
        employees employee = new employees();
        
        employee.setFirst_name(request.getFirstName());
        employee.setLast_name(request.getLastName());
        employee.setEmail(request.getEmail().toLowerCase());
        employee.setMobile(request.getPhoneNumber());
        
        String nickname = request.getUsername();
        if (nickname == null || nickname.isEmpty()) {
            nickname = generateNickname(request.getFirstName(), request.getLastName());
        }
        employee.setNickname(nickname.toLowerCase());
        
        employee.setSelf_password(passwordEncoder.encode(request.getPassword()));
        employee.setEmp_code(generateEmployeeCode(request.getFirstName(), request.getLastName()));
        
        try {
            String ip = locationService.getClientIp(httpRequest);
            LocationService.LocationInfo location = locationService.getLocationFromIp(ip);
            
            if (location != null && location.getCity() != null && !location.getCity().isEmpty()) {
                employee.setCity(location.getCity());
                System.out.println("Location detected: " + location.getFullLocation());
            } else {
                employee.setCity("Unknown");
                System.out.println("Location not detected, set to Unknown");
            }
        } catch (Exception e) {
            System.err.println("Error detecting location: " + e.getMessage());
            employee.setCity("Unknown");
        }
        
        employee.setIs_active(true);
        employee.setStatus((short) 1);
        employee.setCreate_time(LocalDateTime.now());
        employee.setUpdate_time(LocalDateTime.now());
        employee.setApp_role((short) 4);
        employee.setPhoto("/static/user/default.png");
        employee.setApp_status("active");
        
        System.out.println("=========================================");
        System.out.println("Saving employee to database...");
        System.out.println("First Name: " + employee.getFirst_name());
        System.out.println("Last Name: " + employee.getLast_name());
        System.out.println("Email: " + employee.getEmail());
        System.out.println("Nickname: " + employee.getNickname());
        System.out.println("Emp Code: " + employee.getEmp_code());
        System.out.println("City: " + employee.getCity());
        System.out.println("Hire Date: " + (employee.getHire_date() == null ? "NULL" : employee.getHire_date()));
        System.out.println("=========================================");
        
        return employeesRepository.save(employee);
    }

    // ===== AUTHENTICATE EMPLOYEE =====
    public Optional<employees> authenticateEmployee(LoginRequest request) {
        Optional<employees> employeeOpt = employeesRepository.findByEmail(request.getEmail().toLowerCase());
        
        if (employeeOpt.isPresent()) {
            employees employee = employeeOpt.get();
            if (!employee.isIs_active()) {
                return Optional.empty();
            }
            if (passwordEncoder.matches(request.getPassword(), employee.getSelf_password())) {
                return employeeOpt;
            }
        }
        return Optional.empty();
    }

    // ===== SEARCH EMPLOYEES =====
   // In EmployeeService.java, update the searchEmployees method:

public List<employees> searchEmployees(String search, String field, String sort, String status, Integer departmentId) {
    List<employees> allEmployees = employeesRepository.findAll();
    
    // Filter by search
    if (search != null && !search.isEmpty()) {
        String searchLower = search.toLowerCase();
        if (field == null || field.equals("all") || field.isEmpty()) {
            allEmployees = allEmployees.stream()
                .filter(emp -> 
                    (emp.getFirst_name() != null && emp.getFirst_name().toLowerCase().contains(searchLower)) ||
                    (emp.getLast_name() != null && emp.getLast_name().toLowerCase().contains(searchLower)) ||
                    (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(searchLower)) ||
                    (emp.getNickname() != null && emp.getNickname().toLowerCase().contains(searchLower)) ||
                    (emp.getEmp_code() != null && emp.getEmp_code().toLowerCase().contains(searchLower)) ||
                    (emp.getMobile() != null && emp.getMobile().toLowerCase().contains(searchLower))
                )
                .collect(Collectors.toList());
        } else {
            switch (field) {
                case "first_name":
                    allEmployees = allEmployees.stream()
                        .filter(emp -> emp.getFirst_name() != null && emp.getFirst_name().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                    break;
                case "last_name":
                    allEmployees = allEmployees.stream()
                        .filter(emp -> emp.getLast_name() != null && emp.getLast_name().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                    break;
                case "email":
                    allEmployees = allEmployees.stream()
                        .filter(emp -> emp.getEmail() != null && emp.getEmail().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                    break;
                case "nickname":
                    allEmployees = allEmployees.stream()
                        .filter(emp -> emp.getNickname() != null && emp.getNickname().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                    break;
                case "emp_code":
                    allEmployees = allEmployees.stream()
                        .filter(emp -> emp.getEmp_code() != null && emp.getEmp_code().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                    break;
                case "mobile":
                    allEmployees = allEmployees.stream()
                        .filter(emp -> emp.getMobile() != null && emp.getMobile().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                    break;
            }
        }
    }
    
    // Filter by status
    if (status != null && !status.equals("all")) {
        boolean isActive = status.equals("active");
        allEmployees = allEmployees.stream()
            .filter(emp -> emp.isIs_active() == isActive)
            .collect(Collectors.toList());
    }
    
    // Filter by department - ADD THIS
    if (departmentId != null && departmentId > 0) {
        allEmployees = allEmployees.stream()
            .filter(emp -> emp.getDepartment_id() != null && emp.getDepartment_id().equals(departmentId))
            .collect(Collectors.toList());
    }
    
    // Sort
    if (sort != null && sort.equals("desc")) {
        allEmployees.sort((a, b) -> b.getFirst_name().compareToIgnoreCase(a.getFirst_name()));
    } else {
        allEmployees.sort((a, b) -> a.getFirst_name().compareToIgnoreCase(b.getFirst_name()));
    }
    
    return allEmployees;
}




    // Add this method to EmployeeService.java
   
    // ===== EXISTENCE CHECKS =====
    public boolean employeeExistsByEmail(String email) {
        return employeesRepository.existsByEmail(email);
    }

    public boolean employeeExistsByNickname(String nickname) {
        return employeesRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return employeesRepository.existsByEmail(email);
    }

    // ===== SAVE EMPLOYEE =====
    public employees saveEmployee(employees employee) {
        employee.setUpdate_time(LocalDateTime.now());
        return employeesRepository.save(employee);
    }

    // ===== COUNT EMPLOYEES =====
    public long countAllEmployees() {
        return employeesRepository.count();
    }

    // ===== FIND BY EMAIL =====
    public Optional<employees> findByEmail(String email) {
        return employeesRepository.findByEmail(email);
    }

    public Optional<employees> findByNickname(String nickname) {
        return employeesRepository.findByNickname(nickname);
    }

    public Optional<employees> getEmployeeByEmail(String email) {
        return employeesRepository.findByEmail(email);
    }

    public Optional<employees> getEmployeeById(Long id) {
        return employeesRepository.findById(id);
    }

    // ===== FIND BY EMPLOYEE CODE =====
    public Optional<employees> findByEmpCode(String empCode) {
        return employeesRepository.findByEmpCode(empCode);
    }

    // ===== FIND BY SESSION KEY =====
    public Optional<employees> findBySessionKey(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<employees> exactMatch = employeesRepository.findBySessionKey(token);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        String prefix = token.contains("_") ? token.substring(0, token.indexOf('_')) : token;
        return employeesRepository.findBySessionKeyStartingWith(prefix);
    }

    // ===== FIND BY USERNAME, EMAIL, OR EMPLOYEE CODE =====
    public Optional<employees> findByUsernameOrEmailOrEmpCode(String username) {
        Optional<employees> employeeOpt = employeesRepository.findByEmail(username.toLowerCase());
        
        if (employeeOpt.isEmpty()) {
            employeeOpt = employeesRepository.findByNickname(username.toLowerCase());
        }
        
        if (employeeOpt.isEmpty()) {
            employeeOpt = employeesRepository.findByEmpCode(username.toUpperCase());
        }
        
        return employeeOpt;
    }

    // ===== GET ACTIVE/INACTIVE EMPLOYEES =====
    public List<employees> getActiveEmployees() {
        return employeesRepository.findByIsActive(true);
    }

    public List<employees> getInactiveEmployees() {
        return employeesRepository.findByIsActive(false);
    }

    // ===== COUNT ACTIVE/INACTIVE EMPLOYEES =====
    public long countActiveEmployees() {
        return employeesRepository.countByIsActive(true);
    }

    public long countInactiveEmployees() {
        return employeesRepository.countByIsActive(false);
    }

    // ===== SAVE SESSION KEY =====
    public void saveSessionKey(Long employeeId, String sessionKey) {
        Optional<employees> employeeOpt = employeesRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            employees employee = employeeOpt.get();
            employee.setSession_key(sessionKey);
            employeesRepository.save(employee);
            System.out.println("Session key saved for employee ID: " + employeeId);
        }
    }

    // ===== VALIDATE PASSWORD =====
    public boolean validatePassword(employees employee, String rawPassword) {
        if (employee == null || employee.getSelf_password() == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, employee.getSelf_password());
    }

    // ===== CLEAR SESSION KEY =====
    public void clearSessionKey(Long employeeId) {
        Optional<employees> employeeOpt = employeesRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            employees employee = employeeOpt.get();
            employee.clearRememberToken();
            employeesRepository.save(employee);
            System.out.println("Session key cleared for employee ID: " + employeeId);
        }
    }

    // ===== GET ALL EMPLOYEES =====
    public List<employees> getAllEmployees() {
        return employeesRepository.findAll();
    }

    // ===== HELPER METHODS =====
    private String generateNickname(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) firstName = "user";
        if (lastName == null || lastName.isEmpty()) lastName = "unknown";
        String firstLetter = firstName.substring(0, 1);
        return (firstLetter + lastName).toLowerCase().replaceAll("\\s+", "");
    }

    private String generateEmployeeCode(String firstName, String lastName) {
        String fName = firstName != null && firstName.length() >= 3 ? 
                       firstName.substring(0, 3).toUpperCase() : "EMP";
        String lName = lastName != null && lastName.length() >= 3 ? 
                       lastName.substring(0, 3).toUpperCase() : "LOY";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return fName + lName + timestamp;
    }





















public EmployeeDetailsDTO getEmployeeDetails(Long employeeId) {
        Optional<employees> employeeOpt = employeesRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            return null;
        }
        
        employees employee = employeeOpt.get();
        
        // Build employee details
        EmployeeDetailsDTO details = new EmployeeDetailsDTO();
        details.setId(employee.getId());
        details.setFullName(employee.getFirst_name() + " " + employee.getLast_name());
        details.setEmail(employee.getEmail());
        details.setEmpCode(employee.getEmp_code());
        details.setMobile(employee.getMobile());
        details.setDepartmentName("Department " + employee.getDepartment_id());
        
        // Get leave history
        List<leave> employeeLeaves = leaveRepository.findByEmployeeId(employeeId);
        List<WorkflowInstance> workflows = workflowRepository.findByEmployeeId(employeeId);
        
        List<LeaveHistoryDTO> leaveHistory = new ArrayList<>();
        for (leave l : employeeLeaves) {
            LeaveHistoryDTO history = new LeaveHistoryDTO();
            history.setLeaveId(l.getWorkflowinstance_ptr_id());
            history.setStartTime(l.getStart_time());
            history.setEndTime(l.getEnd_time());
            history.setLeaveDay(l.getLeave_day());
            history.setApplyReason(l.getApply_reason());
            history.setApplyTime(l.getApply_time());
            
            // Find status from workflow
            WorkflowInstance workflow = workflows.stream()
                    .filter(w -> w.getLeave() != null && 
                                w.getLeave().getWorkflowinstance_ptr_id().equals(l.getWorkflowinstance_ptr_id()))
                    .findFirst()
                    .orElse(null);
            
            if (workflow != null) {
                Short statusCode = workflow.getApprovalStatus();
                String statusName = getStatusName(statusCode);
                history.setStatus(statusName);
            } else {
                history.setStatus("Pending");
            }
            
            leaveHistory.add(history);
        }
        
        details.setLeaveHistory(leaveHistory);
        return details;
    }
    
    private String getStatusName(Short statusCode) {
        if (statusCode == null) return "Pending";
        switch (statusCode) {
            case 0: return "Pending";
            case 1: return "Approved";
            case 2: return "Rejected";
            case 3: return "Cancelled";
            default: return "Unknown";
        }
    }
}