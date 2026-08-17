package com.example.demo.Entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "att_payloadattcode")
public class payload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "att_date")
    private LocalDate attdate;
    
    private Short week;
    private Short  weekday;


    @Column(name = "work_code", length = 20)
    private String workcode;


    @Column(name = "punch_state")
    private boolean punchstate;

    @Column(name = "punch_date")
    private LocalDate punchdate;

     @Column(name = "punch_time")
    private LocalTime punchtime;

    @Column(name = "punch_datetime")
    private LocalDateTime punchdatetime;






    @Column(name = "adjust_state" , length = 5)
    private String adjuststate;

    @Column(name = "emp_id")
    private Integer empid;

    @Column(name = "trans_id")
    private Integer transid;

    //uuid
    @Column(name = "time_card_id")
    private Integer timecardid;





    // ===== DEFAULT CONSTRUCTOR =====
    public payload() {}

    // ===== PARAMETERIZED CONSTRUCTOR =====
    public payload(LocalDate attdate, Short week, Short weekday, String workcode,
                   boolean punchstate, LocalDate punchdate, LocalTime punchtime,
                   LocalDateTime punchdatetime, String adjuststate, Integer empid,
                   Integer transid, Integer timecardid) {
        this.attdate = attdate;
        this.week = week;
        this.weekday = weekday;
        this.workcode = workcode;
        this.punchstate = punchstate;
        this.punchdate = punchdate;
        this.punchtime = punchtime;
        this.punchdatetime = punchdatetime;
        this.adjuststate = adjuststate;
        this.empid = empid;
        this.transid = transid;
        this.timecardid = timecardid;
    }

    // ===== GETTERS AND SETTERS =====
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getAttdate() {
        return attdate;
    }

    public void setAttdate(LocalDate attdate) {
        this.attdate = attdate;
    }

    public Short getWeek() {
        return week;
    }

    public void setWeek(Short week) {
        this.week = week;
    }

    public Short getWeekday() {
        return weekday;
    }

    public void setWeekday(Short weekday) {
        this.weekday = weekday;
    }

    public String getWorkcode() {
        return workcode;
    }

    public void setWorkcode(String workcode) {
        this.workcode = workcode;
    }

    public boolean isPunchstate() {
        return punchstate;
    }

    public void setPunchstate(boolean punchstate) {
        this.punchstate = punchstate;
    }

    public LocalDate getPunchdate() {
        return punchdate;
    }

    public void setPunchdate(LocalDate punchdate) {
        this.punchdate = punchdate;
    }

    public LocalTime getPunchtime() {
        return punchtime;
    }

    public void setPunchtime(LocalTime punchtime) {
        this.punchtime = punchtime;
    }

    public LocalDateTime getPunchdatetime() {
        return punchdatetime;
    }

    public void setPunchdatetime(LocalDateTime punchdatetime) {
        this.punchdatetime = punchdatetime;
    }

    public String getAdjuststate() {
        return adjuststate;
    }

    public void setAdjuststate(String adjuststate) {
        this.adjuststate = adjuststate;
    }

    public Integer getEmpid() {
        return empid;
    }

    public void setEmpid(Integer empid) {
        this.empid = empid;
    }

    public Integer getTransid() {
        return transid;
    }

    public void setTransid(Integer transid) {
        this.transid = transid;
    }

    public Integer getTimecardid() {
        return timecardid;
    }

    public void setTimecardid(Integer timecardid) {
        this.timecardid = timecardid;
    }

    // ===== HELPER METHODS =====

    /**
     * Check if this is a punch-in record
     */
    public boolean isPunchIn() {
        return punchstate && workcode != null && workcode.equalsIgnoreCase("IN");
    }

    /**
     * Check if this is a punch-out record
     */
    public boolean isPunchOut() {
        return punchstate && workcode != null && workcode.equalsIgnoreCase("OUT");
    }

    /**
     * Get formatted punch date and time
     */
    public String getFormattedPunchDateTime() {
        if (punchdatetime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return punchdatetime.format(formatter);
        } else if (punchdate != null && punchtime != null) {
            return punchdate.toString() + " " + punchtime.toString();
        } else if (punchdate != null) {
            return punchdate.toString();
        } else if (punchtime != null) {
            return punchtime.toString();
        }
        return "N/A";
    }

    /**
     * Get formatted att date
     */
    public String getFormattedAttDate() {
        if (attdate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return attdate.format(formatter);
        }
        return "N/A";
    }

    /**
     * Get week number as string
     */
    public String getWeekString() {
        return week != null ? String.valueOf(week) : "N/A";
    }

    /**
     * Get weekday name
     */
    public String getWeekdayName() {
        if (weekday == null) return "N/A";
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int index = weekday.intValue();
        if (index >= 1 && index <= 7) {
            return days[index];
        }
        return "N/A";
    }

    // ===== TO STRING =====
    @Override
    public String toString() {
        return "payload{" +
                "id=" + id +
                ", attdate=" + attdate +
                ", week=" + week +
                ", weekday=" + weekday +
                ", workcode='" + workcode + '\'' +
                ", punchstate=" + punchstate +
                ", punchdate=" + punchdate +
                ", punchtime=" + punchtime +
                ", punchdatetime=" + punchdatetime +
                ", adjuststate='" + adjuststate + '\'' +
                ", empid=" + empid +
                ", transid=" + transid +
                ", timecardid=" + timecardid +
                '}';
    }

}