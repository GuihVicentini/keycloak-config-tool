package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.RequiredActionProviderConfig;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class RequiredActionImportServiceTest extends AbstractIntegrationTest {

    public static final String REQUIRED_ACTION_ALIAS = "requiredAction1";
    @Autowired
    RequiredActionImportService importService;

    @Test
    @Order(1)
    public void getAllRequiredActions() {
        List<RequiredActionProviderConfig> requiredActions = importService.getAllRequiredActions(TEST_REALM);
        log.info("Required Actions: {}", JsonMapperUtils.objectToJsonPrettyString(requiredActions));
        assertEquals(9, requiredActions.size());
    }

    @Test
    @Order(2)
    public void testDoImport_AddRequiredAction() {
        List<RequiredActionProviderConfig> actual = new ArrayList<>();
        List<RequiredActionProviderConfig> target = new ArrayList<>();

        RequiredActionProviderConfig requiredActionConfig = createRequiredAction(REQUIRED_ACTION_ALIAS);
        target.add(requiredActionConfig);

        importService.doImport(TEST_REALM, actual, target);

       Optional<RequiredActionProviderConfig> createdAction = importService.getAllRequiredActions(TEST_REALM)
               .stream().filter(action -> requiredActionConfig.identifier().equals(action.identifier())).findFirst();

       assertTrue(createdAction.isPresent());

       log.info("Expected: {}", JsonMapperUtils.objectToJsonString(requiredActionConfig));
       log.info("Actual: {}", JsonMapperUtils.objectToJsonString(createdAction.get()));

       assertEquals(requiredActionConfig, createdAction.get());
    }


    @Test
    @Order(3)
    public void testDoImport_UpdateRequiredAction() {
        List<RequiredActionProviderConfig> actual = new ArrayList<>();
        List<RequiredActionProviderConfig> target = new ArrayList<>();

        RequiredActionProviderConfig actualActionConfig = createRequiredAction(REQUIRED_ACTION_ALIAS);
        actual.add(actualActionConfig);

        RequiredActionProviderConfig targetActionConfig = createRequiredAction(REQUIRED_ACTION_ALIAS);
        targetActionConfig.setEnabled(false);
        target.add(targetActionConfig);

        importService.doImport(TEST_REALM, actual, target);

        Optional<RequiredActionProviderConfig> createdAction = importService.getAllRequiredActions(TEST_REALM)
                .stream().filter(action -> targetActionConfig.identifier().equals(action.identifier())).findFirst();

        assertTrue(createdAction.isPresent());

        log.info("Expected: {}", JsonMapperUtils.objectToJsonString(targetActionConfig));
        log.info("Actual: {}", JsonMapperUtils.objectToJsonString(createdAction.get()));

        assertEquals(targetActionConfig, createdAction.get());

    }

    @Test
    @Order(4)
    public void testDoImport_DeleteRequiredAction() {
        List<RequiredActionProviderConfig> actual = new ArrayList<>();
        List<RequiredActionProviderConfig> target = new ArrayList<>();

        RequiredActionProviderConfig actualActionConfig = createRequiredAction(REQUIRED_ACTION_ALIAS);
        actual.add(actualActionConfig);

        importService.doImport(TEST_REALM, actual, target);

        Optional<RequiredActionProviderConfig> createdAction = importService.getAllRequiredActions(TEST_REALM)
                .stream().filter(action -> actualActionConfig.identifier().equals(action.identifier())).findFirst();

        assertTrue(createdAction.isEmpty());

    }

    /**
     * Keycloak doesn't allow to create a required action with a providerId that already exists.
     * Moreover, the providerId and the alias must be the same!
     * @param alias of the required config
     * @return an enabled RequiredActionProviderConfig
     */
    private RequiredActionProviderConfig createRequiredAction(String alias) {
        RequiredActionProviderConfig config = new RequiredActionProviderConfig();
        config.setAlias(alias);
        config.setName(alias.toUpperCase());
        config.setProviderId(alias);
        config.setEnabled(true);
        config.normalize();
        return  config;
    }

}
