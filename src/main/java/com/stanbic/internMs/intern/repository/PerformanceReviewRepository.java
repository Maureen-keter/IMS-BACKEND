package com.stanbic.internMs.intern.repository;

import com.stanbic.internMs.intern.models.PerformanceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview,Long> {
    Page<PerformanceReview> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
