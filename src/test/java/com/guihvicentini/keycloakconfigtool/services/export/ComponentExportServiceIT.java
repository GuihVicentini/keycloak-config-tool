package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ComponentExportServiceIT extends AbstractIntegrationTest {

    @Autowired
    ComponentExportService exportService;

    @Test
    public void getAll() {
        var components = exportService.getAll(TEST_REALM);
        log.info("Ldap Components: {}", JsonMapperUtils.objectToJsonPrettyString(components));
    }

    @Test
    public void getAllUserStorage() {
        var ldapComponent = exportService.getAll(TEST_REALM, ConfigConstants.USER_STORAGE_PROVIDER_TYPE);
        log.info("Ldap Components: {}", JsonMapperUtils.objectToJsonPrettyString(ldapComponent));
    }

    @Test
    public void getAllKeys() {
        var ldapComponent = exportService.getAll(TEST_REALM, ConfigConstants.KEY_PROVIDER_TYPE);
        log.info("Key Components: {}", JsonMapperUtils.objectToJsonPrettyString(ldapComponent));
    }

}
