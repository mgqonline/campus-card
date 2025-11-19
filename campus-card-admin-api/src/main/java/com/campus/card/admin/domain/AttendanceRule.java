package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "attendance_rule")
public class AttendanceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 规则适用场景：SCHOOL 或 ENTERPRISE
    @Column(length = 32)
    private String scenario = "SCHOOL";

    // 工作日，使用逗号分隔的简写：MON,TUE,WED,THU,FRI,SAT,SUN
    @Column(name = "work_days", length = 64)
    private String workDays = "MON,TUE,WED,THU,FRI";

    // 上班（到校/上班）时间
    @Column(name = "work_start")
    private LocalTime workStart = LocalTime.of(8, 0);

    // 下班（离校/下班）时间
    @Column(name = "work_end")
    private LocalTime workEnd = LocalTime.of(17, 0);

    // 迟到容忍分钟数
    @Column(name = "late_grace_min")
    private Integer lateGraceMin = 5;

    // 早退容忍分钟数
    @Column(name = "early_grace_min")
    private Integer earlyGraceMin = 5;

    @Column(name = "enabled")
    private Boolean enabled = true;
}