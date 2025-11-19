package com.campus.card.admin.domain;

import java.io.Serializable;
import java.util.Objects;

public class StudentParentId implements Serializable {
    private Long studentId;
    private Long parentId;

    public StudentParentId() {}
    public StudentParentId(Long studentId, Long parentId) {
        this.studentId = studentId;
        this.parentId = parentId;
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentParentId that = (StudentParentId) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(parentId, that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, parentId);
    }
}