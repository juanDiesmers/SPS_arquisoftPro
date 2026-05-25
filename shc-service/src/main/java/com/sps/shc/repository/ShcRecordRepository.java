package com.sps.shc.repository;

import com.sps.shc.entity.ShcRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShcRecordRepository extends JpaRepository<ShcRecord, Long> {
    List<ShcRecord> findByClienteId(Long clienteId);
    List<ShcRecord> findByCompraId(Long compraId);
}
