package com.example.demo.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personnel_employee")
public class employees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime create_time;
    private String create_user;
    private LocalDateTime change_time;
    private Short status;
    private String emp_code;



    private String first_name;
    private String last_name;
    private String nickname;

    @Column(name = "passport", length = 100)    
    private String passport;

    private String driver_license_automobile;
    private String driver_license_motorcycle;
    private String photo;
    private String self_password;
    private String device_password;
    private Integer dev_previlege;
    private String card_no;
    private String acc_group;
    private String acc_timezone;
    private String gender;
    private LocalDate birthday;
    private String office_tel;
    private String contact_tel;
    private String mobile;
    private String national;
    private String religion;
    private String title;
    private String enroll_sn;
    private String ssn;
    private LocalDateTime update_time;
    
    @Column(name = "hire_date", nullable = true) 
    private LocalDate hire_date;
    
    private Integer verify_mode;
    private String city;
    private Short emp_type;
    private boolean enable_payroll;
    private String app_status;
    private Short app_role;

    private String email;
    private LocalDateTime last_login;

    private boolean is_active;
    private String session_key;
    private Integer department_id;
    private Integer position_id;
    private Integer leave_groupe;



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    // getters and setters
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSelf_password() {
        return self_password;
    }

    public void setSelf_password(String self_password) {
        this.self_password = self_password;
    }

    public String getDevice_password() {
        return device_password;
    }

    public void setDevice_password(String device_password) {
        this.device_password = device_password;
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

    public String getEmp_code() {
        return emp_code;
    }

    public void setEmp_code(String emp_code) {
        this.emp_code = emp_code;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

     public String getNickname() {
        return nickname; // Assuming nickname is used as username
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    private void generateNickname() {
        if (this.first_name != null && !this.first_name.isEmpty() && 
            this.last_name != null && !this.last_name.isEmpty()) {
            String firstLetter = this.first_name.substring(0, 1);
            this.nickname = (firstLetter + this.last_name).toLowerCase();
        }
    }
    
    public void generateNicknameManually() {
        generateNickname();
    }


    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public LocalDateTime getChange_time() {
        return change_time;
    }
    public void setChange_time(LocalDateTime change_time) {
        this.change_time = change_time;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }
    public Short getStatus() {
        return status;
    }
    public void setStatus(Short status) {
        this.status = status;
    }
    public LocalDate getBirthday() {
        return birthday;
    }
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday ;
    }
    public String getNational() {
        return national;
    }
    public void setNational(String national) {
        this.national = national;
    }
    public String getReligion() {
        return religion;
    }
    public void setReligion(String religion) {
        this.religion = religion;
    }
    public boolean isIs_active() {
        return is_active;
    }
    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }
    public String getSession_key() {
        return session_key;
    }
    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }
    public Integer getDepartment_id() {
        return department_id;
    }
    public void setDepartment_id(Integer department_id) {
        this.department_id = department_id;
    }
    public Integer getPosition_id() {
        return position_id;
    }
    public void setPosition_id(Integer position_id) {
        this.position_id = position_id;
    }

    public Integer getLeave_groupe() {
        return leave_groupe;
    }
    public void setLeave_groupe(Integer leave_groupe) {
        this.leave_groupe = leave_groupe;
    }
    public LocalDateTime getLast_login() {
        return last_login;
    }
    public void setLast_login(LocalDateTime last_login) {
        this.last_login = last_login;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnroll_sn() {
        return enroll_sn;
    }
    public void setEnroll_sn(String enroll_sn) {
        this.enroll_sn = enroll_sn;
    }
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
    public LocalDateTime getUpdate_time() {
        return update_time;
    }
    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }
    public LocalDate getHire_date() {
        return hire_date;
    }
    public void setHire_date(LocalDate hire_date) {
        this.hire_date = hire_date;
    }
    public Integer getVerify_mode() {
        return verify_mode;
    }
    public void setVerify_mode(Integer verify_mode) {
        this.verify_mode = verify_mode;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public Short getEmp_type() {
        return emp_type;
    }
    public void setEmp_type(Short emp_type) {
        this.emp_type = emp_type;
    }

    public boolean isEnable_payroll() {
        return enable_payroll;
    }
    public void setEnable_payroll(boolean enable_payroll) {
        this.enable_payroll = enable_payroll;
    }
    public String getApp_status() {
        return app_status;
    }
    public void setApp_status(String app_status) {
        this.app_status = app_status;
    }
    public Short getApp_role() {
        return app_role;
    }


    public void setApp_role(Short app_role) {
        this.app_role = app_role;
    }
    public Integer getDev_previlege() {
        return dev_previlege;
    }
    public void setDev_previlege(Integer dev_previlege) {
        this.dev_previlege = dev_previlege;
    }
    public String getCard_no() {
        return card_no;
    }
    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }
    public String getAcc_group() {
        return acc_group;
    }
    public void setAcc_group(String acc_group) {
        this.acc_group = acc_group;
    }
    public String getAcc_timezone() {
        return acc_timezone;
    }


    public void setAcc_timezone(String acc_timezone) {
        this.acc_timezone = acc_timezone;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getOffice_tel() {
        return office_tel;
    }


    public void setOffice_tel(String office_tel) {
        this.office_tel = office_tel;
    }
    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContact_tel() {
        return contact_tel;
    }
    public void setContact_tel(String contact_tel) {
        this.contact_tel = contact_tel;
    }
    public String getDriver_license_automobile() {
        return driver_license_automobile;
    }
    public void setDriver_license_automobile(String driver_license_automobile) {
        this.driver_license_automobile = driver_license_automobile;
    }
    public String getDriver_license_motorcycle() {
        return driver_license_motorcycle;
    }
    public void setDriver_license_motorcycle(String driver_license_motorcycle) {
        this.driver_license_motorcycle = driver_license_motorcycle;
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