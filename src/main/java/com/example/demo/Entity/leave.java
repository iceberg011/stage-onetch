
package com.example.demo.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "att_leave")
public class leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workflowinstance_ptr_id;

    @Column(name = "start_time")
    private LocalDateTime start_time;

    @Column(name = "end_time")
    private LocalDateTime end_time;
    
    @Column(name = "apply_reason", length = 1000)
    private String apply_reason;

    @Column(name = "apply_time")
    private LocalDateTime apply_time;


    @Column(name = "attachement", length = 100)
    private String attachement;

    private Integer pay_code_id;

    private Double leave_day;

    
    public Integer getWorkflowinstance_ptr_id() {
        return workflowinstance_ptr_id;
    }

    public void setWorkflowinstance_ptr_id(Integer workflowinstance_ptr_id) {
        this.workflowinstance_ptr_id = workflowinstance_ptr_id;
    }


    public LocalDateTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public LocalDateTime getEnd_time() {
        return end_time;
    }

    public void setEnd_time(LocalDateTime end_time) {
        this.end_time = end_time;
    }


    public String getApply_reason() {
        return apply_reason;
    }

    public void setApply_reason(String apply_reason) {
        this.apply_reason = apply_reason;
    }

    public LocalDateTime getApply_time() {
        return apply_time;
    }

    public void setApply_time(LocalDateTime apply_time) {
        this.apply_time = apply_time;
    }

    public String getAttachement() {
        return attachement;
    }

    public void setAttachement(String attachement) {
        this.attachement = attachement;
    }

    public Integer getPay_code_id() {
        return pay_code_id;
    }

    public void setPay_code_id(Integer pay_code_id) {
        this.pay_code_id = pay_code_id;
    }
    
    public Double getLeave_day() {
        return leave_day;
    }


    public void setLeave_day(Double leave_day) {
        this.leave_day = leave_day;
    }







}