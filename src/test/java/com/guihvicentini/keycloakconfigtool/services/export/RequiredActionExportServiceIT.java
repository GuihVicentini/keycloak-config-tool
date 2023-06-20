package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RequiredActionExportServiceIT extends AbstractIntegrationTest {

    @Autowired
    RequiredActionExportService target;

    @Test
    public void getAll() {
        var result = target.getAll(TEST_REALM);
        Assertions.assertEquals(9, result.size());
        Assertions.assertEquals("CONFIGURE_TOTP", result.get(0).getAlias());
        Assertions.assertEquals("terms_and_conditions", result.get(1).getAlias());
        Assertions.assertEquals("UPDATE_PASSWORD", result.get(2).getAlias());
        Assertions.assertEquals("UPDATE_PROFILE", result.get(3).getAlias());
        Assertions.assertEquals("VERIFY_EMAIL", result.get(4).getAlias());
        Assertions.assertEquals("delete_account", result.get(5).getAlias());
        Assertions.assertEquals("webauthn-register", result.get(6).getAlias());
        Assertions.assertEquals("webauthn-register-passwordless", result.get(7).getAlias());
        Assertions.assertEquals("update_user_locale", result.get(8).getAlias());
    }
}
