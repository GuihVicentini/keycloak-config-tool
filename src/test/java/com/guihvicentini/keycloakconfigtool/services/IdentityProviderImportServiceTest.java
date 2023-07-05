package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.IdentityProviderConfig;
import com.guihvicentini.keycloakconfigtool.services.export.IdentityProviderExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class IdentityProviderImportServiceTest extends AbstractIntegrationTest {

    @Autowired
    IdentityProviderImportService importService;
    @Autowired
    IdentityProviderExportService exportService;

    @Test
    @Order(1)
    public void getAllIdp() {
        List<IdentityProviderConfig> idps =  exportService.getAll(TEST_REALM);
        log.debug("Idps: {}", JsonMapperUtils.objectToJsonString(idps));
        assertEquals(1, idps.size());
    }

}
