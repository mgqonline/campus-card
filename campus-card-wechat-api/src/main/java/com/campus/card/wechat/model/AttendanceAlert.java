package com.campus.card.wechat.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_alert")
public class AttendanceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long childId;
    private String title;
    @Column(name = "txtdesc")
    private String txtdesc;
    private LocalDateTime time;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTxtdesc() { return txtdesc; }
    public void setTxtdesc(String txtdesc) { this.txtdesc = txtdesc; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}