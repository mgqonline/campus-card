package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "attendance_record")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long classId;
    private String className;
    private Long deviceId;
    private String deviceName;

    private LocalDateTime attendanceTime;

    // in/out
    private String attendanceType;
    // card/face
    private String checkType;
    private String photoUrl;
    // normal/late/early/absence
    private String status;
    private String remark;
}