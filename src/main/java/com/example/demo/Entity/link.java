package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_user_auth_dept")
public class link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer myuser_id;
    private Integer leave_id;
    private Integer department_id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getmyuser_id() {
        return myuser_id;
    }

    public void setmyuser_id(Integer myuser_id) {
        this.myuser_id = myuser_id;
    }

    public Integer getleave_id() {
        return leave_id;
    }

    public void setleave_id(Integer leave_id) {
        this.leave_id = leave_id;
    }

    public Integer getdepartment_id() {
        return department_id;
    }

    public void setdepartment_id(Integer department_id) {
        this.department_id = department_id;
    }
}