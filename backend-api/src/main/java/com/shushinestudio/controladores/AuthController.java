package com.shushinestudio.controladores;

import com.shushinestudio.dtos.auth.UsuarioLogin;
import com.shushinestudio.dtos.auth.UsuarioPerfil;
import com.shushinestudio.dtos.auth.UsuarioRegistrar;
import com.shushinestudio.dtos.auth.UsuarioToken;
import com.shushinestudio.seguridad.servicios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints de autenticación JWT, registro de usuarios y perfil de Shushine Studio")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping({"/login", "/authenticate"})
    @Operation(summary = "Iniciar sesión y obtener token JWT", description = "Valida credenciales y emite un token JWT con los roles del usuario.")
    public ResponseEntity<UsuarioToken> login(@RequestBody UsuarioLogin loginRequest) {
        return ResponseEntity.ok(usuarioService.login(loginRequest));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Registra un usuario cliente en el sistema y retorna su token de sesión.")
    public ResponseEntity<UsuarioToken> registro(@RequestBody UsuarioRegistrar registroRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registro(registroRequest));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Retorna la información del usuario autenticado mediante su token JWT.")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of(
                    "status", 401,
                    "title", "Unauthorized",
                    "detail", "Token de autenticación ausente o inválido."
            ));
        }
        String login = authentication.getName();
        return ResponseEntity.ok(usuarioService.obtenerPerfil(login));
    }

    @PutMapping("/cambiar-clave")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cambiar contraseña del usuario autenticado", description = "Valida la contraseña actual y actualiza por la nueva contraseña codificada con BCrypt.")
    public ResponseEntity<?> cambiarClave(
            @jakarta.validation.Valid @RequestBody com.shushinestudio.dtos.auth.CambiarClaveDto dto,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of(
                    "status", 401,
                    "title", "Unauthorized",
                    "detail", "Token de autenticación ausente o inválido."
            ));
        }
        String login = authentication.getName();
        usuarioService.cambiarClave(login, dto);
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Contraseña actualizada exitosamente."));
    }

    @PutMapping("/perfil")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar datos de perfil del usuario", description = "Actualiza nombre, apellido, teléfono y correo del usuario autenticado.")
    public ResponseEntity<?> actualizarPerfil(
            @jakarta.validation.Valid @RequestBody com.shushinestudio.dtos.auth.ActualizarPerfilDto dto,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of(
                    "status", 401,
                    "title", "Unauthorized",
                    "detail", "Token de autenticación ausente o inválido."
            ));
        }
        String login = authentication.getName();
        UsuarioPerfil actualizado = usuarioService.actualizarPerfil(login, dto);
        return ResponseEntity.ok(actualizado);
    }
}

