package com.guihvicentini.keycloakconfigtool.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringUtilTest {

    private String toBeReplaced = "org.keycloak.representations.idm.RolesRepresentation";


    @Test
    public void whenStringWithDots_thenReturnLastWord_withFirstCharToLowercase(){
        assertEquals("rolesRepresentation", StringUtil.lastWordSplitByDotsToLower.apply(toBeReplaced));
    }

    @Test
    public void whenFunctionStringWithDotsEmpty_thenThrow(){
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                StringUtil.lastWordSplitByDotsToLower.apply("."));
        assertTrue(exception.getMessage().contains("string has no word separated by dots"));
    }

    @Test
    public void whenFunctionStringWithNoDots_thenThrow(){
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                StringUtil.lastWordSplitByDotsToLower.apply(""));
        assertTrue(exception.getMessage().contains("string is empty"));
    }

    @Test
    public void whenClassInstance_throwException() {
        Exception exception = assertThrows(IllegalStateException.class,
                StringUtil::new);

        assertTrue(exception.getMessage().contains("Utility class"));
    }

    @Test
    public void removeSuffix() {
        String clientId = "some-client-realm";
        String suffix = "-realm";
        String clientIdWithoutSuffix = StringUtil.removeSuffix(clientId, suffix);
        assertEquals("some-client", clientIdWithoutSuffix);
    }
}
