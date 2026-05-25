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
        if (planRepository.count() == 0) {
            // ── Servicios médicos base ───────────────────────────────────────────────
            ServicioMedicoEntity consultaGeneral = servicioMedicoRepository.save(
                    new ServicioMedicoEntity("Consulta General",          new BigDecimal("50000.00")));
            ServicioMedicoEntity laboratorio     = servicioMedicoRepository.save(
                    new ServicioMedicoEntity("Exámenes de Laboratorio",   new BigDecimal("30000.00")));
            ServicioMedicoEntity consultaEsp     = servicioMedicoRepository.save(
                    new ServicioMedicoEntity("Consulta con Especialista", new BigDecimal("80000.00")));
            ServicioMedicoEntity radiologia      = servicioMedicoRepository.save(
                    new ServicioMedicoEntity("Radiología y Diagnóstico",  new BigDecimal("48000.00")));
            ServicioMedicoEntity hospitalizacion = servicioMedicoRepository.save(
                    new ServicioMedicoEntity("Hospitalización",           new BigDecimal("151900.00")));

            // ── Plan Básico: Consulta General + Laboratorio  →  $80.000 ─────────────
            PlanEntity planBasico = new PlanEntity();
            planBasico.setNombre("Plan Básico");
            planBasico.setDescripcion("Cobertura esencial con consulta general y exámenes de laboratorio básicos.");
            planBasico.setConvenio("Convenio Nacional");
            planBasico.setActivo(true);
            planBasico.setServicios(List.of(consultaGeneral, laboratorio));
            planBasico.calculatePrice();   // precio = 80.000
            planRepository.save(planBasico);

            // ── Plan Avanzado: + Especialista + Radiología  →  $208.000 ─────────────
            PlanEntity planAvanzado = new PlanEntity();
            planAvanzado.setNombre("Plan Avanzado");
            planAvanzado.setDescripcion("Acceso a especialistas, laboratorio y radiología diagnóstica.");
            planAvanzado.setConvenio("Convenio Premium");
            planAvanzado.setActivo(true);
            planAvanzado.setServicios(List.of(consultaGeneral, laboratorio, consultaEsp, radiologia));
            planAvanzado.calculatePrice(); // precio = 208.000
            planRepository.save(planAvanzado);

            // ── Plan Familiar: todos los servicios  →  $359.900 ─────────────────────
            PlanEntity planFamiliar = new PlanEntity();
            planFamiliar.setNombre("Plan Familiar");
            planFamiliar.setDescripcion("Cobertura completa para toda la familia: incluye hospitalización y todos los especialistas.");
            planFamiliar.setConvenio("Convenio Familiar");
            planFamiliar.setActivo(true);
            planFamiliar.setServicios(List.of(consultaGeneral, laboratorio, consultaEsp, radiologia, hospitalizacion));
            planFamiliar.calculatePrice(); // precio = 359.900
            planRepository.save(planFamiliar);
        }
    }
}
