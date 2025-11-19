package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    /** 0: 禁用, 1: 启用 */
    private Integer status = 1;

    /** 可选：归属学校ID（前端暂未使用，可留空） */
    private Long schoolId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}