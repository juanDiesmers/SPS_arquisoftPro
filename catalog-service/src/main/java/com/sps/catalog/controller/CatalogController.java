package com.sps.catalog.controller;

import com.sps.catalog.dto.PlanResponse;
import com.sps.catalog.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planes")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> listPlanes() {
        return ResponseEntity.ok(catalogService.listPlanes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getPlan(id));
    }
}
