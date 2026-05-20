package com.sps.catalog.bootstrap;

import com.sps.catalog.entity.PlanEntity;
import com.sps.catalog.repository.PlanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CatalogDataLoader implements ApplicationRunner {

    private final PlanRepository planRepository;

    public CatalogDataLoader(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (planRepository.count() == 0) {
            planRepository.save(new PlanEntity("Plan Básico", "Cobertura de consulta general y laboratorio.", new BigDecimal("129900.00"), "Convenio Nacional", true));
            planRepository.save(new PlanEntity("Plan Avanzado", "Cobertura de especialistas y exámenes avanzados.", new BigDecimal("279900.00"), "Convenio Premium", true));
            planRepository.save(new PlanEntity("Plan Familiar", "Cobertura para hasta 4 miembros de la familia.", new BigDecimal("399900.00"), "Convenio Familiar", true));
        }
    }
}
