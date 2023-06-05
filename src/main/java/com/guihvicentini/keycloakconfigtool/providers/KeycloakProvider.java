package com.guihvicentini.keycloakconfigtool.providers;

import com.guihvicentini.keycloakconfigtool.properties.KeycloakConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@Slf4j
public class KeycloakProvider {

    private Keycloak keycloak;
    private final KeycloakConfigProperties properties;

    public KeycloakProvider(KeycloakConfigProperties properties) {
        this.properties = properties;
    }

    public Keycloak getInstance() {
        if (keycloak == null || keycloak.isClosed()) {
            log.info("Getting Keycloak Instance: {}", properties.getUrl());
            keycloak = getKeycloak();
        }
        return keycloak;
    }

    private Keycloak getKeycloak() {
        URL serverUrl = properties.getUrl();

        Keycloak keycloakInstance = getKeycloakInstance(serverUrl.toString());
        keycloakInstance.tokenManager().getAccessToken();

        return keycloakInstance;
    }

    private Keycloak getKeycloakInstance(String serverUrl) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(properties.getRealm())
                .clientId(properties.getClientId())
                .grantType(properties.getGrantType())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }
}
