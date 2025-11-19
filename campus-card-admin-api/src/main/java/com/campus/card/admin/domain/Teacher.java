package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "teacher")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String teacherNo;

    private String department;
    private String phone;

    /** 0: 禁用, 1: 启用 */
    private Integer status = 1;

    private Long schoolId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}