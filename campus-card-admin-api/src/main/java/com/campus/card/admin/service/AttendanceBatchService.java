package com.campus.card.admin.service;

import com.campus.card.admin.domain.AttendanceRecord;
import com.campus.card.admin.domain.FaceRecognitionLog;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.repository.AttendanceRecordRepository;
import com.campus.card.admin.repository.FaceRecognitionLogRepository;
import com.campus.card.admin.repository.CardRepository;
import com.campus.card.admin.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceBatchService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final FaceRecognitionLogRepository faceRecognitionLogRepository;
    private final CardRepository cardRepository;
    private final StudentRepository studentRepository;

    public AttendanceBatchService(AttendanceRecordRepository attendanceRecordRepository,
                                  FaceRecognitionLogRepository faceRecognitionLogRepository,
                                  CardRepository cardRepository,
                                  StudentRepository studentRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.faceRecognitionLogRepository = faceRecognitionLogRepository;
        this.cardRepository = cardRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public void processFaceBatch(List<com.campus.card.admin.controller.AttendanceController.FaceIngestReq> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        // 1) 批量记录识别日志
        List<FaceRecognitionLog> logs = new ArrayList<>(reqs.size());
        for (com.campus.card.admin.controller.AttendanceController.FaceIngestReq req : reqs) {
            FaceRecognitionLog log = new FaceRecognitionLog();
            log.setDeviceId(req.getDeviceId());
            log.setPersonType(req.getPersonType());
            log.setPersonId(req.getPersonId());
            log.setScore(req.getScore());
            log.setSuccess(Boolean.TRUE.equals(req.getSuccess()));
            log.setPhotoUrl(req.getPhotoUrl());
            log.setOccurredAt(req.getAttendanceTime() != null ? req.getAttendanceTime() : LocalDateTime.now());
            log.setRemark(req.getRemark());
            logs.add(log);
        }
        faceRecognitionLogRepository.saveAll(logs);

        // 2) 批量生成成功识别的考勤记录（仅学生）
        List<com.campus.card.admin.controller.AttendanceController.FaceIngestReq> successStudentReqs = reqs.stream()
                .filter(r -> Boolean.TRUE.equals(r.getSuccess()) && "STUDENT".equalsIgnoreCase(r.getPersonType()))
                .collect(Collectors.toList());
        if (successStudentReqs.isEmpty()) return;

        // 学号映射到学生信息（逐个查询）
        Map<String, StudentInfo> studentMap = successStudentReqs.stream()
                .map(r -> studentRepository.findByStudentNo(r.getPersonId()).orElse(null))
                .filter(s -> s != null)
                .collect(Collectors.toMap(StudentInfo::getStudentNo, s -> s, (a, b) -> a));

        List<AttendanceRecord> batchRecords = new ArrayList<>();
        for (com.campus.card.admin.controller.AttendanceController.FaceIngestReq req : successStudentReqs) {
            StudentInfo student = studentMap.get(req.getPersonId());
            if (student == null) continue;
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId(student.getId());
            record.setStudentName(student.getName());
            record.setStudentNo(student.getStudentNo());
            record.setClassId(student.getClassId());
            record.setAttendanceTime(req.getAttendanceTime() != null ? req.getAttendanceTime() : LocalDateTime.now());
            record.setAttendanceType(req.getAttendanceType() != null ? req.getAttendanceType() : "in");
            record.setCheckType("face");
            record.setPhotoUrl(req.getPhotoUrl());
            record.setStatus("normal");
            record.setRemark(req.getRemark());
            if (!attendanceRecordRepository.existsByStudentIdAndAttendanceTimeAndCheckTypeAndAttendanceType(
                    record.getStudentId(), record.getAttendanceTime(), record.getCheckType(), record.getAttendanceType())) {
                batchRecords.add(record);
            }
        }
        if (!batchRecords.isEmpty()) {
            attendanceRecordRepository.saveAll(batchRecords);
        }
    }

    @Transactional
    public void processCardBatch(List<com.campus.card.admin.controller.AttendanceController.CardIngestReq> reqs) {
        if (reqs == null || reqs.isEmpty()) return;
        List<AttendanceRecord> batchRecords = new ArrayList<>(reqs.size());
        for (com.campus.card.admin.controller.AttendanceController.CardIngestReq req : reqs) {
            com.campus.card.admin.domain.Card card = cardRepository.findByCardNo(req.getCardNo()).orElse(null);
            if (card == null) continue;
            if (card.getHolderType() == null || !"STUDENT".equalsIgnoreCase(card.getHolderType())) continue;
            com.campus.card.admin.domain.StudentInfo student = studentRepository.findByStudentNo(card.getHolderId()).orElse(null);
            if (student == null) continue;
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId(student.getId());
            record.setStudentName(student.getName());
            record.setStudentNo(student.getStudentNo());
            record.setClassId(student.getClassId());
            record.setAttendanceTime(req.getAttendanceTime() != null ? req.getAttendanceTime() : LocalDateTime.now());
            record.setAttendanceType(req.getAttendanceType() != null ? req.getAttendanceType() : "in");
            record.setCheckType("card");
            record.setRemark(req.getRemark());
            record.setStatus("normal");
            if (!attendanceRecordRepository.existsByStudentIdAndAttendanceTimeAndCheckTypeAndAttendanceType(
                    record.getStudentId(), record.getAttendanceTime(), record.getCheckType(), record.getAttendanceType())) {
                batchRecords.add(record);
            }
        }
        if (!batchRecords.isEmpty()) {
            attendanceRecordRepository.saveAll(batchRecords);
        }
    }
}