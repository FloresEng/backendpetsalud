package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.security.ResourceAccessGuard;
import com.petsalud.backendpetsalud.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @Autowired
    private ResourceAccessGuard accessGuard;

    @PostMapping("/propietario/{propietarioId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<Mascota> registrarMascota(@PathVariable Long propietarioId, @RequestBody Mascota mascota) {
        Long ownerId = accessGuard.isCliente() ? accessGuard.currentUserId() : propietarioId;
        mascota.setId(null);
        mascota.setCitas(null);
        mascota.setExpedientesMedicos(null);
        Mascota nuevaMascota = mascotaService.registrarMascota(mascota, ownerId);
        return ResponseEntity.ok(nuevaMascota);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<Mascota> obtenerMascotaPorId(@PathVariable Long id) {
        accessGuard.requireMascotaAccess(id);
        return mascotaService.obtenerMascotaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<Mascota> actualizarMascota(@PathVariable Long id, @RequestBody Mascota mascota) {
        accessGuard.requireMascotaAccess(id);
        Mascota mascotaActualizada = mascotaService.actualizarMascota(id, mascota);
        return ResponseEntity.ok(mascotaActualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Long id) {
        accessGuard.requireMascotaAccess(id);
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/propietario/{propietarioId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<List<Mascota>> listarMascotasPorPropietario(@PathVariable Long propietarioId) {
        Long ownerId = accessGuard.isCliente() ? accessGuard.currentUserId() : propietarioId;
        List<Mascota> mascotas = mascotaService.listarMascotasPorPropietario(ownerId);
        return ResponseEntity.ok(mascotas);
    }
}
