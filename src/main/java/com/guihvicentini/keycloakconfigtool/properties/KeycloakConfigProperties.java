package com.guihvicentini.keycloakconfigtool.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.validation.annotation.Validated;

import java.net.URL;


@ConfigurationProperties(prefix = "keycloak")
@ConfigurationPropertiesScan
@Validated
@Getter
public class KeycloakConfigProperties {

    @NotNull
    private URL url;
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    private String realm = "master";
    private String clientId = "admin-cli";
    private String grantType = "password";

    public KeycloakConfigProperties(URL url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }


}
