package com.example.demo.Controller;

import com.example.demo.Entity.employees;
import com.example.demo.Entity.departments;
import com.example.demo.Entity.link;
import com.example.demo.Repository.EmployeesRepository;
import com.example.demo.Repository.DepartmentRepository;
import com.example.demo.Repository.LinkRepository;
import com.example.demo.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class EmployeesController {

    @Autowired
    private EmployeesRepository employeesRepository;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private LinkRepository linkRepository;

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    // ===== LIST ALL EMPLOYEES WITH SEARCH & FILTER =====
    @GetMapping("/dashboard/Employees")
    public String listEmployees(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "field", required = false) String field,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "department", required = false) Integer departmentId,
            HttpSession session,
            Model model) {
        
        System.out.println("=== GET /dashboard/Employees called ===");
        System.out.println("Search: " + search);
        System.out.println("Field: " + field);
        System.out.println("Sort: " + sort);
        System.out.println("Status: " + status);
        System.out.println("Department ID: " + departmentId);
        
        employees currentEmployee = null;
        
        // First try to get from session
        currentEmployee = (employees) session.getAttribute("employee");
        
        // If not in session, try to get from SecurityContext
        if (currentEmployee == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                String email = auth.getName();
                System.out.println("Getting user from SecurityContext: " + email);
                Optional<employees> employeeOpt = employeeService.findByEmail(email);
                if (employeeOpt.isPresent()) {
                    currentEmployee = employeeOpt.get();
                    session.setAttribute("employee", currentEmployee);
                    System.out.println("User loaded from SecurityContext and saved to session: " + currentEmployee.getEmail());
                }
            }
        }
        
        if (currentEmployee == null) {
            System.out.println("Employee not logged in, redirecting to signin");
            return "redirect:/signin";
        }
        
        System.out.println("Current employee: " + currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name());
        
        // Get all employees with search/filter
        List<employees> filteredEmployees = employeeService.searchEmployees(search, field, sort, status, departmentId);
        
        // Calculate statistics
        long totalEmployees = filteredEmployees.size();
        long activeEmployees = 0;
        long inactiveEmployees = 0;
        long adminCount = 0;
        
        for (employees emp : filteredEmployees) {
            if (emp.isIs_active()) {
                activeEmployees++;
            } else {
                inactiveEmployees++;
            }
            if (emp.getApp_role() != null && emp.getApp_role() == 1) {
                adminCount++;
            }
        }
        
        // Get all departments for the dropdown
        List<departments> allDepartments = departmentRepository.findAll();
        
        // Set layout attributes
        model.addAttribute("pageTitle", "Employees");
        model.addAttribute("pageContent", "User/Users");
        
        // Employee info for sidebar
        model.addAttribute("employee", currentEmployee);
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        
        // Employee statistics
        model.addAttribute("employeeCount", totalEmployees);
        model.addAttribute("activeEmployees", activeEmployees);
        model.addAttribute("inactiveEmployees", inactiveEmployees);
        model.addAttribute("adminCount", adminCount);
        
        // ALL employees for the table
        model.addAttribute("employees", filteredEmployees);
        model.addAttribute("departments", allDepartments);
        
        // Preserve filter values for the form
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedField", field);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDepartment", departmentId);
        
        return "Components/layout";
    }

    // ===== SHOW CREATE EMPLOYEE FORM =====
    @GetMapping("/dashboard/employees/CreateEmployee")
    public String showCreateEmployeeForm(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/employees/CreateEmployee called ===");
        
        try {
            employees currentEmployee = (employees) session.getAttribute("employee");
            
            if (currentEmployee == null) {
                System.out.println("Employee not logged in, redirecting to signin");
                return "redirect:/signin";
            }
            
            System.out.println("Current employee: " + currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name());
            
            // Get all departments to display in dropdown
            List<departments> departments = departmentRepository.findAll();
            System.out.println("Found " + departments.size() + " departments");
            
            model.addAttribute("departments", departments);
            model.addAttribute("pageTitle", "Add New Employee");
            model.addAttribute("pageContent", "User/Newuser");
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            
            return "Components/layout";
            
        } catch (Exception e) {
            System.err.println("ERROR in showCreateEmployeeForm: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading page: " + e.getMessage());
            return "Components/layout";
        }
    }

    // ===== CREATE EMPLOYEE (POST) WITH FILE UPLOADS =====
    @PostMapping("/dashboard/employees/CreateEmployee")
    public String createEmployee(
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "birthday", required = false) String birthday,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "national", required = false) String national,
            @RequestParam(value = "religion", required = false) String religion,
            @RequestParam(value = "office_tel", required = false) String officeTel,
            @RequestParam(value = "contact_tel", required = false) String contactTel,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", defaultValue = "1") Short status,
            @RequestParam(value = "is_active", defaultValue = "true") String isActive,
            @RequestParam(value = "department_id", required = false) Integer departmentId,
            @RequestParam(value = "position_id", required = false) Integer positionId,
            @RequestParam(value = "app_role", required = false) Short appRole,
            @RequestParam(value = "enable_payroll", defaultValue = "true") boolean enablePayroll,
            @RequestParam(value = "self_password", required = false) String selfPassword,
            @RequestParam(value = "device_password", required = false) String devicePassword,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile,
            @RequestParam(value = "driver_license_automobile", required = false) MultipartFile driverLicenseAutoFile,
            @RequestParam(value = "driver_license_motorcycle", required = false) MultipartFile driverLicenseMotorFile,

            HttpSession session,
            Model model) {

        System.out.println("=== POST /dashboard/employees/CreateEmployee called ===");
        System.out.println("Department ID received: " + departmentId);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Email: " + email);
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        if (currentEmployee == null) {
            return "redirect:/signin";
        }

        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            model.addAttribute("error", "First name is required");
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("pageTitle", "Add New Employee");
            model.addAttribute("pageContent", "User/Newuser");
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            return "Components/layout";
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            model.addAttribute("error", "Last name is required");
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("pageTitle", "Add New Employee");
            model.addAttribute("pageContent", "User/Newuser");
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            return "Components/layout";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("pageTitle", "Add New Employee");
            model.addAttribute("pageContent", "User/Newuser");
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            return "Components/layout";
        }

        // Check if email exists
        if (employeeService.employeeExistsByEmail(email)) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("pageTitle", "Add New Employee");
            model.addAttribute("pageContent", "User/Newuser");
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            return "Components/layout";
        }

        // Generate nickname if not provided
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = generateNickname(firstName, lastName);
        }

        // Check if nickname exists
        if (employeeService.employeeExistsByNickname(nickname)) {
            int counter = 1;
            String baseNickname = nickname;
            while (employeeService.employeeExistsByNickname(nickname)) {
                nickname = baseNickname + counter;
                counter++;
            }
            System.out.println("Generated unique nickname: " + nickname);
        }

        // Generate employee code
        String empCode = generateEmployeeCode(firstName, lastName);

        try {
            // Create new employee
            employees newEmployee = new employees();
            newEmployee.setFirst_name(firstName.trim());
            newEmployee.setLast_name(lastName.trim());
            newEmployee.setEmail(email.trim().toLowerCase());
            newEmployee.setNickname(nickname.toLowerCase());
            newEmployee.setEmp_code(empCode);
            newEmployee.setMobile(mobile);
            newEmployee.setCity(city);
            newEmployee.setNational(national);
            newEmployee.setReligion(religion);
            newEmployee.setOffice_tel(officeTel);
            newEmployee.setContact_tel(contactTel);
            newEmployee.setTitle(title);
            newEmployee.setStatus(status);
            newEmployee.setIs_active("true".equalsIgnoreCase(isActive));
            newEmployee.setEnable_payroll(enablePayroll);
            newEmployee.setCreate_time(LocalDateTime.now());
            newEmployee.setUpdate_time(LocalDateTime.now());
            newEmployee.setCreate_user(currentEmployee.getFirst_name() + " " + currentEmployee.getLast_name());
    
            
            // Set password if provided
            if (selfPassword != null && !selfPassword.isEmpty()) {
                newEmployee.setSelf_password(selfPassword);
            }
            
            // Set device password
            if (devicePassword != null && !devicePassword.isEmpty()) {
                newEmployee.setDevice_password(devicePassword);
            }
            
            // Set default app role if not provided
            if (appRole == null) {
                newEmployee.setApp_role((short) 4);
            } else {
                newEmployee.setApp_role(appRole);
            }
            
            // Set gender
            if (gender != null && !gender.isEmpty()) {
                newEmployee.setGender(gender);
            }
            
            // Set birthday if provided
            if (birthday != null && !birthday.isEmpty()) {
                try {
                    newEmployee.setBirthday(LocalDate.parse(birthday));
                } catch (Exception e) {
                    System.out.println("Invalid birthday format: " + birthday);
                }
            }
            
            // Handle file uploads
            String photoFileName = saveFile(photoFile, "photo");
            if (photoFileName != null) {
                newEmployee.setPhoto(photoFileName);
            }
            
            String driverLicenseAutoFileName = saveFile(driverLicenseAutoFile, "license_auto");
            if (driverLicenseAutoFileName != null) {
                newEmployee.setDriver_license_automobile(driverLicenseAutoFileName);
            }
            
            String driverLicenseMotorFileName = saveFile(driverLicenseMotorFile, "license_motor");
            if (driverLicenseMotorFileName != null) {
                newEmployee.setDriver_license_motorcycle(driverLicenseMotorFileName);
            }
            
            // Set department
            if (departmentId != null && departmentId > 0) {
                Optional<departments> deptOpt = departmentRepository.findById(departmentId.longValue());
                if (deptOpt.isPresent()) {
                    newEmployee.setDepartment_id(departmentId);
                    System.out.println("Employee assigned to department ID: " + departmentId);
                }
            }
            
            // Set position
            if (positionId != null && positionId > 0) {
                newEmployee.setPosition_id(positionId);
            }

            // Save employee
            employeeService.saveEmployee(newEmployee);
            System.out.println("Employee created successfully with ID: " + newEmployee.getId() + 
                             ", Name: " + newEmployee.getFirst_name() + " " + newEmployee.getLast_name());
            
            // Handle department link if needed
            if (departmentId != null && departmentId > 0) {
                link newLink = new link();
                newLink.setmyuser_id(Math.toIntExact(newEmployee.getId()));
                newLink.setdepartment_id(departmentId);
                linkRepository.save(newLink);
                System.out.println("Department link created for employee ID: " + newEmployee.getId());
            }
            
            return "redirect:/dashboard/Employees?success=EmployeeCreated";
            
        } catch (Exception e) {
            System.err.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Error creating employee: " + e.getMessage());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("pageTitle", "Add New Employee");
            model.addAttribute("pageContent", "User/Newuser");
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            return "Components/layout";
        }
    }

    // ===== EDIT EMPLOYEE (GET) =====
    @GetMapping("/dashboard/employees/edit/{id}")
    public String editEmployee(
            @PathVariable Long id,
            HttpSession session,
            Model model) {
        
        System.out.println("=== GET /dashboard/employees/edit/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        Optional<employees> employeeOpt = employeesRepository.findById(id);
        
        if (employeeOpt.isEmpty()) {
            System.out.println("Employee not found with ID: " + id);
            return "redirect:/dashboard/Employees?error=EmployeeNotFound";
        }
        
        employees employeeToEdit = employeeOpt.get();
        
        List<departments> departments = departmentRepository.findAll();
        model.addAttribute("departments", departments);
        
        Integer deptId = employeeToEdit.getDepartment_id();
        model.addAttribute("employeeDepartmentId", deptId);
        
        System.out.println("\n--- EMPLOYEE DEPARTMENT INFO ---");
        System.out.println("Employee: " + employeeToEdit.getFirst_name() + " " + employeeToEdit.getLast_name() + 
                          " (ID: " + employeeToEdit.getId() + ")");
        if (deptId != null && deptId > 0) {
            Optional<departments> deptOpt = departmentRepository.findById(deptId.longValue());
            if (deptOpt.isPresent()) {
                departments dept = deptOpt.get();
                System.out.println("Currently assigned to: " + dept.getdept_name() + " (ID: " + deptId + ")");
            }
        } else {
            System.out.println("Currently assigned to: No Department");
        }
        System.out.println("==============================\n");
        
        model.addAttribute("employee", employeeToEdit);
        model.addAttribute("pageTitle", "Edit Employee");
        model.addAttribute("pageContent", "User/EditUser");
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        
        return "Components/layout";
    }

    // ===== UPDATE EMPLOYEE (POST) WITH FILE UPLOADS =====
    @PostMapping("/dashboard/employees/update/{id}")
    public String updateEmployee(
            @PathVariable Long id,
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "birthday", required = false) String birthday,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "national", required = false) String national,
            @RequestParam(value = "religion", required = false) String religion,
            @RequestParam(value = "office_tel", required = false) String officeTel,
            @RequestParam(value = "contact_tel", required = false) String contactTel,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", defaultValue = "1") Short status,
            @RequestParam(value = "is_active", defaultValue = "true") String isActive,
            @RequestParam(value = "department_id", required = false) Integer departmentId,
            @RequestParam(value = "position_id", required = false) Integer positionId,
            @RequestParam(value = "app_role", required = false) Short appRole,
            @RequestParam(value = "enable_payroll", defaultValue = "true") boolean enablePayroll,
            @RequestParam(value = "self_password", required = false) String selfPassword,
            @RequestParam(value = "device_password", required = false) String devicePassword,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile,
            @RequestParam(value = "driver_license_automobile", required = false) MultipartFile driverLicenseAutoFile,
            @RequestParam(value = "driver_license_motorcycle", required = false) MultipartFile driverLicenseMotorFile,
  
            HttpSession session,
            Model model) {

        System.out.println("\n=== POST /dashboard/employees/update/" + id + " called ===");
        System.out.println("Department ID received: " + departmentId);
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        Optional<employees> employeeOpt = employeesRepository.findById(id);
        
        if (employeeOpt.isEmpty()) {
            System.out.println("Employee not found with ID: " + id);
            return "redirect:/dashboard/Employees?error=EmployeeNotFound";
        }
        
        employees employeeToUpdate = employeeOpt.get();
        System.out.println("Updating employee: " + employeeToUpdate.getFirst_name() + " " + employeeToUpdate.getLast_name());
        
        // Update basic fields
        employeeToUpdate.setFirst_name(firstName);
        employeeToUpdate.setLast_name(lastName);
        employeeToUpdate.setEmail(email);
        employeeToUpdate.setMobile(mobile);
        employeeToUpdate.setCity(city);
        employeeToUpdate.setNational(national);
        employeeToUpdate.setReligion(religion);
        employeeToUpdate.setOffice_tel(officeTel);
        employeeToUpdate.setContact_tel(contactTel);
        employeeToUpdate.setTitle(title);
        employeeToUpdate.setStatus(status);
        employeeToUpdate.setIs_active("true".equalsIgnoreCase(isActive));
        employeeToUpdate.setEnable_payroll(enablePayroll);
        employeeToUpdate.setUpdate_time(LocalDateTime.now());
  
        
        // Set app role
        if (appRole != null) {
            employeeToUpdate.setApp_role(appRole);
        }
        
        // Set gender
        if (gender != null && !gender.isEmpty()) {
            employeeToUpdate.setGender(gender);
        }
        
        // Set nickname if provided
        if (nickname != null && !nickname.isEmpty()) {
            employeeToUpdate.setNickname(nickname);
        }
        
        // Set birthday if provided
        if (birthday != null && !birthday.isEmpty()) {
            try {
                employeeToUpdate.setBirthday(LocalDate.parse(birthday));
            } catch (Exception e) {
                System.out.println("Invalid birthday format: " + birthday);
            }
        }
        
        // Set password if provided
        if (selfPassword != null && !selfPassword.isEmpty()) {
            employeeToUpdate.setSelf_password(selfPassword);
        }
        
        // Set device password if provided
        if (devicePassword != null && !devicePassword.isEmpty()) {
            employeeToUpdate.setDevice_password(devicePassword);
        }
        
        // Handle file uploads - only update if new file is provided
        if (photoFile != null && !photoFile.isEmpty()) {
            String photoFileName = saveFile(photoFile, "photo");
            if (photoFileName != null) {
                // Delete old photo if exists
                if (employeeToUpdate.getPhoto() != null && !employeeToUpdate.getPhoto().isEmpty()) {
                    deleteFile(employeeToUpdate.getPhoto());
                }
                employeeToUpdate.setPhoto(photoFileName);
            }
        }
        
        if (driverLicenseAutoFile != null && !driverLicenseAutoFile.isEmpty()) {
            String driverLicenseAutoFileName = saveFile(driverLicenseAutoFile, "license_auto");
            if (driverLicenseAutoFileName != null) {
                // Delete old license if exists
                if (employeeToUpdate.getDriver_license_automobile() != null && !employeeToUpdate.getDriver_license_automobile().isEmpty()) {
                    deleteFile(employeeToUpdate.getDriver_license_automobile());
                }
                employeeToUpdate.setDriver_license_automobile(driverLicenseAutoFileName);
            }
        }
        
        if (driverLicenseMotorFile != null && !driverLicenseMotorFile.isEmpty()) {
            String driverLicenseMotorFileName = saveFile(driverLicenseMotorFile, "license_motor");
            if (driverLicenseMotorFileName != null) {
                // Delete old license if exists
                if (employeeToUpdate.getDriver_license_motorcycle() != null && !employeeToUpdate.getDriver_license_motorcycle().isEmpty()) {
                    deleteFile(employeeToUpdate.getDriver_license_motorcycle());
                }
                employeeToUpdate.setDriver_license_motorcycle(driverLicenseMotorFileName);
            }
        }
        
        // Set department
        if (departmentId != null && departmentId > 0) {
            Optional<departments> deptOpt = departmentRepository.findById(departmentId.longValue());
            if (deptOpt.isPresent()) {
                employeeToUpdate.setDepartment_id(departmentId);
                System.out.println("Employee assigned to department ID: " + departmentId);
            }
        } else {
            employeeToUpdate.setDepartment_id(null);
        }
        
        // Set position
        if (positionId != null && positionId > 0) {
            employeeToUpdate.setPosition_id(positionId);
        } else {
            employeeToUpdate.setPosition_id(null);
        }
        
        try {
            employeeService.saveEmployee(employeeToUpdate);
            System.out.println("✅ Employee updated successfully: " + employeeToUpdate.getFirst_name() + " " + employeeToUpdate.getLast_name());
            
            return "redirect:/dashboard/Employees?success=EmployeeUpdated";
            
        } catch (Exception e) {
            System.err.println("❌ Error updating employee: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Error updating employee: " + e.getMessage());
            model.addAttribute("employee", employeeToUpdate);
            model.addAttribute("pageTitle", "Edit Employee");
            model.addAttribute("pageContent", "User/EditUser");
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("employeeDepartmentId", employeeToUpdate.getDepartment_id());
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            return "Components/layout";
        }
    }

    // ===== DELETE EMPLOYEE =====
    @GetMapping("/dashboard/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, HttpSession session) {
        System.out.println("=== GET /dashboard/employees/delete/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        if (currentEmployee.getId().equals(id)) {
            return "redirect:/dashboard/Employees?error=CannotDeleteSelf";
        }
        
        Optional<employees> employeeOpt = employeesRepository.findById(id);
        
        if (employeeOpt.isPresent()) {
            // Delete associated files
            employees employee = employeeOpt.get();
            if (employee.getPhoto() != null && !employee.getPhoto().isEmpty()) {
                deleteFile(employee.getPhoto());
            }
            if (employee.getDriver_license_automobile() != null && !employee.getDriver_license_automobile().isEmpty()) {
                deleteFile(employee.getDriver_license_automobile());
            }
            if (employee.getDriver_license_motorcycle() != null && !employee.getDriver_license_motorcycle().isEmpty()) {
                deleteFile(employee.getDriver_license_motorcycle());
            }
            
            linkRepository.deleteByMyuser_id(Math.toIntExact(id));
            employeesRepository.deleteById(id);
            System.out.println("Employee deleted with ID: " + id);
            return "redirect:/dashboard/Employees?success=EmployeeDeleted";
        }
        
        return "redirect:/dashboard/Employees?error=EmployeeNotFound";
    }

    // ===== PROFILE VIEW =====
    @GetMapping("/dashboard/profile/{id}")
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/profile/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        Optional<employees> employeeOpt = employeesRepository.findById(id);
        
        if (employeeOpt.isEmpty()) {
            return "redirect:/dashboard/Employees?error=EmployeeNotFound";
        }
        
        employees profileEmployee = employeeOpt.get();
        
        model.addAttribute("profileEmployee", profileEmployee);
        model.addAttribute("pageTitle", "Employee Profile");
        model.addAttribute("pageContent", "UserProfile/AccountSettings");
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        
        return "Components/layout";
    }

    // ===== HELPER METHODS =====
    
    // Save file to upload directory
    private String saveFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = prefix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File saved: " + fileName);
            return fileName;
            
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            return null;
        }
    }
    
    // Delete file from upload directory
    private void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("File deleted: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }

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
}