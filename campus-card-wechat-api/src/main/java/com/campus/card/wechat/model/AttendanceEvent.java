package com.campus.card.wechat.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_event")
public class AttendanceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long childId;
    private String type; // 进校/离校
    private String gate;
    private LocalDateTime time;
    private String photoUrl;
    private Boolean late;
    private Boolean earlyLeave;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Boolean getLate() { return late; }
    public void setLate(Boolean late) { this.late = late; }
    public Boolean getEarlyLeave() { return earlyLeave; }
    public void setEarlyLeave(Boolean earlyLeave) { this.earlyLeave = earlyLeave; }
}