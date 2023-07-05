package com.guihvicentini.keycloakconfigtool.facade;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.exceptions.WriteConfigFileException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class ConfigCommandFacadeTest extends AbstractIntegrationTest {

    private static final String RESOURCES_OUTPUT = "src/test/resources/output/";
    private static final String RESOURCES_INPUT = "src/test/resources/input/";
    private static final String EXPORTED_REAM_JSON = "exported-ream.json";
    private static final String NEW_REALM = "new-realm.json";
    private static final String TEST_REALM_JSON = "updated-test-realm.json";
    private static final String NULL_REALM_JSON = "null-realm.json";
    public static final String REALM = "test";
    public static final String NOT_EXISTING_REALM = "not-existing-realm";

    private static final String INPUT_NEW_REALM_JSON = RESOURCES_INPUT + NEW_REALM;
    private static final String INPUT_TEST_REALM_JSON = RESOURCES_INPUT + TEST_REALM_JSON;
    private static final String INPUT_NULL_REALM_JSON = RESOURCES_INPUT +  NULL_REALM_JSON;

    private static final String OUTPUT_EXPORTED_REALM_JSON = RESOURCES_OUTPUT + EXPORTED_REAM_JSON;
    private static final String OUTPUT_NULL_REALM_JSON = RESOURCES_OUTPUT + NULL_REALM_JSON;

    @Autowired
    ConfigCommandFacade commandFacade;


    @Test
    @Order(1)
    public void whenOutputFileEmpty_thenLogConfig(){
        commandFacade.exportConfig(REALM, "");
    }

    @Test
    @Order(2)
    public void whenOutputFileNotEmpty_thenWriteConfigToFile(){
        commandFacade.exportConfig(REALM, OUTPUT_EXPORTED_REALM_JSON);
    }

    @Test
    @Order(3)
    public void whenExport_and_RealmDoesntExist_and_outputFileEmpty_thenLogNull(){
        commandFacade.exportConfig(NOT_EXISTING_REALM, "");
    }

    @Test
    @Order(4)
    public void whenExport_and_RealmDoesntExist_and_outputFile_thenLogWriteNullToFile(){
        commandFacade.exportConfig("not-existing-realm", OUTPUT_NULL_REALM_JSON);
    }

    @Test
    @Order(5)
    public void whenOutputFileNotEmpty_andPathDoesntExist_thenThrowException(){
        String path = "/non/existing/path";
        Exception e = assertThrows(WriteConfigFileException.class,
                () -> commandFacade.exportConfig(REALM, path));
        assertTrue(e.getMessage().contains(path));
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
