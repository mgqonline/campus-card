package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "system_config")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cfg_key", nullable = false, length = 64, unique = true)
    private String key;

    @Column(name = "cfg_value", length = 512)
    private String value;

    @Column(name = "category", length = 32)
    private String category; // e.g. PARAM, INTERFACE, PUSH, STORAGE, BACKUP

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}