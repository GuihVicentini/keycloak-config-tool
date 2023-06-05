package com.guihvicentini.keycloakconfigtool.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.filehandlers.WriteFileHandler;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class IdentityProviderResourceAdapterTest extends AbstractIntegrationTest {


    public static final String INPUT_FOLDER = "src/test/resources/input/adapters/identityProviders/";
    public static final String IDPS = INPUT_FOLDER+"test-oidc.json";
    public static final String MAPPERS = INPUT_FOLDER+"test-oidc-mappers.json";

    public static final String REALM = "test";
    private static final String TEST_OIDC = "test-oidc";
    public static final String TEST_IDP_MAPPER = "test-idp-mapper";
    public static final String TEST_OIDC_ID = "bb6e9f37-4d6a-4d7a-911d-584c2e6cac9a";
    public static final String MAPPER_ID = "ca7cf067-8ec2-40d6-99fe-93a20c4eef7e";
    public static final String NEW_OIDC = "new-oidc";
    public static final String NEW_MAPPER = "new-mapper";

    @Autowired
    WriteFileHandler fileHandler;
    @Autowired
    IdentityProviderResourceAdapter resourceAdapter;
    @Autowired
    ObjectMapper mapper;

    private List<IdentityProviderRepresentation> expectedOidcs;

    private List<IdentityProviderMapperRepresentation> expectedTestOidcMappers;

    @BeforeEach
    public void setup() throws IOException {
        expectedOidcs = Arrays.asList(mapper.readValue(new File(IDPS),
                IdentityProviderRepresentation[].class));
        expectedTestOidcMappers = Arrays.asList(mapper.readValue(new File(MAPPERS),
                IdentityProviderMapperRepresentation[].class));
    }

    @Test
    @Order(1)
    public void getAllIdps() {
        List<IdentityProviderRepresentation> idps = resourceAdapter.getAll(REALM);

        // convert to jsonNode as IdentityProviderRepresentation doesn't overwrite the equals' method.
        JsonNode expected = mapper.valueToTree(expectedOidcs);
        JsonNode actual = mapper.valueToTree(idps);
        assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    public void getByAlias() {
        IdentityProviderRepresentation representation = resourceAdapter.getByAlias(REALM, TEST_OIDC);
        IdentityProviderRepresentation expectedOidc =  expectedOidcs.stream()
                .filter(idp -> TEST_OIDC.equals(idp.getAlias()))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("test-oidc Idp not found for testing resource adapter"));

        JsonNode expected = mapper.valueToTree(expectedOidc);
        JsonNode actual = mapper.valueToTree(representation);
        assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    public void getAllMappers() {
        List<IdentityProviderMapperRepresentation> mappers = resourceAdapter.getMappers(REALM, TEST_OIDC);

        JsonNode expected = mapper.valueToTree(expectedTestOidcMappers);
        JsonNode actual =  mapper.valueToTree(mappers);

        assertEquals(expectedOidcs.size(), mappers.size());
        expected.fields().forEachRemaining(field -> {
            // a new id for the mapper is generated when the realm is created even though the id is declared.
            if (!field.getKey().equals("id")) {
                assertEquals(field.getValue(), actual.get(field.getKey()));
            }
        });
    }

    @Test
    @Order(4)
    public void getMapperByName() {
        Optional<IdentityProviderMapperRepresentation> mapperRepresentation = resourceAdapter.getMapperByName(REALM, TEST_OIDC, TEST_IDP_MAPPER);
        assertTrue(mapperRepresentation.isPresent());

        IdentityProviderMapperRepresentation expectedOidcMapper = expectedTestOidcMappers.stream()
                .filter(expectedMapper -> TEST_IDP_MAPPER.equals(expectedMapper.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expected mapper not found for testing resource adapter"));

        // a new id for the mapper is generated when the realm is created even though the id is declared.
        // so we have to ignore the id when comparing the objects
        mapperRepresentation.get().setId("");
        expectedOidcMapper.setId("");

        JsonNode expected = this.mapper.valueToTree(expectedOidcMapper);
        JsonNode actual = this.mapper.valueToTree(mapperRepresentation.get());

        assertEquals(expected, actual);

        mapperRepresentation = resourceAdapter.getMapperByName(REALM, TEST_OIDC, "unknown-mapper");
        assertTrue(mapperRepresentation.isEmpty());
    }

    /**
     * A new id is created every time the realm gets imported. Shall this change in future keycloak releases
     * this shall be caught by this test.
     */
    @Test
    @Order(5)
    public void getMapperById() {
        Exception exception = assertThrows(NotFoundException.class,
                ()-> resourceAdapter.getMapperById(REALM, TEST_OIDC, MAPPER_ID));
    }



    @Test
    @Order(6)
    public void createOidc() {
        IdentityProviderRepresentation existingOidc = ListUtil.getRandomElement(expectedOidcs);
        IdentityProviderRepresentation newIdp;
        newIdp = existingOidc;
        newIdp.setEnabled(false);
        newIdp.setAlias(NEW_OIDC);

        // new idp can't have the same internalId as any existing OIDC
        newIdp.setInternalId(UUID.randomUUID().toString());

        // new id equals the idp alias
        String newId = resourceAdapter.create(REALM, newIdp);
        assertNotEquals(TEST_OIDC, newId);
        assertEquals(NEW_OIDC, newId);

        List<IdentityProviderRepresentation> allIdps = resourceAdapter.getAll(REALM);
        assertTrue(allIdps.size() > expectedOidcs.size());
    }

    @Test
    @Order(7)
    public void deleteOidc() {
        resourceAdapter.delete(REALM, NEW_OIDC);
        assertThrows(NotFoundException.class, () -> resourceAdapter.getByAlias(REALM, NEW_OIDC));
    }

    @Test
    @Order(8)
    public void updateOidc() {
        IdentityProviderRepresentation toBeUpdated = ListUtil.getRandomElement(expectedOidcs);
        toBeUpdated.setEnabled(false);
        resourceAdapter.update(REALM, toBeUpdated);

        JsonNode expected = mapper.valueToTree(toBeUpdated);
        JsonNode actual = mapper.valueToTree(resourceAdapter.getByAlias(REALM, TEST_OIDC));

        assertEquals(expected, actual);

        assertFalse(resourceAdapter.getByAlias(REALM, TEST_OIDC).isEnabled());
    }


    @Test
    @Order(9)
    public void createMapper() {
        IdentityProviderMapperRepresentation toBeAdded = ListUtil.getRandomElement(expectedTestOidcMappers);
        toBeAdded.setName(NEW_MAPPER);

        // regardless if the representation has already an id, a new id will be generated and assigned to the new mapper.
        String newId = resourceAdapter.addMapper(REALM, TEST_OIDC, toBeAdded);
        Optional<IdentityProviderMapperRepresentation> createdMapper = resourceAdapter
                .getMapperByName(REALM, TEST_OIDC, toBeAdded.getName());

        assertTrue(createdMapper.isPresent());
        toBeAdded.setId(newId);

        JsonNode expected = mapper.valueToTree(toBeAdded);
        JsonNode actual = mapper.valueToTree(createdMapper.get());

        assertEquals(expected, actual);
    }

    @Test
    @Order(10)
    public void deleteMapper() {
        Optional<IdentityProviderMapperRepresentation> toBeDeleted =
                resourceAdapter.getMapperByName(REALM, TEST_OIDC, NEW_MAPPER);
        assertTrue(toBeDeleted.isPresent());

        resourceAdapter.deleteMapper(REALM, TEST_OIDC, toBeDeleted.get().getId());

        Optional<IdentityProviderMapperRepresentation> deletedMapper = resourceAdapter
                .getMapperByName(REALM, TEST_OIDC, NEW_MAPPER);

        assertTrue(deletedMapper.isEmpty());
    }

    @Test
    @Order(11)
    public void updateMapper() {
        Optional<IdentityProviderMapperRepresentation> toBeUpdated = resourceAdapter
                .getMapperByName(REALM, TEST_OIDC, TEST_IDP_MAPPER);

        assertTrue(toBeUpdated.isPresent());

        Map<String, String> hashMap = new HashMap<>();

        String claims = "[{\"key\":\"acr\",\"value\":\"strong\"},{\"key\":\"roles\",\"value\":\"someOtherValue\"}]";
        String syncMode = "INHERIT";
        String areClaimValuesRegex = "true";
        String attributes = "[]";
        String role = "test-realm-composite-role";

        hashMap.put("claims", claims);
        hashMap.put("syncMode", syncMode);
        hashMap.put("are.claim.values.regex", areClaimValuesRegex);
        hashMap.put("attributes", attributes);
        hashMap.put("role", role);

        toBeUpdated.get().setConfig(hashMap);

        resourceAdapter.updateMapper(REALM, TEST_OIDC, toBeUpdated.get());

        Optional<IdentityProviderMapperRepresentation> updatedMapper = resourceAdapter
                .getMapperByName(REALM, TEST_OIDC, TEST_IDP_MAPPER);

        assertTrue(updatedMapper.isPresent());

        JsonNode expected = mapper.valueToTree(toBeUpdated.get());
        JsonNode actual = mapper.valueToTree(updatedMapper.get());

        assertEquals(expected, actual);
    }
}
