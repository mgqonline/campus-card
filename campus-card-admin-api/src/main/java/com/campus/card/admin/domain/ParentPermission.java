package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "parent_permission")
public class ParentPermission {
    @Id
    private Long parentId; // 作为主键

    @Column(name = "view_attendance")
    private Boolean viewAttendance = true;
    @Column(name = "view_consumption")
    private Boolean viewConsumption = true;
    @Column(name = "view_grades")
    private Boolean viewGrades = true;
    @Column(name = "message_teacher")
    private Boolean messageTeacher = true;
}