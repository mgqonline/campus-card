package com.campus.card.wechat.model;

import javax.persistence.*;

@Entity
@Table(name = "student")
public class Student {
    @Id
    private Long id;
    private String name;
    private Long classId;
    private String grade;
    private String cardNo;
    private String faceStatus;
    @Column(name = "student_no")
    private String studentNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }
    public String getFaceStatus() { return faceStatus; }
    public void setFaceStatus(String faceStatus) { this.faceStatus = faceStatus; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
}