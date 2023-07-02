package com.guihvicentini.keycloakconfigtool.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationExecution;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlow;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationSubFlow;
import com.guihvicentini.keycloakconfigtool.models.AuthenticatorConfigConfig;
import com.guihvicentini.keycloakconfigtool.services.export.AuthenticationFlowExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class AuthenticationFlowImportServiceTest extends AbstractIntegrationTest {

    @Autowired
    AuthenticationFlowImportService flowImportService;
    @Autowired
    AuthenticationFlowExportService flowExportService;


    @Test
    public void getAllFlows() {
        var flows = flowExportService.getAllFlows(TEST_REALM);
        log.info("Flows: {}", JsonMapperUtils.objectToJsonNode(flows).toPrettyString());
    }

    @Test
    @Order(1)
    void testDoImport_FlowExistsInTargetButNotInActual_CreateFlow() {
        List<AuthenticationFlow> actualFlows = new ArrayList<>();
        List<AuthenticationFlow> targetFlows = Arrays.asList(createFlowConfig("flow1"), createFlowConfig("flow2"));

        flowImportService.doImport(TEST_REALM, actualFlows, targetFlows);

        // Assert that the missing flow was created
        AuthenticationFlow createdFlow = targetFlows.get(0);
        AuthenticationFlow importedFlow = flowExportService.getAllFlows(TEST_REALM).stream()
                .filter(flow -> flow.getAlias().equals(createdFlow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Created flow not found"));

        assertEquals(createdFlow, importedFlow);

        AuthenticationFlow createdSecondFlow = targetFlows.get(1);
        AuthenticationFlow importedSecondFlow = flowExportService.getAllFlows(TEST_REALM).stream()
                .filter(flow -> flow.getAlias().equals(createdSecondFlow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Created flow not found"));

        log.info("expected: {}", JsonMapperUtils.objectToJsonNode(createdSecondFlow));
        log.info("actual: {}", JsonMapperUtils.objectToJsonNode(importedSecondFlow));

        JsonNode expected = JsonMapperUtils.objectToJsonNode(createdSecondFlow);
        JsonNode actual = JsonMapperUtils.objectToJsonNode(importedSecondFlow);
//        assertEquals(createdFlow, importedSecondFlow);
        assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    void testDoImport_FlowExistsInTargetAndInActualButDifferent_UpdateFlow() {
        List<AuthenticationFlow> actualFlows = Arrays.asList(createFlowConfig("flow1"),
                createFlowConfig("flow2"));
        List<AuthenticationFlow> targetFlows = Arrays.asList(createFlowConfig("flow1"),
                createFlowConfig("flow2"));

        addAuthExecution(targetFlows.get(1), "auth-cookie");
        addAuthExecution(targetFlows.get(1), "identity-provider-redirector");
        AuthenticationExecution idpExecution = (AuthenticationExecution) targetFlows.get(1).getSubFlowsAndExecutions().get(1);
        idpExecution.setConfig(createIdpAuthenticatorConfig("idp"));
        addAuthSubFLow(targetFlows.get(1), "sub-flow");

        flowImportService.doImport(TEST_REALM, actualFlows, targetFlows);

        // Assert that the updated flow was updated
        AuthenticationFlow updatedFlow = targetFlows.get(1);
        AuthenticationFlow importedFlow = flowExportService.getAllFlows(TEST_REALM).stream()
                .filter(flow -> flow.getAlias().equals(updatedFlow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Updated flow not found"));

        log.info("expected: {}", JsonMapperUtils.objectToJsonNode(updatedFlow));
        log.info("actual: {}", JsonMapperUtils.objectToJsonNode(importedFlow));

        assertEquals(updatedFlow, importedFlow);
    }

    private AuthenticatorConfigConfig createIdpAuthenticatorConfig(String alias) {
        AuthenticatorConfigConfig authConfig = new AuthenticatorConfigConfig();
        authConfig.setAlias(alias);
        authConfig.setConfig(Map.of("defaultProvider","test-oidc"));
        return authConfig;
    }

    @Test
    void testDoImport_FlowExistsInActualButNotInTarget_DeleteFlow() {
        List<AuthenticationFlow> actualFlows = Arrays.asList(createFlowConfig("flow1"), createFlowConfig("flow2"));
        List<AuthenticationFlow> targetFlows = new ArrayList<>();

        flowImportService.doImport(TEST_REALM, actualFlows, targetFlows);

        // Assert that the deleted flow was removed
        AuthenticationFlow deletedFlow1 = actualFlows.get(0);
        Optional<AuthenticationFlow> importedFlow1 = flowExportService.getAllFlows(TEST_REALM)
                .stream().filter(flow -> deletedFlow1.getAlias().equals(flow.getAlias())).findFirst();

        assertTrue(importedFlow1.isEmpty());

        AuthenticationFlow deletedFlow2 = actualFlows.get(1);
        Optional<AuthenticationFlow> importedFlow2 = flowExportService.getAllFlows(TEST_REALM)
                .stream().filter(flow -> deletedFlow2.getAlias().equals(flow.getAlias())).findFirst();

        assertTrue(importedFlow2.isEmpty());
    }

    // Helper method to create an AuthenticationFlowConfig
    private AuthenticationFlow createFlowConfig(String alias) {
        AuthenticationFlow flowConfig = new AuthenticationFlow();
        flowConfig.setAlias(alias);
        flowConfig.setTopLevel(true);
        flowConfig.setProviderId("basic-flow");
        flowConfig.setSubFlowsAndExecutions(new ArrayList<>());
        return flowConfig;
    }

    private void addAuthExecution(AuthenticationFlow config, String authenticator) {
        config.getSubFlowsAndExecutions().add(createExecution(authenticator));
    }

    private void addAuthSubFLow(AuthenticationFlow config, String subFlowAlias) {
        config.getSubFlowsAndExecutions().add(createSubFlowExecution(subFlowAlias));
    }

    private AuthenticationSubFlow createSubFlowExecution(String subFlowAlias) {
        AuthenticationSubFlow subFlow = new AuthenticationSubFlow();
        subFlow.setAlias(subFlowAlias);
        subFlow.setAuthenticationFlow(true);
        subFlow.setRequirement("REQUIRED");
        subFlow.setProviderId("basic-flow");
        subFlow.setSubFlowsAndExecutions(new ArrayList<>());
        return subFlow;
    }

    private AuthenticationExecution createExecution(String authenticator) {
        AuthenticationExecution execution = new AuthenticationExecution();
        execution.setProviderId(authenticator);
        execution.setAuthenticationFlow(false);
        execution.setRequirement("DISABLED");
        return execution;
    }

}


