package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "school_logo")
public class SchoolLogo {
    @Id
    private Long schoolId;

    @Column(nullable = false, length = 255)
    private String logoUrl;

    private LocalDateTime updatedAt;
}