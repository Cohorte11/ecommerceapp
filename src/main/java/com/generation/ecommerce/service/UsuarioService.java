package com.generation.ecommerce.service;

import com.generation.ecommerce.model.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario guardarUsuario(Usuario newUser);

    List<Usuario> buscarUsuarios();
}