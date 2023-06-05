package com.guihvicentini.keycloakconfigtool.utils;

import com.guihvicentini.keycloakconfigtool.filehandlers.ReadFileHandler;
import com.guihvicentini.keycloakconfigtool.models.ComponentExportConfig;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import com.guihvicentini.keycloakconfigtool.models.RequiredActionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MapUtilTest {

    public static final String CLIENT_REGISTRATION_POLICY = "clientRegistrationPolicy";
    public static final String KEY_PROVIDER = "keyProvider";
    private static final String INPUT_FOLDER = "src/test/resources/input/utils/";
    private static final String NEW_REALM = INPUT_FOLDER+"new-realm.json";
    private static final String TEST_REALM = INPUT_FOLDER+"test-realm.json";
    public static final String USER_STORAGE_PROVIDER = "userStorageProvider";
    private Map<String, String> attributes;
    private Map<String, String> renamedAttributes;
    private Map<String, String> sortedAttributes;
    private Map<String, String> reverseMap;

    private Map<String, List<ComponentExportConfig>> newComponents;
    private Map<String, List<ComponentExportConfig>> testComponents;

    @Autowired
    private ReadFileHandler fileHandler;

    private final Map<String, String> controlMap = Map.of(
            "post.logout.redirect.uris", "+",
            "oauth2.device.authorization.grant.enabled", "false",
            "backchannel.logout.revoke.offline.tokens", "false",
            "tls-client-certificate-bound-access-tokens", "false",
            "backchannel.logout.session.required", "true",
            "client_credentials.use_refresh_token","false",
            "acr.loa.map","{}",
            "require.pushed.authorization.requests", "false",
            "token.response.type.bearer.lower-case", "false"
    );

    @BeforeEach
    public void setup(){
        attributes = new LinkedHashMap<>();
        attributes.put("post.logout.redirect.uris", "+");
        attributes.put("oauth2.device.authorization.grant.enabled", "false");
        attributes.put("backchannel.logout.revoke.offline.tokens", "false");
        attributes.put("tls-client-certificate-bound-access-tokens", "false");
        attributes.put("backchannel.logout.session.required", "true");
        attributes.put("client_credentials.use_refresh_token", "false");
        attributes.put("acr.loa.map", "{}");
        attributes.put("require.pushed.authorization.requests", "false");
        attributes.put("token.response.type.bearer.lower-case", "false");

        renamedAttributes = new HashMap<>();
        renamedAttributes.put("uris", "+");
        renamedAttributes.put("enabled", "false");
        renamedAttributes.put("tokens", "false");
        renamedAttributes.put("tls-client-certificate-bound-access-tokens", "false");
        renamedAttributes.put("required", "true");
        renamedAttributes.put("use_refresh_token", "false");
        renamedAttributes.put("map", "{}");
        renamedAttributes.put("requests", "false");
        renamedAttributes.put("lower-case", "false");

        sortedAttributes = new LinkedHashMap<>();
        sortedAttributes.put("acr.loa.map", "{}");
        sortedAttributes.put("backchannel.logout.revoke.offline.tokens", "false");
        sortedAttributes.put("backchannel.logout.session.required", "true");
        sortedAttributes.put("client_credentials.use_refresh_token", "false");
        sortedAttributes.put("oauth2.device.authorization.grant.enabled", "false");
        sortedAttributes.put("post.logout.redirect.uris", "+");
        sortedAttributes.put("require.pushed.authorization.requests", "false");
        sortedAttributes.put("tls-client-certificate-bound-access-tokens", "false");
        sortedAttributes.put("token.response.type.bearer.lower-case", "false");

        reverseMap = new HashMap<>();
        reverseMap.put("uris", "post.logout.redirect.uris");
        reverseMap.put("enabled", "oauth2.device.authorization.grant.enabled");
        reverseMap.put("tokens", "backchannel.logout.revoke.offline.tokens");
        reverseMap.put("required", "backchannel.logout.session.required");
        reverseMap.put("use_refresh_token", "client_credentials.use_refresh_token");
        reverseMap.put("map", "acr.loa.map");
        reverseMap.put("requests", "require.pushed.authorization.requests");
        reverseMap.put("lower-case", "token.response.type.bearer.lower-case");

        RealmConfig newRealm = fileHandler.readRealmConfig(NEW_REALM,"");
        RealmConfig testRealm = fileHandler.readRealmConfig(TEST_REALM,"");

        newComponents = newRealm.getComponents();
        testComponents = testRealm.getComponents();

    }

    @Test
    public void sortMapAlphabetically(){
        MapUtil.sortMapByKey(attributes);
        assertIterableEquals(sortedAttributes.keySet(), attributes.keySet());
    }

    @Test
    public void renamedKeys(){
        MapUtil.renameKeys(attributes, StringUtil.lastWordSplitByDotsToLower);
        assertEquals(renamedAttributes, attributes);
        MapUtil.renameKeys(attributes, reverseMap);
        assertEquals(controlMap, attributes);
    }

    @Test
    public void getNonEqualElements() {

        var keyProviders = testComponents.get(KEY_PROVIDER);
        var newKeyProviders = newComponents.get(KEY_PROVIDER);

        assertEquals(keyProviders, newKeyProviders);

        var clientComponents = testComponents.get(CLIENT_REGISTRATION_POLICY);
        var newClientComponents = newComponents.get(CLIENT_REGISTRATION_POLICY);

        assertEquals(clientComponents, newClientComponents);

        var expectedComponents = testComponents.get(USER_STORAGE_PROVIDER);
        var expected = new HashMap<>();
        expected.put(USER_STORAGE_PROVIDER, expectedComponents);

        var missing = MapUtil.getMissingElements(testComponents, newComponents);

        assertEquals(expected, missing);
    }

    @Test
    public void whenComponentsEquals_returnEmptyMap() {
        testComponents.remove(USER_STORAGE_PROVIDER);
        newComponents.remove(USER_STORAGE_PROVIDER);
        assertEquals(testComponents, newComponents);
        assertEquals(Collections.emptyMap(), MapUtil.getMissingElements(testComponents, newComponents));
    }

    @Test
    public void whenComponentMissing_returnMissingComponent() {
        ComponentExportConfig missingComponent = ListUtil.getRandomElement(newComponents.get(CLIENT_REGISTRATION_POLICY));
        newComponents.get(CLIENT_REGISTRATION_POLICY).remove(missingComponent);

        Map<String, List<ComponentExportConfig>> missingElements = MapUtil.getMissingElements(testComponents, newComponents);

        var expected = new HashMap<>();
        expected.put(USER_STORAGE_PROVIDER, testComponents.get(USER_STORAGE_PROVIDER));
        expected.put(CLIENT_REGISTRATION_POLICY, List.of(missingComponent));

        assertEquals(expected, missingElements);
    }

    @Test
    public void whenSecondMapHasMoreElements_thenReturnOnlyMissingElements() {
        ComponentExportConfig addedComponent = new ComponentExportConfig();
        addedComponent.setName("added-key");
        addedComponent.setProviderId("hmac-generated");
        addedComponent.setSubType(null);
        addedComponent.setSubComponents(Collections.emptyMap());
        addedComponent.setConfig(Map.of(
                "algorithm", List.of("HS256"),
                "priority", List.of("50"))
        );

        newComponents.get(KEY_PROVIDER).add(addedComponent);

        Map<String, List<ComponentExportConfig>> missingElements = MapUtil.getMissingElements(testComponents, newComponents);

        var expected = new HashMap<>();
        expected.put(USER_STORAGE_PROVIDER, testComponents.get(USER_STORAGE_PROVIDER));

        assertEquals(expected, missingElements);

        // should return the added element
        newComponents.remove(USER_STORAGE_PROVIDER);
        missingElements = MapUtil.getMissingElements(newComponents, testComponents);

        expected.put(KEY_PROVIDER, List.of(addedComponent));
        expected.remove(USER_STORAGE_PROVIDER);

        assertEquals(expected, missingElements);

    }

    @Test
    public void whenKeyMissingOnSecond_returnMissingEntry(){
        String missingKey = CLIENT_REGISTRATION_POLICY;
        List<ComponentExportConfig> missingValue = newComponents.remove(missingKey);;

        Map<String, List<ComponentExportConfig>> missingElements = MapUtil.getMissingElements(testComponents, newComponents);

        var expected = new HashMap<>();
        expected.put(USER_STORAGE_PROVIDER, testComponents.get(USER_STORAGE_PROVIDER));
        expected.put(missingKey, missingValue);

        assertEquals(expected, missingElements);
    }

    @Test
    public void testGetMissingElements() {
        // Create the first map
        Map<String, List<RequiredActionConfig>> first = new HashMap<>();
        List<RequiredActionConfig> firstList = new ArrayList<>();
        firstList.add(createRequiredConfig("1"));
        firstList.add(createRequiredConfig("2"));
        first.put("key1", firstList);

        // Create the second map
        Map<String, List<RequiredActionConfig>> second = new HashMap<>();
        List<RequiredActionConfig> secondList1 = new ArrayList<>();
        secondList1.add(createRequiredConfig("1"));
        second.put("key1", secondList1);
        List<RequiredActionConfig> secondList2 = new ArrayList<>();
        secondList2.add(createRequiredConfig("3"));
        second.put("key2", secondList2);

        // Call the method
        Map<String, List<RequiredActionConfig>> result = MapUtil.getMissingElements(first, second);

        // Assert the result
        assertEquals(1, result.size());
        assertTrue(result.containsKey("key1"));
        assertFalse(result.containsKey("key2"));

        List<RequiredActionConfig> missingList1 = result.get("key1");
        assertEquals(1, missingList1.size());

        RequiredActionConfig missingConfig1 = missingList1.get(0);
        assertEquals("2", missingConfig1.identifier());

        result = MapUtil.getMissingElements(second, first);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("key2"));
        assertFalse(result.containsKey("key1"));

        List<RequiredActionConfig> missingList3 = result.get("key2");
        assertEquals(1, missingList3.size());
        RequiredActionConfig missingConfig3 = missingList3.get(0);

        assertEquals("3", missingConfig3.identifier());
    }

    @Test
    public void whenClassInstance_throwException() {
        Exception exception = assertThrows(IllegalStateException.class,
                MapUtil::new);
        assertTrue(exception.getMessage().contains("Utility class"));
    }

    private RequiredActionConfig createRequiredConfig(String name) {
        RequiredActionConfig config = new RequiredActionConfig();
        config.setAlias(name);
        config.setName(name);
        config.normalize();
        return config;
    }


}
