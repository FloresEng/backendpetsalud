package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.security.ResourceAccessGuard;
import com.petsalud.backendpetsalud.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ResourceAccessGuard accessGuard;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<Venta> realizarVenta(
            @RequestParam Long usuarioId,
            @RequestBody List<Long> productosIds) {
        Long customerId = accessGuard.isCliente() ? accessGuard.currentUserId() : usuarioId;
        Venta nuevaVenta = ventaService.realizarVenta(customerId, productosIds);
        return ResponseEntity.ok(nuevaVenta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        accessGuard.requireVentaAccess(id);
        return ventaService.obtenerVentaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<List<Venta>> listarVentasPorUsuario(@PathVariable Long usuarioId) {
        accessGuard.requireUserAccess(usuarioId);
        Long customerId = accessGuard.isCliente() ? accessGuard.currentUserId() : usuarioId;
        List<Venta> ventas = ventaService.listarVentasPorUsuario(customerId);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VETERINARIO', 'ADMIN')")
    public ResponseEntity<List<Venta>> listarTodasLasVentas() {
        List<Venta> ventas = ventaService.listarTodasLasVentas();
        return ResponseEntity.ok(ventas);
    }
}
