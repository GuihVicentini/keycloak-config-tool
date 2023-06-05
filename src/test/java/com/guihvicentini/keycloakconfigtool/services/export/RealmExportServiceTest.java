package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class RealmExportServiceTest extends AbstractIntegrationTest {

    @Autowired
    RealmExportService exportService;

    @Test
    public void getRealmConfig() {
        RealmConfig config = exportService.getFullRealm(TEST_REALM);
        log.info("Realm: {}", JsonMapperUtils.objectToJsonPrettyString(config));
    }
}
