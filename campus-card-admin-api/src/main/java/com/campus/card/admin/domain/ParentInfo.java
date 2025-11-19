package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "parent")
public class ParentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 32)
    private String phone;

    // 数据库 parent 表无 email 列，暂不映射以避免查询错误
    @Transient
    private String email;

    /** 0: 禁用, 1: 启用 */
    private Integer status = 1;

    // 关系（父亲/母亲/监护人/其他），与表 parent.relation 对齐
    @Column(length = 32)
    private String relation;

    // 数据库表 parent 不包含 create_time/update_time 列，避免查询时报 Unknown column 错误
    @Transient
    private LocalDateTime createTime;
    @Transient
    private LocalDateTime updateTime;
}