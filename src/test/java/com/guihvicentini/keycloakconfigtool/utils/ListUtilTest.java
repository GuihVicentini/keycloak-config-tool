package com.guihvicentini.keycloakconfigtool.utils;

import com.guihvicentini.keycloakconfigtool.filehandlers.ReadFileHandler;
import com.guihvicentini.keycloakconfigtool.models.ComponentExportConfig;
import com.guihvicentini.keycloakconfigtool.models.IdentityProviderConfig;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ListUtilTest {

    public static final String INPUT_FOLDER = "src/test/resources/input/utils/";
    private static final String TEST_REALM = INPUT_FOLDER + "test-realm.json";
    private static final String NEW_REALM = INPUT_FOLDER + "new-realm.json";
    @Autowired
    private ReadFileHandler readFileHandler;
    private Map<String, List<ComponentExportConfig>> testRealmComponents;
    private Map<String, List<ComponentExportConfig>> newRealmComponents;
    private RealmConfig testRealm;
    private RealmConfig newRealm;


    @BeforeEach
    public void setup(){
        testRealm = readFileHandler.readRealmConfig(TEST_REALM, "");
        newRealm = readFileHandler.readRealmConfig(NEW_REALM, "");

        testRealmComponents = testRealm.getComponents();
        newRealmComponents = newRealm.getComponents();

    }

    @Test
    public void whenListsEqual_thenReturnEmptyList(){
        assertEquals(Collections.emptyList(), ListUtil.getMissingElements(testRealmComponents.values(),
                testRealmComponents.values()));
    }


    @Test
    public void whenListsNotEqual_thenReturnMissingElements(){
        var expectedList = testRealmComponents.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
        assertEquals(expectedList, ListUtil.getMissingElements(testRealmComponents.values(),
                Collections.emptyList()));
    }

    @Test
    public void whenObjectMissingInSecondList_thenReturnObject(){
        var expected = testRealm.getIdentityProviders();
        var actual = ListUtil.getMissingConfigElements(testRealm.getIdentityProviders(),
                newRealm.getIdentityProviders());
        assertEquals(expected, actual);
    }

    @Test
    public void whenObjectNotMissingInSecondList_thenReturnEmptyList(){
        var actual = ListUtil.getMissingConfigElements(testRealm.getIdentityProviders(),
                testRealm.getIdentityProviders());
        assertEquals(Collections.emptyList(), actual);
    }

    @Test
    public void whenObjectNotMissingInSecondList_butNotEqual_thenReturnEmptyList(){
        var randomIdp = ListUtil.getRandomElement(testRealm.getIdentityProviders());
        randomIdp.setEnabled(!randomIdp.isEnabled());
        newRealm.getIdentityProviders().add(randomIdp);

        var foundedByAlias =  testRealm.getIdentityProviders()
                .stream().filter(idp -> randomIdp.getAlias().equals(idp.getAlias())).findFirst();

        assertTrue(foundedByAlias.isPresent());

        var actual = ListUtil.getMissingConfigElements(testRealm.getIdentityProviders(),
                newRealm.getIdentityProviders());

        assertEquals(Collections.emptyList(), actual);
    }

    @Test
    public void whenObjectNotMissingInSecondList_butNotEqual_thenReturnObject(){
        IdentityProviderConfig newIdp = new IdentityProviderConfig();
        var randomIdp = ListUtil.getRandomElement(testRealm.getIdentityProviders());
        newIdp.setAlias(randomIdp.getAlias());
        boolean enabled = !randomIdp.isEnabled();
        newIdp.setEnabled(enabled);
        newRealm.getIdentityProviders().add(newIdp);

        var foundedByAlias = newRealm.getIdentityProviders()
                .stream().filter(idp -> randomIdp.getAlias().equals(idp.getAlias())).findFirst();

        assertTrue(foundedByAlias.isPresent());

        var expected =  testRealm.getIdentityProviders()
                .stream().filter(idp -> randomIdp.getAlias().equals(idp.getAlias())).toList();

        assertFalse(expected.isEmpty());

        var actual = ListUtil.getNonEqualConfigsWithSameIdentifier(testRealm.getIdentityProviders(),
                newRealm.getIdentityProviders());

        assertEquals(expected, actual);
    }


    @Test
    public void whenListsDifferent_returnMissingElements() {
        List<String> first = new ArrayList<>();
        first.add("a");
        first.add("b");
        first.add("c");
        int firstSize = first.size();
        List<String> second = new ArrayList<>();
        second.add("b");
        second.add("c");
        second.add("d");
        int secondSize = second.size();

        List<String> missing = ListUtil.getMissingElements(first, second);
        int missingExpectedSize = 1;
        assertEquals(firstSize, first.size());
        assertEquals(missingExpectedSize, missing.size());
        assertTrue(missing.contains(first.get(0)));

        missing = ListUtil.getMissingElements(second, first);
        assertEquals(secondSize, second.size());
        assertEquals(missingExpectedSize, missing.size());
        assertTrue(missing.contains(second.get(2)));
    }

    @Test
    public void whenClassInstance_throwException() {
        Exception exception = assertThrows(IllegalStateException.class,
                ListUtil::new);
        assertTrue(exception.getMessage().contains("Utility class"));
    }

    @Test
    public void whenGetRandomFromNullOrEmptyList_throwException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> ListUtil.getRandomElement(List.of()));
        assertTrue(exception.getMessage().contains("The list is null or empty."));
    }

}
