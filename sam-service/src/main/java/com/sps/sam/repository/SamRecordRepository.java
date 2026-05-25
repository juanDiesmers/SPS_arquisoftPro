package com.sps.sam.repository;

import com.sps.sam.entity.SamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SamRecordRepository extends JpaRepository<SamRecord, Long> {
    List<SamRecord> findByClienteId(Long clienteId);
    List<SamRecord> findByCompraId(Long compraId);
}
