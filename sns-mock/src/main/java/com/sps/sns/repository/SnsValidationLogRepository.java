package com.sps.sns.repository;

import com.sps.sns.entity.SnsValidationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnsValidationLogRepository extends JpaRepository<SnsValidationLog, Long> {
}
