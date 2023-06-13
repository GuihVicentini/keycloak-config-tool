package com.guihvicentini.keycloakconfigtool.utils;

import com.guihvicentini.keycloakconfigtool.models.IdentityProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JsonMapperUtilTest {

    @Test
    public void logDebug(){
        IdentityProviderConfig idp = new IdentityProviderConfig();
        String idpJsonString = JsonMapperUtils.objectToJsonString(idp);
        assertTrue(idpJsonString.contains("alias"));
        assertTrue(idpJsonString.contains("displayName"));
    }

    @Test
    public void whenClassInstance_throwException() {
        Exception exception = assertThrows(IllegalStateException.class,
                JsonMapperUtils::new);

        assertTrue(exception.getMessage().contains("Utility class"));
    }

}
