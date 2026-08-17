package com.example.demo.DTO;

import com.example.demo.Entity.departments;
import com.example.demo.Entity.employees;
import java.util.List;

public class EmployeeWithDepartmentsDTO {
    private Long id;
    private String first_name;
    private String last_name;
    private String email;
    private String nickname;
    private String emp_code;
    private String mobile;
    private boolean is_active;
    private Short app_role;
    private Integer department_id;
    private String gender;
    private String city;
    private String title;
    private List<departments> departments;

    // Constructor
    public EmployeeWithDepartmentsDTO(employees employee, List<departments> departments) {
        this.id = employee.getId();
        this.first_name = employee.getFirst_name();
        this.last_name = employee.getLast_name();
        this.email = employee.getEmail();
        this.nickname = employee.getNickname();
        this.emp_code = employee.getEmp_code();
        this.mobile = employee.getMobile();
        this.is_active = employee.isIs_active();
        this.app_role = employee.getApp_role();
        this.department_id = employee.getDepartment_id();
        this.gender = employee.getGender();
        this.city = employee.getCity();
        this.title = employee.getTitle();
        this.departments = departments;
    }

    // ===== GETTERS AND SETTERS =====
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getFirst_name() { 
        return first_name; 
    }
    public void setFirst_name(String first_name) { 
        this.first_name = first_name; 
    }

    public String getLast_name() { 
        return last_name; 
    }
    public void setLast_name(String last_name) { 
        this.last_name = last_name; 
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getNickname() { 
        return nickname; 
    }
    public void setNickname(String nickname) { 
        this.nickname = nickname; 
    }

    public String getEmp_code() { 
        return emp_code; 
    }
    public void setEmp_code(String emp_code) { 
        this.emp_code = emp_code; 
    }

    public String getMobile() { 
        return mobile; 
    }
    public void setMobile(String mobile) { 
        this.mobile = mobile; 
    }

    public boolean isIs_active() { 
        return is_active; 
    }
    public void setIs_active(boolean is_active) { 
        this.is_active = is_active; 
    }

    public Short getApp_role() { 
        return app_role; 
    }
    public void setApp_role(Short app_role) { 
        this.app_role = app_role; 
    }

    public Integer getDepartment_id() { 
        return department_id; 
    }
    public void setDepartment_id(Integer department_id) { 
        this.department_id = department_id; 
    }

    public String getGender() { 
        return gender; 
    }
    public void setGender(String gender) { 
        this.gender = gender; 
    }

    public String getCity() { 
        return city; 
    }
    public void setCity(String city) { 
        this.city = city; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public List<departments> getDepartments() { 
        return departments; 
    }
    public void setDepartments(List<departments> departments) { 
        this.departments = departments; 
    }

    // ===== HELPER METHODS =====
    public String getFullName() {
        return this.first_name + " " + this.last_name;
    }

    public String getRoleName() {
        if (this.app_role == null) {
            return "User";
        }
        switch (this.app_role) {
            case 1:
                return "Administrator";
            case 2:
                return "Manager";
            case 3:
                return "Staff";
            default:
                return "User";
        }
    }

    public boolean isAdmin() {
        return this.app_role != null && this.app_role == 1;
    }

    public boolean isManager() {
        return this.app_role != null && this.app_role == 2;
    }

    public boolean isStaff() {
        return this.app_role != null && this.app_role == 3;
    }
}