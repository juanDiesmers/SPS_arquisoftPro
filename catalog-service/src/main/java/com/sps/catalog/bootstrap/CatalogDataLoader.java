package com.sps.catalog.bootstrap;

import com.sps.catalog.entity.PlanEntity;
import com.sps.catalog.entity.ServicioMedicoEntity;
import com.sps.catalog.repository.PlanRepository;
import com.sps.catalog.repository.ServicioMedicoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CatalogDataLoader implements ApplicationRunner {

    private final PlanRepository planRepository;
    private final ServicioMedicoRepository servicioMedicoRepository;

    public CatalogDataLoader(PlanRepository planRepository, ServicioMedicoRepository servicioMedicoRepository) {
        this.planRepository = planRepository;
        this.servicioMedicoRepository = servicioMedicoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (planRepository.count() == 0 && servicioMedicoRepository.count() == 0) {
            // Seed services for Basic
            ServicioMedicoEntity consultaGeneral = servicioMedicoRepository.save(new ServicioMedicoEntity("Consulta General", new BigDecimal("50000.00")));
            ServicioMedicoEntity examenesLab = servicioMedicoRepository.save(new ServicioMedicoEntity("Exámenes de Laboratorio", new BigDecimal("30000.00")));
            ServicioMedicoEntity hospBasica = servicioMedicoRepository.save(new ServicioMedicoEntity("Hospitalización Básica", new BigDecimal("49900.00")));

            // Seed services for Advanced
            ServicioMedicoEntity consultaEsp = servicioMedicoRepository.save(new ServicioMedicoEntity("Consulta con Especialista", new BigDecimal("100000.00")));
            ServicioMedicoEntity examenesAvanzados = servicioMedicoRepository.save(new ServicioMedicoEntity("Exámenes Avanzados", new BigDecimal("80000.00")));
            ServicioMedicoEntity hospEspecializada = servicioMedicoRepository.save(new ServicioMedicoEntity("Hospitalización Especializada", new BigDecimal("99900.00")));

            // Seed services for Family
            ServicioMedicoEntity coberturaFam = servicioMedicoRepository.save(new ServicioMedicoEntity("Cobertura Médica Familiar", new BigDecimal("399900.00")));

            // Seed Plan Básico
            PlanEntity planBasico = new PlanEntity("Plan Básico", "Cobertura de consulta general y laboratorio.", BigDecimal.ZERO, "Convenio Nacional", true);
            planBasico.setServicios(List.of(consultaGeneral, examenesLab, hospBasica));
            planRepository.save(planBasico);

            // Seed Plan Avanzado
            PlanEntity planAvanzado = new PlanEntity("Plan Avanzado", "Cobertura de especialistas y exámenes avanzados.", BigDecimal.ZERO, "Convenio Premium", true);
            planAvanzado.setServicios(List.of(consultaEsp, examenesAvanzados, hospEspecializada));
            planRepository.save(planAvanzado);

            // Seed Plan Familiar
            PlanEntity planFamiliar = new PlanEntity("Plan Familiar", "Cobertura para hasta 4 miembros de la familia.", BigDecimal.ZERO, "Convenio Familiar", true);
            planFamiliar.setServicios(List.of(coberturaFam));
            planRepository.save(planFamiliar);
        }
    }
}
