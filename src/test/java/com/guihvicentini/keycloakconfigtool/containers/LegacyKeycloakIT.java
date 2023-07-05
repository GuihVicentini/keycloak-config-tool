package com.guihvicentini.keycloakconfigtool.containers;

import com.guihvicentini.keycloakconfigtool.facade.ConfigCommandFacade;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
public class LegacyKeycloakIT {


    public static final String TEST_REALM = "test";
    public static final GenericContainer<?> KEYCLOAK;
    private static final Network network = Network.newNetwork();

    @Autowired
    ConfigCommandFacade commandFacade;
    private static final String RESOURCES_INPUT = "src/test/resources/input/";
    private static final String NEW_REALM = "new-realm.json";
    private static final String TEST_REALM_JSON = "updated-test-realm.json";
    private static final String INPUT_NEW_REALM_JSON = RESOURCES_INPUT + NEW_REALM;
    private static final String INPUT_TEST_REALM_JSON = RESOURCES_INPUT + TEST_REALM_JSON;

    static {

        KEYCLOAK = new GenericContainer<>(DockerImageName
                .parse("quay.io/keycloak/keycloak:15.0.2"))
                .withNetwork(network)
                .withNetworkAliases("keycloak-test")
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
//                .withEnv("KEYCLOAK_CONTEXT_PATH", "/")
//                .withEnv("KEYCLOAK_HOME_DIR", "keycloak")
//                .withEnv("KEYCLOAK_IMPORT", "/opt/keycloak/data/import/test-realm.json")
//                .withEnv("KEYCLOAK_PROFILE_FEATURE_UPLOAD_SCRIPTS", "enabled")
                .withFileSystemBind("src/test/resources/realms/test-realm.json",
                        "/opt/keycloak/data/import/test-realm.json", BindMode.READ_ONLY)
                .withExposedPorts(8080, 8080)
                .withCommand("" +
                        "-Dkeycloak.import=/opt/keycloak/data/import/test-realm.json " +
                        "-Dkeycloak.profile.upload_script=enabled " +
                        "-Dkeycloak.home.dir=keycloak")
                .waitingFor(Wait.forHttp("/").forPort(8080));
        KEYCLOAK.start();

    }

    @DynamicPropertySource
    static void keycloakConfigProperties(DynamicPropertyRegistry registry){
        registry.add("keycloak.url", () -> String.format("http://localhost:%s", KEYCLOAK.getMappedPort(8080)));
    }

    @Test
    @Order(6)
    public void whenImport_and_RealmExists_thenUpdateRealm(){
        commandFacade.applyConfig(INPUT_TEST_REALM_JSON, "");
    }

    @Test
    @Order(7)
    public void whenImport_and_RealmDoesntExist_thenCreateRealm(){
        commandFacade.applyConfig(INPUT_NEW_REALM_JSON, "");
    }


}
