package com.sps.sam.controller;

import com.sps.sam.entity.SamRecord;
import com.sps.sam.repository.SamRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sam")
public class SamController {

    private final SamRecordRepository repository;

    public SamController(SamRecordRepository repository) {
        this.repository = repository;
    }

    /** Lista todas las agendas médicas */
    @GetMapping("/agendas")
    public ResponseEntity<List<SamRecord>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    /** Obtiene las agendas de un cliente por su ID */
    @GetMapping("/agendas/cliente/{clienteId}")
    public ResponseEntity<List<SamRecord>> getByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(repository.findByClienteId(clienteId));
    }

    /** Obtiene la agenda de una compra específica */
    @GetMapping("/agendas/compra/{compraId}")
    public ResponseEntity<List<SamRecord>> getByCompra(@PathVariable Long compraId) {
        return ResponseEntity.ok(repository.findByCompraId(compraId));
    }
}
