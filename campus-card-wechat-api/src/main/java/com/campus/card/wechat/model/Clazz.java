package com.campus.card.wechat.model;

import javax.persistence.*;

@Entity
@Table(name = "school_class")
public class Clazz {
    @Id
    private Long id;
    
    private String name;
    
    @Column(name = "grade_id")
    private Long gradeId;
    
    @Column(name = "school_id")
    private Long schoolId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }
    
    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
}