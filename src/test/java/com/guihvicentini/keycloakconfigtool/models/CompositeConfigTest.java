package com.guihvicentini.keycloakconfigtool.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompositeConfigTest {

    public static final String TEST_CLIENT = "test-client";
    public static final String ROLE_A = "role-a";
    public static final String ROLE_Z = "role-z";
    private CompositeConfig config;

    private Set<String> realm;
    private Map<String, List<String>> client;

    @BeforeEach
    public void setup(){
        config = new CompositeConfig();

        realm = new HashSet<>();
        realm.add("master");

        List<String> roles= new ArrayList<>();
        roles.add(ROLE_A);
        roles.add(ROLE_Z);

        client = new HashMap<>();
        client.put(TEST_CLIENT, roles);
    }

    @Test
    public void whenPropertiesNull_normalize_thenEmptyCollection(){
        config.normalize();
        assertEquals(Collections.emptySet(), config.getRealm());
        assertEquals(Collections.emptyMap(), config.getClient());
    }

    @Test
    public void whenPropertiesNotNull_normalize_thenReturnProperties(){
        config.setRealm(realm);
        config.setClient(client);
        config.normalize();
        assertEquals(realm, config.getRealm());
        assertEquals(client, config.getClient());
        String actualFirstRole = config.getClient().get(TEST_CLIENT).get(0);
        assertEquals(ROLE_A, actualFirstRole);
    }


}
