package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}