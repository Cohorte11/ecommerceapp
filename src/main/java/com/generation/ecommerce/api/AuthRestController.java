package com.generation.ecommerce.api;

import com.generation.ecommerce.DTO.LoginDTO;
import com.generation.ecommerce.DTO.RegistroDTO;
import com.generation.ecommerce.model.ERol;
import com.generation.ecommerce.model.Rol;
import com.generation.ecommerce.model.Usuario;
import com.generation.ecommerce.security.JwtUtils;
import com.generation.ecommerce.security.UserDetailsImpl;
import com.generation.ecommerce.service.RolService;
import com.generation.ecommerce.service.UsuarioServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;

    private final UsuarioServiceImpl usuarioService;

    private final RolService rolService;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    @GetMapping("/lista")
    public ResponseEntity<List<Usuario>> listaDeUsuarios() {
        return new ResponseEntity<>(usuarioService.buscarUsuarios(), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginRequest) {

        // Autentica al usuario utilizando el gestor de autenticación de Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Establece la autenticación en el contexto de seguridad de Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtiene los detalles del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Genera un token JWT para el usuario autenticado
        String jwtToken = jwtUtils.generateJwtToken(userDetails);

        // Retorna una respuesta exitosa con el token JWT y detalles del usuario
        return new ResponseEntity<>(new JwtResponse(jwtToken, userDetails.getUsername(), userDetails.getEmail()), HttpStatus.OK);
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistroDTO solicitudRegistro) {

        // Verifica si el nombre de usuario ya está en uso
        if (usuarioService.existeUsuarioPorUsername(solicitudRegistro.getUsername())) {
            return ResponseEntity.badRequest().body(new String("Error: El username ya está en uso!"));
        }

        // Verifica si el correo electrónico ya está en uso
        if (usuarioService.existeUsuarioPorEmail(solicitudRegistro.getEmail())) {
            return ResponseEntity.badRequest().body(new String("Error: El email ya está en uso"));
        }

        // Crea una nueva cuenta de usuario
        Usuario usuario = Usuario.builder()
                .username(solicitudRegistro.getUsername())
                .email(solicitudRegistro.getEmail())
                .password(passwordEncoder.encode(solicitudRegistro.getPassword())) // Codifica la contraseña antes de almacenarla
                .build();

        // Obtiene los roles seleccionados por el usuario y los asigna al nuevo usuario
        Set<String> strRoles = solicitudRegistro.getRoles();
        Set<Rol> roles = new HashSet<>();
        if (strRoles == null) {
            Rol rolUsuario = rolService.buscarRolPorNombre(ERol.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
            roles.add(rolUsuario);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Rol rolAdmin = rolService.buscarRolPorNombre(ERol.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(rolAdmin);
                        break;
                    default:
                        Rol rolUsuario= rolService.buscarRolPorNombre(ERol.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(rolUsuario);
                }
            });
        }

        // Asigna los roles al usuario y guarda el usuario en la base de datos
        usuario.setRoles(roles);
        usuarioService.guardarUsuario(usuario);

        // Retorna una respuesta exitosa indicando que el usuario se registró correctamente
        return new ResponseEntity(new String("Usuario registrado exitosamente!"), HttpStatus.CREATED);
    }


    // Clase interna que representa la respuesta del token JWT
    @AllArgsConstructor
    @Getter
    @Setter
    public class JwtResponse {
        private String token;
        private String username;
        private String email;
    }
}
