package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Notificacion;
import com.petsalud.backendpetsalud.security.ResourceAccessGuard;
import com.petsalud.backendpetsalud.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private ResourceAccessGuard accessGuard;

    @PostMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Notificacion> enviarNotificacion(@PathVariable Long usuarioId, @RequestBody Notificacion notificacion) {
        Notificacion nuevaNotificacion = notificacionService.enviarNotificacion(usuarioId, notificacion);
        return ResponseEntity.ok(nuevaNotificacion);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<Notificacion> obtenerNotificacionPorId(@PathVariable Long id) {
        accessGuard.requireNotificacionAccess(id);
        return notificacionService.obtenerNotificacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<Notificacion>> listarNotificacionesPorUsuario(@PathVariable Long usuarioId) {
        accessGuard.requireUserAccess(usuarioId);
        Long targetUserId = accessGuard.isCliente() ? accessGuard.currentUserId() : usuarioId;
        List<Notificacion> notificaciones = notificacionService.listarNotificacionesPorUsuario(targetUserId);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<List<Notificacion>> listarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        accessGuard.requireUserAccess(usuarioId);
        Long targetUserId = accessGuard.isCliente() ? accessGuard.currentUserId() : usuarioId;
        List<Notificacion> notificaciones = notificacionService.listarNotificacionesNoLeidas(targetUserId);
        return ResponseEntity.ok(notificaciones);
    }

    @PutMapping("/{id}/leida")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        accessGuard.requireNotificacionAccess(id);
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        accessGuard.requireNotificacionAccess(id);
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}
