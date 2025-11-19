package com.campus.card.admin.repository;

import com.campus.card.admin.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByStudentIdAndAttendanceTimeBetween(Long studentId, LocalDateTime start, LocalDateTime end);
    List<AttendanceRecord> findByClassIdAndAttendanceTimeBetween(Long classId, LocalDateTime start, LocalDateTime end);
    List<AttendanceRecord> findByAttendanceTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.attendanceTime BETWEEN :start AND :end")
    long countByAttendanceTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.status = :status AND r.attendanceTime BETWEEN :start AND :end")
    long countByStatusAndAttendanceTimeBetween(@Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    boolean existsByStudentIdAndAttendanceTimeAndCheckTypeAndAttendanceType(Long studentId, LocalDateTime attendanceTime, String checkType, String attendanceType);
}