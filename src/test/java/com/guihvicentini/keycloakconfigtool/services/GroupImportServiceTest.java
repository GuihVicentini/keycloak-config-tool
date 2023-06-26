package com.guihvicentini.keycloakconfigtool.services;


import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import com.guihvicentini.keycloakconfigtool.services.export.GroupExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class GroupImportServiceTest extends AbstractIntegrationTest {


    @Autowired
    private GroupImportService groupImportService;
    @Autowired
    private GroupExportService groupExportService;

    @Test
    public void getAll(){
        var groups = groupExportService.getGroupConfigs(TEST_REALM);
        log.info("Groups: {}", JsonMapperUtils.objectToJsonString(groups));
    }

    @Test
    @Order(1)
    void testDoImport_GroupExistsInTargetButNotInActual_CreateGroup() {
        // Existing groups in the realm
        List<GroupConfig> actualGroups = List.of();

        // Target groups to import/update
        List<GroupConfig> targetGroups = Arrays.asList(
                createGroupConfig("group1", Arrays.asList("uma_authorization", "offline_access")),
                createGroupConfig("group2", Arrays.asList("default-roles-test"))
        );

        groupImportService.doImport(TEST_REALM, actualGroups, targetGroups);

        // Assert that the new group was created
        List<GroupConfig> importedGroups = groupExportService.getGroupConfigs(TEST_REALM);

        // test realm should have already 1 group + 2 newly created = 3
        assertEquals(3, importedGroups.size());

        GroupConfig newGroupOne = importedGroups.stream()
                .filter(group -> group.getName().equals("group1"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New group not found"));

        assertEquals(Arrays.asList("uma_authorization", "offline_access"), newGroupOne.getRealmRoles());

        GroupConfig newGroupTwo = importedGroups.stream()
                .filter(group -> group.getName().equals("group2"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New group not found"));

        assertEquals(Arrays.asList("default-roles-test"), newGroupTwo.getRealmRoles());
    }

    @Test
    @Order(2)
    void testDoImport_GroupExistsInTargetAndInActualButDifferent_UpdateGroup() {
        // Existing groups in the realm
        List<GroupConfig> actualGroups = Arrays.asList(
                createGroupConfig("group1", Arrays.asList("uma_authorization", "offline_access")),
                createGroupConfig("group2", Arrays.asList("default-roles-test"))
        );

        // Target groups to import/update
        List<GroupConfig> targetGroups = Arrays.asList(
                createGroupConfig("group1", Arrays.asList("uma_authorization", "default-roles-test")), // Updated group
                createGroupConfig("group2", Arrays.asList("default-roles-test")) // Existing group
        );

        groupImportService.doImport(TEST_REALM, actualGroups, targetGroups);

        // Assert that the group was updated
        List<GroupConfig> importedGroups = groupExportService.getGroupConfigs(TEST_REALM);

        assertEquals(3, importedGroups.size());

        GroupConfig updatedGroup = importedGroups.stream()
                .filter(group -> group.getName().equals("group1"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Updated group not found"));

        // list must be sorted
        assertEquals(Arrays.asList("default-roles-test", "uma_authorization"), updatedGroup.getRealmRoles());
    }

    @Test
    @Order(3)
    void testDoImport_GroupExistsInActualButNotInTarget_DeleteGroup() {
        // Existing groups in the realm
        List<GroupConfig> actualGroups = Arrays.asList(
                createGroupConfig("group1", Arrays.asList("uma_authorization", "default-roles-test")),
                createGroupConfig("group2", Arrays.asList("default-roles-test"))
        );

        // Target groups to import/update
        List<GroupConfig> targetGroups = Arrays.asList(
                createGroupConfig("group1", Arrays.asList("uma_authorization", "default-roles-test")) // Existing group
        );

        groupImportService.doImport(TEST_REALM, actualGroups, targetGroups);

        // Assert that the group was deleted
        List<GroupConfig> importedGroups = groupExportService.getGroupConfigs(TEST_REALM);

        assertEquals(2, importedGroups.size());

        Optional<GroupConfig> deletedGroup = importedGroups.stream()
                .filter(group -> group.getName().equals("group2"))
                .findFirst();

        assertTrue(deletedGroup.isEmpty());

        GroupConfig remainingGroup = importedGroups.stream()
                .filter(group -> group.getName().equals("group1"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Updated group not found"));

        assertEquals("group1", remainingGroup.getName());

        // list must be sorted
        assertEquals(Arrays.asList("default-roles-test", "uma_authorization"), remainingGroup.getRealmRoles());
    }

    // Helper method to create a GroupConfig
    private GroupConfig createGroupConfig(String name, List<String> realmRoles) {
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName(name);
        groupConfig.setRealmRoles(realmRoles);
        // Set other properties as needed
        return groupConfig;
    }
}
