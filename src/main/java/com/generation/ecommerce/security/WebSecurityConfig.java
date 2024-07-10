package com.generation.ecommerce.security;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Indica que esta clase proporciona configuración para el contexto de la aplicación
@EnableMethodSecurity // Habilita la seguridad basada en métodos
@AllArgsConstructor
public class WebSecurityConfig {

    private UserDetailsServiceImpl userDetailsService;
    private AuthEntryPointJwt unauthorizedHandler;

    // Define un bean para el filtro de tokens de autenticación JWT
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    // Define un bean para el proveedor de autenticación DAO
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        // Configura el proveedor de autenticación con el servicio de detalles de usuario y el codificador de contraseñas
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    // Define un bean para el administrador de autenticación
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // Devuelve el administrador de autenticación desde la configuración de autenticación
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Define un bean para el codificador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Devuelve un codificador de contraseñas basado en BCrypt
        return new BCryptPasswordEncoder();
    }

    // Define un bean para la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable()) // Deshabilita la protección CSRF y CORS
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Configura el manejador de punto de entrada para errores de autenticación
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Configura la gestión de sesiones como sin estado
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/producto/lista").permitAll() // Permite el acceso a rutas específicas sin autenticación
                        .requestMatchers("/api/producto/nuevo").hasRole("ADMIN")
                        .anyRequest().authenticated() // Requiere autenticación para cualquier otra solicitud
                );
        http.authenticationProvider(authenticationProvider()); // Agrega el proveedor de autenticación al objeto HttpSecurity
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class); // Agrega el filtro de tokens JWT antes del filtro de autenticación de usuario y contraseña

        return http.build(); // Devuelve la cadena de filtros de seguridad HTTP configurada
    }
}