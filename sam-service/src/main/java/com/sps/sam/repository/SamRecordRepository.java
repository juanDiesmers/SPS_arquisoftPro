package com.sps.sam.repository;

import com.sps.sam.entity.SamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SamRecordRepository extends JpaRepository<SamRecord, Long> {
}
