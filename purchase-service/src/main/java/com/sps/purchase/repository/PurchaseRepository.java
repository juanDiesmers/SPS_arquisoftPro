package com.sps.purchase.repository;

import com.sps.purchase.entity.PurchaseEntity;
import com.sps.purchase.entity.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {
    List<PurchaseEntity> findByEstado(PurchaseStatus estado);
    List<PurchaseEntity> findByClienteIdOrderByCreatedAtDesc(Long clienteId);
}
