package com.campus.card.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student")
public class StudentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "student_no", nullable = false, unique = true, length = 64)
    private String studentNo;

    @Column(name = "class_id")
    private Long classId;

    /** 0: 禁用, 1: 启用 */
    private Integer status;

    // 学生照片存储路径或URL
    @Column(name = "photo_path")
    private String photoPath;

    // 学生档案（JSON），用于扩展存储各类档案信息
    @Lob
    private String archive;
}