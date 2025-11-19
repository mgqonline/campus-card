package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNo(String cardNo);
    List<Card> findByHolderTypeAndHolderId(String holderType, String holderId);
}