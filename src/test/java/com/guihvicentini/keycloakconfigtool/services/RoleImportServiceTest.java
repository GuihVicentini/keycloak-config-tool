package com.guihvicentini.keycloakconfigtool.services;


import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.RoleConfig;
import com.guihvicentini.keycloakconfigtool.models.RolesConfig;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class RoleImportServiceTest extends AbstractIntegrationTest {

    @Autowired
    private RoleImportService importService;

    @Test
    @Order(1)
    public void whenGetAllRoles_thenReturnFullRoleInformation() {
        RolesConfig roles = importService.getRealmAndClientRoles(TEST_REALM);

        log.info("Roles: {}", JsonMapperUtils.objectToJsonPrettyString(roles));

        roles.getRealm().forEach(role -> assertFalse(role.getContainerId().matches(UUID_MATCH)));
        roles.getClient().values().forEach(list -> list.forEach(role -> assertFalse(role.getContainerId().matches(UUID_MATCH))));

        Optional<RoleConfig> maybeTestClientCompositeRole = roles.getClient().get("test-client").stream()
                .filter(role -> role.identifier().equals("test-client-composite-role")).findFirst();

        assertTrue(maybeTestClientCompositeRole.isPresent());
        RoleConfig clientCompositeRole = maybeTestClientCompositeRole.get();

        Map<String, List<String>> clientComposites = clientCompositeRole.getComposites().getClient();

        assertFalse(clientComposites.isEmpty());

        Optional<RoleConfig> maybeTestRealmCompositeRole = roles.getRealm().stream()
                .filter(role -> role.identifier().equals("test-realm-composite-role")).findFirst();

        assertTrue(maybeTestRealmCompositeRole.isPresent());
        RoleConfig realmCompositeRole = maybeTestClientCompositeRole.get();

        Set<String> reamComposites = realmCompositeRole.getComposites().getRealm();
        assertFalse(reamComposites.isEmpty());

    }
}
