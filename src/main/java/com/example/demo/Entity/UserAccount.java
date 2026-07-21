package com.example.demo.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_user")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String first_name;
    private String last_name;
    private String email;
    private String password;
    private Integer tele_phone;
    @Column(name = "updated_time")
    private LocalDateTime update_time;

    private String emp_pin;
    private Integer login_id;
    private String login_type;
    private Integer login_count;
    private boolean is_staff;
    private boolean is_active;
    private boolean is_superuser;
    private boolean is_public;
    private boolean can_manage_all_dept;
    private Integer del_flag;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_join;

    @Column(name = "last_login")
    private LocalDateTime last_login;

    @Column(name = "session_key", unique = true)  // UNIQUE constraint to prevent duplicate keys
    private String session_key;
    
    private Integer login_ip;
    private String photo;

    @Column(unique = true)  
    private String username;  

    // Getters and Setters
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
        generateUsername();
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
        generateUsername();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer gettele_phone() {
        return tele_phone;
    }

    public void settele_phone(Integer tele_phone) {
        this.tele_phone = tele_phone;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    private void generateUsername() {
        if (this.first_name != null && !this.first_name.isEmpty() && 
            this.last_name != null && !this.last_name.isEmpty()) {
            String firstLetter = this.first_name.substring(0, 1);
            this.username = (firstLetter + this.last_name).toLowerCase();
        }
    }
    
    public void generateUsernameManually() {
        generateUsername();
    }

    public String getEmp_pin() {
        return emp_pin;
    }

    public void setEmp_pin(String emp_pin) {
        this.emp_pin = emp_pin;
    }
    public Integer getLogin_id() {
        return login_id;
    }
    public void setLogin_id(Integer login_id) {
        this.login_id = login_id;
    }
    public String getLogin_type() {
        return login_type;
    }
    public void setLogin_type(String login_type) {
        this.login_type = login_type;
    }
    public Integer getLogin_count() {
        return login_count;
    }

    public void setLogin_count(Integer login_count) {
        this.login_count = login_count;
    }

    public boolean getIs_staff() {
        return is_staff;
    }

    public void setIs_staff(boolean is_staff) {
        this.is_staff = is_staff;
    }

    public boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public boolean getIs_superuser() {
        return is_superuser;    
    }
    
    public void setIs_superuser(boolean is_superuser) {
        this.is_superuser = is_superuser;
    }           

    public boolean getIs_public() {
        return is_public;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    public boolean getCan_manage_all_dept() {
        return can_manage_all_dept;
    }

    public void setCan_manage_all_dept(boolean can_manage_all_dept) {
        this.can_manage_all_dept = can_manage_all_dept;
    }

    public LocalDate getDate_join() {
        return date_join;
    }

    public void setDate_join(LocalDate date_join) {
        this.date_join = date_join;
    }

    public LocalDateTime getLast_login() {
        return last_login;
    }

    public void setLast_login(LocalDateTime last_login) {
        this.last_login = last_login;
    }

    public String getSession_key() {
        return session_key;
    }

    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }

    public Integer getLogin_ip() {
        return login_ip;
    }
    public void setLogin_ip(Integer login_ip) {
        this.login_ip = login_ip;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Integer getDel_flag() {
        return del_flag;
    }

    public void setDel_flag(Integer del_flag) {
        this.del_flag = del_flag;
    }

    public LocalDateTime getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }

    // ===== REMEMBER ME HELPER METHODS =====
    public boolean isRememberTokenValid() {
        if (this.session_key == null || this.session_key.isEmpty()) {
            return false;
        }
        // Extract timestamp from session_key (format: token_timestamp)
        try {
            String[] parts = this.session_key.split("_");
            if (parts.length == 2) {
                long timestamp = Long.parseLong(parts[1]);
                long now = System.currentTimeMillis();
                long diffHours = (now - timestamp) / (1000 * 60 * 60);
                return diffHours < 24; // 24 hours expiration
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void clearRememberToken() {
        this.session_key = null;
    }

    public void setRememberToken(String token) {
        // Store token with timestamp: token_timestamp
        this.session_key = token + "_" + System.currentTimeMillis();
    }
    
    // Get the token part only (without timestamp)
    public String getRememberToken() {
        if (this.session_key == null || this.session_key.isEmpty()) {
            return null;
        }
        String[] parts = this.session_key.split("_");
        return parts.length > 0 ? parts[0] : null;
    }
}