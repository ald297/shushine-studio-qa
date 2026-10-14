package com.shushinestudio.seguridad.servicios;

import com.shushinestudio.dtos.auth.UsuarioLogin;
import com.shushinestudio.dtos.auth.UsuarioPerfil;
import com.shushinestudio.dtos.auth.UsuarioRegistrar;
import com.shushinestudio.dtos.auth.UsuarioToken;
import com.shushinestudio.seguridad.modelos.Rol;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.shushinestudio.repositorios.IClienteRepository clienteRepository;

    @Autowired
    private RolService rolService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    public UsuarioToken login(UsuarioLogin loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getClave())
        );

        Usuario usuario = usuarioRepository.findByLogin(loginRequest.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales no válidas para el usuario: " + loginRequest.getLogin()));

        String token = jwtService.getToken(usuario);

        return UsuarioToken.builder()
                .token(token)
                .id(usuario.getId())
                .login(usuario.getLogin())
                .nombre(usuario.getNombre() + (usuario.getApellido() != null ? " " + usuario.getApellido() : ""))
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : "CLIENTE")
                .build();
    }

    @Transactional
    public UsuarioToken registro(UsuarioRegistrar registroRequest) {
        if (usuarioRepository.existsByLogin(registroRequest.getLogin())) {
            throw new IllegalArgumentException("El nombre de usuario '" + registroRequest.getLogin() + "' ya se encuentra registrado.");
        }

        Rol rol;
        if (registroRequest.getRolId() != null) {
            rol = rolService.obtenerPorId(registroRequest.getRolId());
            if (rol == null) {
                throw new IllegalArgumentException("El rol especificado con ID " + registroRequest.getRolId() + " no existe.");
            }
        } else {
            rol = rolService.obtenerPorNombre("CLIENTE")
                    .orElseThrow(() -> new IllegalStateException("El rol por defecto CLIENTE no está configurado en el sistema."));
        }

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(registroRequest.getNombre())
                .apellido(registroRequest.getApellido())
                .telefono(registroRequest.getTelefono())
                .login(registroRequest.getLogin())
                .clave(passwordEncoder.encode(registroRequest.getClave()))
                .rol(rol)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // Auto-crear entidad Cliente si el rol es CLIENTE para persistencia íntegra
        if (rol.getNombre() != null && rol.getNombre().equalsIgnoreCase("CLIENTE")) {
            com.shushinestudio.modelos.Cliente nuevoCliente = com.shushinestudio.modelos.Cliente.builder()
                    .usuario(usuarioGuardado)
                    .nombreWalkin(usuarioGuardado.getNombre() + (usuarioGuardado.getApellido() != null ? " " + usuarioGuardado.getApellido() : ""))
                    .telefonoWalkin(usuarioGuardado.getTelefono())
                    .nivelFidelidad("Bronce")
                    .puntosAcumulados(0)
                    .esWalkin(false)
                    .build();
            clienteRepository.save(nuevoCliente);
        }

        String token = jwtService.getToken(usuarioGuardado);

        return UsuarioToken.builder()
                .token(token)
                .id(usuarioGuardado.getId())
                .login(usuarioGuardado.getLogin())
                .nombre(usuarioGuardado.getNombre() + (usuarioGuardado.getApellido() != null ? " " + usuarioGuardado.getApellido() : ""))
                .rol(usuarioGuardado.getRol().getNombre())
                .build();
    }

    @Transactional(readOnly = true)
    public UsuarioPerfil obtenerPerfil(String login) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con login: " + login));

        UsuarioPerfil perfil = UsuarioPerfil.builder()
                .id(usuario.getId())
                .login(usuario.getLogin())
                .correo(usuario.getCorreo() != null ? usuario.getCorreo() : (usuario.getLogin() != null && usuario.getLogin().contains("@") ? usuario.getLogin() : usuario.getLogin() + "@shushinestudio.com"))
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .activo(usuario.getActivo())
                .puntosAcumulados(0)
                .nivelFidelidad("Bronce")
                .build();

        clienteRepository.findByUsuarioId(usuario.getId()).ifPresent(cliente -> {
            if (cliente.getPuntosAcumulados() != null) {
                perfil.setPuntosAcumulados(cliente.getPuntosAcumulados());
            }
            if (cliente.getNivelFidelidad() != null) {
                perfil.setNivelFidelidad(cliente.getNivelFidelidad());
            }
            perfil.setTipoCabello(cliente.getTipoCabello());
            perfil.setNotasPreferencias(cliente.getNotasPreferencias());
        });

        return perfil;
    }

    @Transactional
    public void cambiarClave(String login, com.shushinestudio.dtos.auth.CambiarClaveDto dto) {
        if (dto.getNuevaClave() == null || dto.getNuevaClave().isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía.");
        }
        if (!dto.getNuevaClave().equals(dto.getConfirmarClave())) {
            throw new IllegalArgumentException("La nueva contraseña y la confirmación no coinciden.");
        }
        if (dto.getNuevaClave().length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + login));

        if (!passwordEncoder.matches(dto.getClaveActual(), usuario.getClave())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        if (passwordEncoder.matches(dto.getNuevaClave(), usuario.getClave())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la contraseña actual.");
        }

        usuario.setClave(passwordEncoder.encode(dto.getNuevaClave()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioPerfil actualizarPerfil(String login, com.shushinestudio.dtos.auth.ActualizarPerfilDto dto) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + login));

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }

        usuario.setNombre(dto.getNombre().trim());
        if (dto.getApellido() != null) {
            usuario.setApellido(dto.getApellido().trim());
        }
        if (dto.getTelefono() != null) {
            usuario.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()) {
            usuario.setCorreo(dto.getCorreo().trim());
        }

        usuario.setNombreCompleto((usuario.getNombre() != null ? usuario.getNombre() : "") + 
                                  (usuario.getApellido() != null ? " " + usuario.getApellido() : ""));

        Usuario guardado = usuarioRepository.save(usuario);

        return UsuarioPerfil.builder()
                .id(guardado.getId())
                .login(guardado.getLogin())
                .correo(guardado.getCorreo())
                .nombre(guardado.getNombre())
                .apellido(guardado.getApellido())
                .telefono(guardado.getTelefono())
                .rol(guardado.getRol() != null ? guardado.getRol().getNombre() : null)
                .activo(guardado.getActivo())
                .build();
    }
}

