package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "org_data_scope")
public class DataScope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SUBJECT: USER 或 ROLE */
    @Column(name = "subject_type", length = 16, nullable = false)
    private String subjectType; // USER / ROLE

    @Column(name = "subject_id", nullable = false)
    private Long subjectId; // 绑定的用户ID或角色ID

    /** SCOPE: SCHOOL / GRADE / CLASS / PERSONAL */
    @Column(name = "scope_type", length = 16, nullable = false)
    private String scopeType; // SCHOOL / GRADE / CLASS / PERSONAL

    /** 允许的学校/年级/班级/学生ID（CSV，逗号分隔），根据 scopeType 使用对应字段 */
    @Column(name = "school_ids", length = 2000)
    private String schoolIds;

    @Column(name = "grade_ids", length = 2000)
    private String gradeIds;

    @Column(name = "class_ids", length = 2000)
    private String classIds;

    @Column(name = "student_ids", length = 2000)
    private String studentIds;

    /** 状态：1 启用 0 禁用 */
    @Column(name = "status")
    private Integer status = 1;
}