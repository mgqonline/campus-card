package com.campus.card.admin.repository;

import com.campus.card.admin.domain.DataScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataScopeRepository extends JpaRepository<DataScope, Long> {
    List<DataScope> findBySubjectTypeAndSubjectId(String subjectType, Long subjectId);
    Optional<DataScope> findFirstBySubjectTypeAndSubjectId(String subjectType, Long subjectId);
}