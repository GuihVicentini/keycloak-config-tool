package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class IdentityProviderExportServiceTest extends AbstractIntegrationTest {

    @Autowired
    IdentityProviderExportService exportService;


    @Test
    public void getAllIdps() {
        var idps = exportService.getIdps(TEST_REALM);
        log.info("Idps: {}", JsonMapperUtils.objectToJsonPrettyString(idps));
    }
}
