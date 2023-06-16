package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupExportServiceIT extends AbstractIntegrationTest {

    @Autowired
    GroupExportService groupExportService;

    @Test
    public void getAllGroupConfigs() {
        var groups = groupExportService.getGroupConfigs(TEST_REALM);

        assertEquals(1, groups.size());
        assertEquals("test-group", groups.get(0).getName());
        assertEquals("/test-group", groups.get(0).getPath());
    }

}
