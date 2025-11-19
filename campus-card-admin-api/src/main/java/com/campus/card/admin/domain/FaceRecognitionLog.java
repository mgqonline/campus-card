package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "face_recognition_log")
public class FaceRecognitionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long deviceId;
    private String personType; // STUDENT/TEACHER
    private String personId;   // 学号或工号
    private Double score;
    private Boolean success;
    private String photoUrl;
    private LocalDateTime occurredAt;
    private String remark;
}