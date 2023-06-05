package com.guihvicentini.keycloakconfigtool.models;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class IdentityProviderConfigTest {

    @Test
    public void whenConfigNull_normalize_thenExceptEmptyMap(){
        IdentityProviderConfig idp = new IdentityProviderConfig();
        assertNull(idp.getConfig());
        idp.normalize();
        assertEquals(Collections.emptyMap(), idp.getConfig());
    }

}
