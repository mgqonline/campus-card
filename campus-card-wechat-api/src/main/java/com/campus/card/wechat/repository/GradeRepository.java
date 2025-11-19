package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}