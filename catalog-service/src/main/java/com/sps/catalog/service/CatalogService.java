package com.sps.catalog.service;

import com.sps.catalog.dto.PlanResponse;
import com.sps.catalog.entity.PlanEntity;
import com.sps.catalog.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {
    private final PlanRepository planRepository;

    public CatalogService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanResponse> listPlanes() {
        return planRepository.findByActivoTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PlanResponse getPlan(Long id) {
        PlanEntity plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
        return toDto(plan);
    }

    private PlanResponse toDto(PlanEntity entity) {
        return new PlanResponse(entity.getId(), entity.getNombre(), entity.getDescripcion(), entity.getPrecio(), entity.getConvenio(), entity.isActivo());
    }
}
