package com.generation.ecommerce.service;

import com.generation.ecommerce.model.Usuario;
import com.generation.ecommerce.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class UsuarioServiceImpl implements UsuarioService{

    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario guardarUsuario(Usuario nuevoUsuario) {
        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    public List<Usuario> buscarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Boolean existeUsuarioPorUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public Boolean existeUsuarioPorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}