package com.campus.card.wechat.model;

import javax.persistence.*;

@Entity
@Table(name = "grade")
public class Grade {
    @Id
    private Long id;
    
    private String name;
    
    private Integer year;
    
    @Column(name = "school_id")
    private Long schoolId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
}