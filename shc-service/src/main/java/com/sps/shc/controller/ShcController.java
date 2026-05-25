package com.sps.shc.controller;

import com.sps.shc.entity.ShcRecord;
import com.sps.shc.repository.ShcRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shc")
public class ShcController {

    private final ShcRecordRepository repository;

    public ShcController(ShcRecordRepository repository) {
        this.repository = repository;
    }

    /** Lista todas las historias clínicas */
    @GetMapping("/historias")
    public ResponseEntity<List<ShcRecord>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    /** Obtiene la historia clínica de un cliente por su ID */
    @GetMapping("/historias/cliente/{clienteId}")
    public ResponseEntity<List<ShcRecord>> getByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(repository.findByClienteId(clienteId));
    }

    /** Obtiene la historia clínica de una compra específica */
    @GetMapping("/historias/compra/{compraId}")
    public ResponseEntity<List<ShcRecord>> getByCompra(@PathVariable Long compraId) {
        return ResponseEntity.ok(repository.findByCompraId(compraId));
    }
}
