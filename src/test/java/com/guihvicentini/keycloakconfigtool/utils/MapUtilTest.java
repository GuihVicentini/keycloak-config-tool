package com.guihvicentini.keycloakconfigtool.utils;

import com.guihvicentini.keycloakconfigtool.models.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MapUtilTest {

    private Map<String, List<Config>> firstMap;
    private Map<String, List<Config>> secondMap;

    private Map<String, String> attributes;

    private Map<String, String> sortedAttributes;



    @BeforeEach
    void setUp() {
        // Initialize test data for maps
        firstMap = new HashMap<>();
        secondMap = new HashMap<>();

        List<Config> list1 = new ArrayList<>();
        list1.add(new TestConfig("A"));
        list1.add(new TestConfig("B"));
        list1.add(new TestConfig("C"));
        firstMap.put("Key1", list1);

        List<Config> list2 = new ArrayList<>();
        list2.add(new TestConfig("C"));
        list2.add(new TestConfig("D"));
        list2.add(new TestConfig("E"));
        list2.add(new TestConfig("F"));
        secondMap.put("Key1", list2);

        List<Config> list3 = new ArrayList<>();
        list3.add(new TestConfig("G"));
        list3.add(new TestConfig("H"));
        list3.add(new TestConfig("I"));
        secondMap.put("Key2", list3);

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
    }

    @Test
    void testGetMissingElements() {
        Map<String, List<Config>> missingFirstElements = MapUtil.getMissingElements(firstMap, secondMap);

        assertEquals(1, missingFirstElements.size());
        assertTrue(missingFirstElements.containsKey("Key1"));
        List<Config> missingList = missingFirstElements.get("Key1");
        assertEquals(2, missingList.size());
        assertEquals("A", missingList.get(0).identifier());
        assertEquals("B", missingList.get(1).identifier());

        Map<String, List<Config>> missingSecondElements = MapUtil.getMissingElements(secondMap, firstMap);

        assertEquals(2, missingSecondElements.size());
        assertTrue(missingSecondElements.containsKey("Key1"));
        List<Config> missingList1 = missingSecondElements.get("Key1");
        assertEquals(3, missingList1.size());
        assertEquals("D", missingList1.get(0).identifier());
        assertEquals("E", missingList1.get(1).identifier());
        assertEquals("F", missingList1.get(2).identifier());

        assertTrue(missingSecondElements.containsKey("Key2"));
        List<Config> missingList2 = missingSecondElements.get("Key2");
        assertEquals(3, missingList2.size());
        assertEquals("G", missingList2.get(0).identifier());
        assertEquals("H", missingList2.get(1).identifier());
        assertEquals("I", missingList2.get(2).identifier());

    }

    @Test
    void whenMissingElementsAreEqual_returnsEmptyMap() {
        Map<String, List<Config>> missingElements = MapUtil.getMissingElements(secondMap, secondMap);

        assertEquals(0, missingElements.size());
        assertEquals(Collections.emptyMap(), missingElements);
    }

    @Test
    public void testSortMapAlphabetically(){
        MapUtil.sortMapByKey(attributes);
        assertIterableEquals(sortedAttributes.keySet(), attributes.keySet());
    }

    @Test
    void testRenameKeysWithKeyTransformerFunction() {
        MapUtil.renameKeys(firstMap, key -> key + "_Renamed");

        assertFalse(firstMap.containsKey("Key1"));
        assertTrue(firstMap.containsKey("Key1_Renamed"));
        List<Config> renamedList = firstMap.get("Key1_Renamed");
        assertEquals(3, renamedList.size());
    }

    @Test
    void testRenameKeysWithKeyTransformerMap() {
        Map<String, String> keyTransformer = new HashMap<>();
        keyTransformer.put("Key1", "Key1_Renamed");

        MapUtil.renameKeys(firstMap, keyTransformer);

        assertFalse(firstMap.containsKey("Key1"));
        assertTrue(firstMap.containsKey("Key1_Renamed"));
        List<Config> renamedList = firstMap.get("Key1_Renamed");
        assertEquals(3, renamedList.size());
    }

    @Test
    void testRemoveMatchingEntries() {
        MapUtil.removeMatchingEntries(firstMap, secondMap);

        assertEquals(1, firstMap.size());
        assertTrue(firstMap.containsKey("Key1"));
    }

    @Test
    void testFlatten() {
        Map<String, Object> nestedMap = new LinkedHashMap<>();
        nestedMap.put("key1", "value1");
        nestedMap.put("key2", "value2");

        Map<String, Object> innerMap = new LinkedHashMap<>();
        innerMap.put("nestedKey1", "nestedValue1");
        innerMap.put("nestedKey2", "nestedValue2");

        nestedMap.put("innerMap", innerMap);

        Map<String, Object> flattenedMap = MapUtil.flatten(nestedMap);

        assertEquals(4, flattenedMap.size());
        assertTrue(flattenedMap.containsKey("/key1"));
        assertEquals("value1", flattenedMap.get("/key1"));
        assertTrue(flattenedMap.containsKey("/key2"));
        assertEquals("value2", flattenedMap.get("/key2"));
        assertTrue(flattenedMap.containsKey("/innerMap/nestedKey1"));
        assertEquals("nestedValue1", flattenedMap.get("/innerMap/nestedKey1"));
        assertTrue(flattenedMap.containsKey("/innerMap/nestedKey2"));
        assertEquals("nestedValue2", flattenedMap.get("/innerMap/nestedKey2"));
    }

    private record TestConfig(String identifier) implements Config {
    }
}

