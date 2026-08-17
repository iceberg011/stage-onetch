package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.Entity.departments;
import com.example.demo.Entity.employees;
import com.example.demo.Entity.link;
import com.example.demo.Repository.DepartmentRepository;
import com.example.demo.Repository.LinkRepository;
import com.example.demo.Service.DepartmentService;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@Controller
public class DepartmentsController {

    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;
    private final LinkRepository linkRepository;

    public DepartmentsController(DepartmentRepository departmentRepository, 
                                 DepartmentService departmentService,
                                 LinkRepository linkRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentService = departmentService;
        this.linkRepository = linkRepository;
    }

    // ===== LIST ALL DEPARTMENTS =====
    @GetMapping("/dashboard/departments")
    public String listDepartments(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session,
            Model model) {
        
        System.out.println("=== GET /dashboard/departments called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        List<departments> allDepartments = departmentService.searchDepartments(search, null, null, null);
        
        model.addAttribute("pageTitle", "Departments");
        model.addAttribute("pageContent", "Departments/Departments");
        
        model.addAttribute("employee", currentEmployee);
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        
        model.addAttribute("departments", allDepartments);
        model.addAttribute("departmentCount", allDepartments.size());
        model.addAttribute("searchQuery", search);
        
        return "Components/layout";
    }

    // ===== SHOW CREATE DEPARTMENT FORM =====
    @GetMapping("/dashboard/departments/CreateDepartment")
    public String showCreateDepartmentForm(HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/departments/CreateDepartment called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        // Get all departments for parent department dropdown (excluding itself)
        List<departments> allDepartments = departmentService.findAll();
        
        model.addAttribute("pageTitle", "Add Department");
        model.addAttribute("pageContent", "Departments/NewDepartment");
        
        model.addAttribute("employee", currentEmployee);
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        model.addAttribute("departments", allDepartments);
        
        return "Components/layout";
    }

    // ===== CREATE DEPARTMENT =====
    @PostMapping("/dashboard/departments/CreateDepartment")
    public String createDepartment(
            @RequestParam("dept_name") String deptName,
            @RequestParam(value = "is_default", defaultValue = "false") boolean isDefault,
            @RequestParam(value = "parent_dept_id", required = false) Integer parentDeptId,
            HttpSession session,
            Model model) {
        
        System.out.println("=== POST /dashboard/departments/CreateDepartment called ===");
        System.out.println("Department Name: " + deptName);
        System.out.println("Parent Department ID: " + parentDeptId);
        System.out.println("Is Default: " + isDefault);
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        // Validate required fields
        if (deptName == null || deptName.trim().isEmpty()) {
            model.addAttribute("error", "Department name is required");
            model.addAttribute("pageTitle", "Add Department");
            model.addAttribute("pageContent", "Departments/NewDepartment");
            model.addAttribute("employee", currentEmployee);
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            model.addAttribute("departments", departmentService.findAll());
            return "Components/layout";
        }
        
        // Check if department name already exists
        List<departments> allDepts = departmentRepository.findAll();
        for (departments dept : allDepts) {
            if (dept.getdept_name() != null && dept.getdept_name().equalsIgnoreCase(deptName.trim())) {
                model.addAttribute("error", "Department name already exists!");
                model.addAttribute("pageTitle", "Add Department");
                model.addAttribute("pageContent", "Departments/NewDepartment");
                model.addAttribute("employee", currentEmployee);
                model.addAttribute("firstName", currentEmployee.getFirst_name());
                model.addAttribute("lastName", currentEmployee.getLast_name());
                model.addAttribute("email", currentEmployee.getEmail());
                model.addAttribute("nickname", currentEmployee.getNickname());
                model.addAttribute("departments", departmentService.findAll());
                return "Components/layout";
            }
        }
        
        try {
            departments department = new departments();
            department.setdept_name(deptName.trim());
            department.setIs_default(isDefault);
            
            // Set parent department - FIXED: Check if parentDeptId is not null and > 0
            if (parentDeptId != null && parentDeptId > 0) {
                // Verify parent department exists
                Optional<departments> parentDept = departmentRepository.findById(parentDeptId.longValue());
                if (parentDept.isPresent()) {
                    department.setParent_dept_id(parentDeptId);
                    System.out.println("Parent department set to: " + parentDept.get().getdept_name());
                } else {
                    System.out.println("Parent department not found with ID: " + parentDeptId);
                    department.setParent_dept_id(null);
                }
            } else {
                department.setParent_dept_id(null);
                System.out.println("No parent department selected");
            }
            
            // Auto-generate dept_code
            department.setdept_code(departmentService.generateDepartmentCode());
            
            departmentService.save(department);
            
            System.out.println("Department created successfully with ID: " + department.getId());
            
            return "redirect:/dashboard/departments?success=DepartmentCreated";
            
        } catch (Exception e) {
            System.err.println("Error creating department: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "Error creating department: " + e.getMessage());
            model.addAttribute("pageTitle", "Add Department");
            model.addAttribute("pageContent", "Departments/NewDepartment");
            model.addAttribute("employee", currentEmployee);
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            model.addAttribute("departments", departmentService.findAll());
            return "Components/layout";
        }
    }

    // ===== SHOW EDIT DEPARTMENT FORM =====
    @GetMapping("/dashboard/departments/edit/{id}")
    public String showEditDepartmentForm(@PathVariable Long id, HttpSession session, Model model) {
        System.out.println("=== GET /dashboard/departments/edit/" + id + " called ===");

        employees currentEmployee = (employees) session.getAttribute("employee");

        if (currentEmployee == null) {
            return "redirect:/signin";
        }

        Optional<departments> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isEmpty()) {
            System.out.println("Department not found with ID: " + id);
            return "redirect:/dashboard/departments?error=DepartmentNotFound";
        }

        departments department = departmentOpt.get();
        List<departments> allDepartments = departmentService.findAll();

        model.addAttribute("pageTitle", "Edit Department");
        model.addAttribute("pageContent", "Departments/EditDepartment");
        model.addAttribute("employee", currentEmployee);
        model.addAttribute("firstName", currentEmployee.getFirst_name());
        model.addAttribute("lastName", currentEmployee.getLast_name());
        model.addAttribute("email", currentEmployee.getEmail());
        model.addAttribute("nickname", currentEmployee.getNickname());
        model.addAttribute("department", department);
        model.addAttribute("departments", allDepartments);

        return "Components/layout";
    }

    // ===== UPDATE DEPARTMENT =====
    @PostMapping("/dashboard/departments/update/{id}")
    public String updateDepartment(
            @PathVariable Long id,
            @RequestParam("dept_name") String deptName,
            @RequestParam(value = "is_default", defaultValue = "false") boolean isDefault,
            @RequestParam(value = "parent_dept_id", required = false) Integer parentDeptId,
            HttpSession session,
            Model model) {

        System.out.println("=== POST /dashboard/departments/update/" + id + " called ===");

        employees currentEmployee = (employees) session.getAttribute("employee");

        if (currentEmployee == null) {
            return "redirect:/signin";
        }

        Optional<departments> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isEmpty()) {
            return "redirect:/dashboard/departments?error=DepartmentNotFound";
        }

        if (deptName == null || deptName.trim().isEmpty()) {
            departments department = departmentOpt.get();
            model.addAttribute("error", "Department name is required");
            model.addAttribute("pageTitle", "Edit Department");
            model.addAttribute("pageContent", "Departments/EditDepartment");
            model.addAttribute("employee", currentEmployee);
            model.addAttribute("firstName", currentEmployee.getFirst_name());
            model.addAttribute("lastName", currentEmployee.getLast_name());
            model.addAttribute("email", currentEmployee.getEmail());
            model.addAttribute("nickname", currentEmployee.getNickname());
            model.addAttribute("department", department);
            model.addAttribute("departments", departmentService.findAll());
            return "Components/layout";
        }

        departments department = departmentOpt.get();

        for (departments existingDept : departmentRepository.findAll()) {
            if (!existingDept.getId().equals(id.intValue())
                    && existingDept.getdept_name() != null
                    && existingDept.getdept_name().equalsIgnoreCase(deptName.trim())) {
                model.addAttribute("error", "Department name already exists!");
                model.addAttribute("pageTitle", "Edit Department");
                model.addAttribute("pageContent", "Departments/EditDepartment");
                model.addAttribute("employee", currentEmployee);
                model.addAttribute("firstName", currentEmployee.getFirst_name());
                model.addAttribute("lastName", currentEmployee.getLast_name());
                model.addAttribute("email", currentEmployee.getEmail());
                model.addAttribute("nickname", currentEmployee.getNickname());
                model.addAttribute("department", department);
                model.addAttribute("departments", departmentService.findAll());
                return "Components/layout";
            }
        }

        department.setdept_name(deptName.trim());
        department.setIs_default(isDefault);

        if (parentDeptId != null && parentDeptId > 0 && !parentDeptId.equals(id.intValue())) {
            Optional<departments> parentDept = departmentRepository.findById(parentDeptId.longValue());
            if (parentDept.isPresent()) {
                department.setParent_dept_id(parentDeptId);
            } else {
                department.setParent_dept_id(null);
            }
        } else {
            department.setParent_dept_id(null);
        }

        departmentService.save(department);
        return "redirect:/dashboard/departments?success=DepartmentUpdated";
    }

