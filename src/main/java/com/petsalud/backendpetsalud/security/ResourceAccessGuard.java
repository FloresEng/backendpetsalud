package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.entity.ExpedienteMedico;
import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Notificacion;
import com.petsalud.backendpetsalud.entity.Recompensa;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.ExpedienteMedicoRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.RecompensaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ResourceAccessGuard {

    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final ExpedienteMedicoRepository expedienteMedicoRepository;
    private final VentaRepository ventaRepository;
    private final NotificacionRepository notificacionRepository;
    private final RecompensaRepository recompensaRepository;

    public ResourceAccessGuard(
            UsuarioRepository usuarioRepository,
            MascotaRepository mascotaRepository,
            CitaRepository citaRepository,
            ExpedienteMedicoRepository expedienteMedicoRepository,
            VentaRepository ventaRepository,
            NotificacionRepository notificacionRepository,
            RecompensaRepository recompensaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.expedienteMedicoRepository = expedienteMedicoRepository;
        this.ventaRepository = ventaRepository;
        this.notificacionRepository = notificacionRepository;
        this.recompensaRepository = recompensaRepository;
    }

    public boolean isCliente() {
        return currentAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("CLIENTE"));
    }

    public boolean isVeterinario() {
        return currentAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("VETERINARIO"));
    }

    public Long currentUserId() {
        return usuarioRepository.findByEmail(currentAuthentication().getName())
                .map(Usuario::getId)
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
    }

    public void requireUserAccess(Long userId) {
        if (isCliente() && !currentUserId().equals(userId)) {
            deny();
        }
    }

    public void requireMascotaAccess(Long mascotaId) {
        if (isCliente()) {
            Mascota mascota = mascotaRepository.findById(mascotaId)
                    .orElseThrow(ResourceAccessGuard::deniedException);
            requireUserAccess(mascota.getPropietario().getId());
        }
    }

    public void requireCitaAccess(Long citaId) {
        if (isCliente()) {
            Cita cita = citaRepository.findById(citaId)
                    .orElseThrow(ResourceAccessGuard::deniedException);
            requireMascotaAccess(cita.getMascota().getId());
        }
    }

    public void requireExpedienteAccess(Long expedienteId) {
        if (isCliente()) {
            ExpedienteMedico expediente = expedienteMedicoRepository.findById(expedienteId)
                    .orElseThrow(ResourceAccessGuard::deniedException);
            requireMascotaAccess(expediente.getMascota().getId());
        }
    }

    public void requireVentaAccess(Long ventaId) {
        if (isCliente()) {
            Venta venta = ventaRepository.findById(ventaId)
                    .orElseThrow(ResourceAccessGuard::deniedException);
            requireUserAccess(venta.getUsuario().getId());
        }
    }

    public void requireNotificacionAccess(Long notificacionId) {
        if (isCliente()) {
            Notificacion notificacion = notificacionRepository.findById(notificacionId)
                    .orElseThrow(ResourceAccessGuard::deniedException);
            requireUserAccess(notificacion.getUsuario().getId());
        }
    }

    public void requireRecompensaAccess(Long recompensaId) {
        if (isCliente()) {
            Recompensa recompensa = recompensaRepository.findById(recompensaId)
                    .orElseThrow(ResourceAccessGuard::deniedException);
            requireUserAccess(recompensa.getUsuario().getId());
        }
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw deniedException();
        }
        return authentication;
    }

    private static void deny() {
        throw deniedException();
    }

    private static AccessDeniedException deniedException() {
        return new AccessDeniedException("No tienes permiso para acceder a este recurso");
    }
}
