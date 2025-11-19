package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.HelpArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpArticleRepository extends JpaRepository<HelpArticle, Long> {
    long countByCategory(String category);
}