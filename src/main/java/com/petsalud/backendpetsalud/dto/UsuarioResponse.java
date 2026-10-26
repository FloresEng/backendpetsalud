package com.petsalud.backendpetsalud.dto;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        String telefono,
        String direccion,
        Rol rol
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getRol()
        );
    }
}
