package com.shushinestudio.seguridad;

import com.shushinestudio.seguridad.modelos.Rol;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.servicios.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Clave HMAC-SHA512 en Base64 oficial del proyecto
        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "Y/I5T3T1aSOP+BezsicbSiUtzIZWxdcOVnylSYlJl/H22uPQID8VGjfzc5U5cVhKA6qm17V2yMP/mlrlTIkb+g=="
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        Rol rolCliente = Rol.builder()
                .id(2)
                .nombre("CLIENTE")
                .descripcion("Cliente Shushine")
                .build();

        testUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .login("camila_test")
                .nombre("Camila")
                .apellido("Calderon")
                .clave("encoded_pass")
                .rol(rolCliente)
                .activo(true)
                .build();
    }

    @Test
    void testGenerarToken_Exitoso() {
        String token = jwtService.getToken(testUsuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.split("\\.").length == 3, "El token JWT debe tener 3 partes (header.payload.signature)");
    }

    @Test
    void testExtraerUsernameDeToken() {
        String token = jwtService.getToken(testUsuario);
        String username = jwtService.getUsernameFromToken(token);

        assertEquals("camila_test", username);
    }

    @Test
    void testValidarToken_Valido() {
        String token = jwtService.getToken(testUsuario);
        boolean isValid = jwtService.isTokenValid(token, testUsuario);

        assertTrue(isValid);
        assertFalse(jwtService.isTokenExpired(token));
    }
}
