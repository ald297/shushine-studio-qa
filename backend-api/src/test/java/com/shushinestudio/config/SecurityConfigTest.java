package com.shushinestudio.config;

import com.shushinestudio.seguridad.configuracion.JwtAuthenticationFilter;
import com.shushinestudio.seguridad.servicios.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@Import({SecurityConfig.class, SecurityConfigTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.jwt.secret-key=Y/I5T3T1aSOP+BezsicbSiUtzIZWxdcOVnylSYlJl/H22uPQID8VGjfzc5U5cVhKA6qm17V2yMP/mlrlTIkb+g=="
})
public class SecurityConfigTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtService jwtService() {
            return new JwtService();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return username -> null;
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
            return new AuthenticationProvider() {
                @Override
                public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                    return authentication;
                }

                @Override
                public boolean supports(Class<?> authentication) {
                    return true;
                }
            };
        }
    }

    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;

    @Test
    void testSecurityFilterChainLoadsWithoutErrors() {
        assertNotNull(securityFilterChain, "SecurityFilterChain debe inicializarse correctamente sin lanzar excepciones de prefijo de roles.");
    }
}
