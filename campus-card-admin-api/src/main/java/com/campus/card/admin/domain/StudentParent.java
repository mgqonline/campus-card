package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "student_parent")
@IdClass(StudentParentId.class)
public class StudentParent {
    @Id
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Id
    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(length = 32)
    private String relation; // 父亲/母亲/监护人/其他
}