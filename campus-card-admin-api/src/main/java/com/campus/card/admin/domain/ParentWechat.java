package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "parent_wechat")
public class ParentWechat {
    @Id
    @Column(name = "parent_id")
    private Long parentId; // 与家长ID一致，作为主键

    @Column(name = "open_id", length = 128, nullable = false)
    private String openId;

    @Column(name = "union_id", length = 128)
    private String unionId;

    @Column(name = "nickname", length = 64)
    private String nickname;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "status")
    private Integer status = 1; // 1: 已绑定, 0: 解绑

    @Column(name = "bind_time")
    private LocalDateTime bindTime;
}