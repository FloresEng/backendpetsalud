package com.petsalud.backendpetsalud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.dto.UsuarioResponse;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.security.UsuarioDetails;
import com.petsalud.backendpetsalud.dto.UsuarioResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceImplTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);
    }

    @Test
    void registraConPasswordCodificadaYRolClienteAunqueSeSoliciteOtroRol() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = Usuario.builder()
                .email("cliente@petsalud.cl")
                .password("una-clave-segura")
                .rol(Rol.ADMIN)
                .build();

        Usuario registrado = usuarioService.registrarUsuario(usuario);

        assertNotEquals("una-clave-segura", registrado.getPassword());
        assertTrue(passwordEncoder.matches("una-clave-segura", registrado.getPassword()));
        assertEquals(Rol.CLIENTE, registrado.getRol());
    }

    @Test
    void respuestaDeUsuarioNoIncluyeLaPassword() throws Exception {
        UsuarioResponse response = new UsuarioResponse(
                1L,
                "cliente@petsalud.cl",
                "Ana",
                "Pérez",
                null,
                null,
                Rol.CLIENTE
        );

        String json = new ObjectMapper().writeValueAsString(response);

        assertTrue(json.contains("\"email\":\"cliente@petsalud.cl\""));
        assertFalse(json.contains("password"));
    }

    @Test
    void authoritiesDeUsuarioCoincidenConSusRoles() {
        for (Rol rol : Rol.values()) {
            Usuario usuario = Usuario.builder()
                    .email("usuario@petsalud.cl")
                    .password("hash")
                    .rol(rol)
                    .build();

            Set<String> authorities = new UsuarioDetails(usuario).getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(java.util.stream.Collectors.toSet());

            assertEquals(Set.of(rol.name()), authorities);
        }
    }
}
