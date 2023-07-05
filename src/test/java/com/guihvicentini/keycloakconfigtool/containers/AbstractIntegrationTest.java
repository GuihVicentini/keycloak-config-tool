package com.guihvicentini.keycloakconfigtool.containers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractIntegrationTest {

    public static final String TEST_REALM = "test";
    public static final String UUID_MATCH = "^([0-9a-f]{8}-?[0-9a-f]{4}-?4[0-9a-f]{3}-?[89ab][0-9a-f]{3}-?[0-9a-f]{12}$).*";
    public static final GenericContainer<?> KEYCLOAK;
    public static final GenericContainer<?> LDAP;

    private static final Network network = Network.newNetwork();

    static {
        KEYCLOAK = new GenericContainer<>(DockerImageName
                .parse("quay.io/keycloak/keycloak:20.0.0"))
                .withNetwork(network)
                .withNetworkAliases("keycloak-test")
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                .withEnv("KEYCLOAK_CONTEXT_PATH", "/")
                .withFileSystemBind("src/test/resources/realms/test-realm.json",
                        "/opt/keycloak/data/import/test-realm.json", BindMode.READ_ONLY)
                .withExposedPorts(8080, 8443)
                .withCommand("start-dev --import-realm")
                .waitingFor(Wait.forHttp("/").forPort(8080));
        KEYCLOAK.start();

        LDAP = new GenericContainer<>(DockerImageName
                .parse("bitnami/openldap:latest"))
                .withNetwork(network)
                .withNetworkAliases("openldap")
                .withEnv("LDAP_ADMIN_USERNAME", "admin")
                .withEnv("LDAP_ADMIN_PASSWORD", "adminpassword");
//        LDAP.start();
    }

    @DynamicPropertySource
    static void keycloakConfigProperties(DynamicPropertyRegistry registry){
        registry.add("keycloak.url", () -> String.format("http://localhost:%s", KEYCLOAK.getMappedPort(8080)));
    }

}
