package com.campus.card.wechat.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_application")
public class LeaveApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "type", length = 32)
    private String type; // SICK/PERSONAL/OTHER

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "reason", length = 1024)
    private String reason;

    @Lob
    @Column(name = "attachments_json")
    private String attachmentsJson; // JSON 数组字符串

    @Column(name = "status", length = 20)
    private String status; // PENDING/APPROVED/REJECTED/CANCELED

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "approver_teacher_id")
    private Long approverTeacherId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getAttachmentsJson() { return attachmentsJson; }
    public void setAttachmentsJson(String attachmentsJson) { this.attachmentsJson = attachmentsJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
    public Long getApproverTeacherId() { return approverTeacherId; }
    public void setApproverTeacherId(Long approverTeacherId) { this.approverTeacherId = approverTeacherId; }
}