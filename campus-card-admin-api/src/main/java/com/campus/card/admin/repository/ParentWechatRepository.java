package com.campus.card.admin.repository;

import com.campus.card.admin.domain.ParentWechat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentWechatRepository extends JpaRepository<ParentWechat, Long> {
}