package com.sps.catalog.repository;

import com.sps.catalog.entity.ServicioMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioMedicoRepository extends JpaRepository<ServicioMedicoEntity, Long> {
}
