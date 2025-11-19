package com.campus.card.admin.repository;

import com.campus.card.admin.domain.SchoolLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolLogoRepository extends JpaRepository<SchoolLogo, Long> {
}