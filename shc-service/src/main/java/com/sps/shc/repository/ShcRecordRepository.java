package com.sps.shc.repository;

import com.sps.shc.entity.ShcRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShcRecordRepository extends JpaRepository<ShcRecord, Long> {
}
