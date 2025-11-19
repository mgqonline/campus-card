package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "student_face")
public class StudentFace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(length = 128)
    private String faceToken; // 人脸特征标识/第三方接口返回token

    /** 0: 禁用, 1: 启用 */
    private Integer status = 1;
}