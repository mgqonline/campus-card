package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "school_class")
public class Clazz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "school_id")
    private Long schoolId;
    
    /** 班主任教师ID */
    @Column(name = "head_teacher_id")
    private Long headTeacherId;

    /** 0: 禁用, 1: 启用 */
    private Integer status = 1;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /** 非持久化：班主任姓名，供前端展示 */
    @Transient
    private String headTeacherName;
}