package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "class_subject_teacher",
       uniqueConstraints = @UniqueConstraint(columnNames = {"classId", "subjectId"}))
public class ClassSubjectTeacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long classId;

    @Column(nullable = false)
    private Long subjectId;

    @Column(nullable = false)
    private Long teacherId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}