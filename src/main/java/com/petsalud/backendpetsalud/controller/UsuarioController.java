package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.UsuarioRegistroRequest;
import com.petsalud.backendpetsalud.dto.UsuarioResponse;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.security.ResourceAccessGuard;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ResourceAccessGuard accessGuard;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrarUsuario(@RequestBody UsuarioRegistroRequest request) {
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(request.password())
                .nombre(request.nombre())
                .apellido(request.apellido())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .build();
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return ResponseEntity.ok(UsuarioResponse.from(nuevoUsuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        accessGuard.requireUserAccess(id);
        return usuarioService.obtenerUsuarioPorId(id)
                .map(UsuarioResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMIN')")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        accessGuard.requireUserAccess(id);
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        return ResponseEntity.ok(UsuarioResponse.from(usuarioActualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios.stream().map(UsuarioResponse::from).toList());
    }
}
