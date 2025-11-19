package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "card_tx")
public class CardTx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNo;
    // CONSUME/RECHARGE/REFUND
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String merchant;
    private LocalDateTime occurredAt;
    private String note;
}