package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.ExpedienteMedico;
import com.petsalud.backendpetsalud.security.ResourceAccessGuard;
import com.petsalud.backendpetsalud.service.ExpedienteMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expedientes")
public class ExpedienteMedicoController {

    @Autowired
    private ExpedienteMedicoService expedienteMedicoService;

    @Autowired
    private ResourceAccessGuard accessGuard;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('VETERINARIO', 'ADMIN')")
    public ResponseEntity<ExpedienteMedico> agregarRegistro(
            @RequestParam Long mascotaId,
            @RequestParam Long veterinarioId,
            @RequestBody ExpedienteMedico registro) {
        Long authorId = accessGuard.isVeterinario() ? accessGuard.currentUserId() : veterinarioId;
        registro.setId(null);
        registro.setMascota(null);
        registro.setVeterinario(null);
        ExpedienteMedico nuevoRegistro = expedienteMedicoService.agregarRegistro(mascotaId, authorId, registro);
        return ResponseEntity.ok(nuevoRegistro);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VETERINARIO', 'ADMIN')")
    public ResponseEntity<ExpedienteMedico> obtenerRegistroPorId(@PathVariable Long id) {
        return expedienteMedicoService.obtenerRegistroPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mascota/{mascotaId}")
    @PreAuthorize("hasAnyAuthority('VETERINARIO', 'ADMIN')")
    public ResponseEntity<List<ExpedienteMedico>> listarRegistrosPorMascota(@PathVariable Long mascotaId) {
        List<ExpedienteMedico> registros = expedienteMedicoService.listarRegistrosPorMascota(mascotaId);
        return ResponseEntity.ok(registros);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VETERINARIO', 'ADMIN')")
    public ResponseEntity<Void> eliminarRegistro(@PathVariable Long id) {
        expedienteMedicoService.eliminarRegistro(id);
        return ResponseEntity.noContent().build();
    }
}
