package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "semester")
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long schoolId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 32)
    private String code;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean current = false;

    /** 0: 禁用, 1: 启用 */
    private Integer status = 1;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}