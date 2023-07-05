package com.guihvicentini.keycloakconfigtool.containers;

import com.guihvicentini.keycloakconfigtool.facade.ConfigCommandFacade;
import org.junit.jupiter.api.Disabled;
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
    public static final GenericContainer<?> KEYCLOAK_15;
    private static final Network network = Network.newNetwork();

    @Autowired
    ConfigCommandFacade commandFacade;
    private static final String RESOURCES_INPUT = "src/test/resources/input/";
    private static final String NEW_REALM = "new-realm.json";
    private static final String TEST_REALM_JSON = "test-realm-15.json";
    private static final String INPUT_NEW_REALM_JSON = RESOURCES_INPUT + NEW_REALM;
    private static final String INPUT_TEST_REALM_JSON = RESOURCES_INPUT + TEST_REALM_JSON;

    static {

        KEYCLOAK_15 = new GenericContainer<>(DockerImageName
                .parse("quay.io/keycloak/keycloak:15.0.2"))
                .withNetwork(network)
                .withNetworkAliases("keycloak-test")
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withFileSystemBind("src/test/resources/realms/test-realm-15.0.2.json",
                        "/opt/keycloak/data/import/test-realm.json", BindMode.READ_ONLY)
                .withExposedPorts(8080, 8080)
                .withCommand("" +
                        "-Dkeycloak.import=/opt/keycloak/data/import/test-realm.json " +
                        "-Dkeycloak.profile.upload_script=enabled " +
                        "-Dkeycloak.home.dir=keycloak")
                .withCreateContainerCmdModifier(cmd -> cmd.withName("keycloak-15"))
                .waitingFor(Wait.forHttp("/").forPort(8080));
        KEYCLOAK_15.start();

    }

    @DynamicPropertySource
    static void keycloakConfigPropertiesV15(DynamicPropertyRegistry registry){
        registry.add("keycloak.url", () -> String.format("http://localhost:%s/auth", KEYCLOAK_15.getMappedPort(8080)));
    }

    @Disabled("Verifying issues")
    @Test
    public void whenExport_and_outputFileEmpty_thenLogRealm(){
        commandFacade.exportConfig(TEST_REALM, "");
    }

    @Disabled("Verifying issues")
    @Test
    public void whenImport_and_RealmExists_thenUpdateRealm(){
        commandFacade.applyConfig(INPUT_TEST_REALM_JSON, "");
    }

    @Test
    public void whenImport_and_RealmDoesntExist_thenCreateRealm(){
        commandFacade.applyConfig(INPUT_NEW_REALM_JSON, "");
    }


}
