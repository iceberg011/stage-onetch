package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "personnel_department")
public class departments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dept_code", unique = true, length = 50)
    private String dept_code;
    
    @Column(name = "dept_name", length = 100)
    private String dept_name;
    
    @Column(name = "is_default")
    private boolean is_default;
    
    private Integer parent_dept_id;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getdept_code() {
        return dept_code;
    }

    public void setdept_code(String dept_code) {
        this.dept_code = dept_code;
    }

    public String getdept_name() {
        return dept_name;
    }

    public void setdept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public boolean getIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }

    public Integer getParent_dept_id() {
        return parent_dept_id;
    }

    public void setParent_dept_id(Integer parent_dept_id) {
        this.parent_dept_id = parent_dept_id;
    }
}