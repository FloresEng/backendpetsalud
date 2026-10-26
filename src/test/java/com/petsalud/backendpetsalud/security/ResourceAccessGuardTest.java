package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.ExpedienteMedicoRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.RecompensaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceAccessGuardTest {

    private UsuarioRepository usuarioRepository;
    private MascotaRepository mascotaRepository;
    private ResourceAccessGuard accessGuard;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        mascotaRepository = mock(MascotaRepository.class);
        accessGuard = new ResourceAccessGuard(
                usuarioRepository,
                mascotaRepository,
                mock(CitaRepository.class),
                mock(ExpedienteMedicoRepository.class),
                mock(VentaRepository.class),
                mock(NotificacionRepository.class),
                mock(RecompensaRepository.class)
        );

        Usuario cliente = Usuario.builder().id(10L).email("cliente@petsalud.cl").build();
        when(usuarioRepository.findByEmail("cliente@petsalud.cl")).thenReturn(Optional.of(cliente));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "cliente@petsalud.cl",
                        "password",
                        List.of(new SimpleGrantedAuthority("CLIENTE"))
                )
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clienteSoloPuedeAccederASuUsuario() {
        assertDoesNotThrow(() -> accessGuard.requireUserAccess(10L));
        assertThrows(AccessDeniedException.class, () -> accessGuard.requireUserAccess(11L));
    }

    @Test
    void clienteSoloPuedeAccederASusMascotas() {
        Mascota propia = Mascota.builder()
                .id(20L)
                .propietario(Usuario.builder().id(10L).build())
                .build();
        Mascota ajena = Mascota.builder()
                .id(21L)
                .propietario(Usuario.builder().id(11L).build())
                .build();
        when(mascotaRepository.findById(20L)).thenReturn(Optional.of(propia));
        when(mascotaRepository.findById(21L)).thenReturn(Optional.of(ajena));

        assertDoesNotThrow(() -> accessGuard.requireMascotaAccess(20L));
        assertThrows(AccessDeniedException.class, () -> accessGuard.requireMascotaAccess(21L));
    }
}
