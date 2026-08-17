package com.example.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "att_attshift")
public class shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer alias;

    @Column(name = "cycle_unit")
    private Integer cycleUnit;

   


  




}