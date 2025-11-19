package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "school_config")
public class SchoolConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long schoolId;

    @Column(name = "cfg_key", nullable = false, length = 64)
    private String key;

    @Column(name = "cfg_value", length = 256)
    private String value;
}