    // ===== DELETE DEPARTMENT =====
    @GetMapping("/dashboard/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, HttpSession session) {
        System.out.println("=== GET /dashboard/departments/delete/" + id + " called ===");
        
        employees currentEmployee = (employees) session.getAttribute("employee");
        
        if (currentEmployee == null) {
            return "redirect:/signin";
        }
        
        // Check if department is linked to any user
        List<link> allLinks = linkRepository.findAll();
        for (link link : allLinks) {
            if (link.getdepartment_id() != null && link.getdepartment_id().equals(Math.toIntExact(id))) {
                return "redirect:/dashboard/departments?error=DepartmentInUse";
            }
        }
        
        // Also check if department has sub-departments (children)
        List<departments> allDepts = departmentRepository.findAll();
        for (departments dept : allDepts) {
            if (dept.getParent_dept_id() != null && dept.getParent_dept_id().equals(Math.toIntExact(id))) {
                return "redirect:/dashboard/departments?error=HasSubDepartments";
            }
        }
        
        try {
            departmentService.deleteById(id);
            return "redirect:/dashboard/departments?success=DepartmentDeleted";
        } catch (Exception e) {
            System.err.println("Error deleting department: " + e.getMessage());
            return "redirect:/dashboard/departments?error=DeleteFailed";
        }
    }
}