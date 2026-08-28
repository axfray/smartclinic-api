package com.smartclinic.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-0123456789abcdef";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField("secret", TEST_SECRET);
        setField("expirationMs", 86400000L);
        jwtUtil.init();
    }

    @Test
    void generateToken_shouldProduceValidToken() {
        String token = jwtUtil.generateToken("juan@mail.com");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String token = jwtUtil.generateToken("juan@mail.com");

        assertEquals("juan@mail.com", jwtUtil.extractEmail(token));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenInvalid() {
        assertFalse(jwtUtil.validateToken("token-invalido"));
    }

    private void setField(String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = JwtUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        if (value instanceof Long) {
            field.setLong(jwtUtil, (Long) value);
        } else {
            field.set(jwtUtil, value);
        }
    }
}