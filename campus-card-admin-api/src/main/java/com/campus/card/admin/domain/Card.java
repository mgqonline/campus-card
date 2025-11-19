package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String cardNo;

    private Long typeId;
    // STUDENT/TEACHER/STAFF/VISITOR
    private String holderType;
    private String holderId;
    // ACTIVE/LOST/FROZEN/CANCELLED
    private String status = "ACTIVE";

    private BigDecimal balance = BigDecimal.ZERO;

    private LocalDateTime createdAt;
    // 临时卡过期时间（仅 VISITOR 使用，可为空）
    private LocalDateTime expireAt;
}