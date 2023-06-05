package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationExecutionExportConfig;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlowConfig;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class AuthenticationFlowImportServiceTest extends AbstractIntegrationTest {

    @Autowired
    AuthenticationFlowImportService flowImportService;

    @Autowired
    AuthenticationManagementResourceAdapter resourceAdapter;



    @Test
    public void getAllFlows() {
        var flows = flowImportService.getAllFlows(TEST_REALM);
        log.info("Flows: {}", JsonMapperUtils.objectToJsonNode(flows).toPrettyString());
    }

    @Test
    public void getAllFlows_2() {
        var flows = resourceAdapter.getFlows(TEST_REALM);
        log.info("Flows: {}", JsonMapperUtils.objectToJsonNode(flows).toPrettyString());
    }

    @Test
    public void getFlowExecutions() {
        var flows = resourceAdapter.getAuthenticationExecutions(TEST_REALM, "test-flow");
        log.info("FlowsExecutions: {}", JsonMapperUtils.objectToJsonNode(flows).toPrettyString());
    }

    @Test
    public void getFlowExecutionsConfig() {
        var flows = flowImportService.getAllFlowExecutions(TEST_REALM, "test-flow");
        log.info("FlowsExecutions: {}", JsonMapperUtils.objectToJsonNode(flows).toPrettyString());
    }


    @Test
    @Order(1)
    void testDoImport_FlowExistsInTargetButNotInActual_CreateFlow() {
        List<AuthenticationFlowConfig> actualFlows = new ArrayList<>();
        List<AuthenticationFlowConfig> targetFlows = Arrays.asList(createFlowConfig("flow1"), createFlowConfig("flow2"));

        flowImportService.doImport(TEST_REALM, actualFlows, targetFlows);

        // Assert that the missing flow was created
        AuthenticationFlowConfig createdFlow = targetFlows.get(0);
        AuthenticationFlowConfig importedFlow = flowImportService.getAllFlows(TEST_REALM).stream()
                .filter(flow -> flow.getAlias().equals(createdFlow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Created flow not found"));

        assertEquals(createdFlow, importedFlow);

        AuthenticationFlowConfig createdSecondFlow = targetFlows.get(1);
        AuthenticationFlowConfig importedSecondFlow = flowImportService.getAllFlows(TEST_REALM).stream()
                .filter(flow -> flow.getAlias().equals(createdSecondFlow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Created flow not found"));

        log.info("expected: {}", JsonMapperUtils.objectToJsonNode(createdSecondFlow));
        log.info("actual: {}", JsonMapperUtils.objectToJsonNode(importedSecondFlow));

        // TODO WTF Objects are equal but still getting error
        assertEquals(createdFlow, importedSecondFlow);
    }

    @Test
    @Order(2)
    void testDoImport_FlowExistsInTargetAndInActualButDifferent_UpdateFlow() {
        List<AuthenticationFlowConfig> actualFlows = Arrays.asList(createFlowConfig("flow1"), createFlowConfig("flow2"));
        List<AuthenticationFlowConfig> targetFlows = Arrays.asList(createFlowConfig("flow1"), createFlowConfig("flow2"));
        addAuthExecution(targetFlows.get(1), "auth-cookie");

        flowImportService.doImport(TEST_REALM, actualFlows, targetFlows);

        // Assert that the updated flow was updated
        AuthenticationFlowConfig updatedFlow = targetFlows.get(1);
        AuthenticationFlowConfig importedFlow = flowImportService.getAllFlows(TEST_REALM).stream()
                .filter(flow -> flow.getAlias().equals(updatedFlow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Updated flow not found"));

        log.info("expected: {}", JsonMapperUtils.objectToJsonNode(updatedFlow));
        log.info("actual: {}", JsonMapperUtils.objectToJsonNode(importedFlow));

        assertEquals(updatedFlow, importedFlow);
    }

    @Test
    void testDoImport_FlowExistsInActualButNotInTarget_DeleteFlow() {
        List<AuthenticationFlowConfig> actualFlows = Arrays.asList(createFlowConfig("flow1"), createFlowConfig("flow2"));
        List<AuthenticationFlowConfig> targetFlows = Arrays.asList(createFlowConfig("flow1"));

        flowImportService.doImport(TEST_REALM, actualFlows, targetFlows);

        // Assert that the deleted flow was removed
        AuthenticationFlowConfig deletedFlow = actualFlows.get(1);
        Optional<AuthenticationFlowConfig> importedFlows = flowImportService.getAllFlows(TEST_REALM)
                .stream().filter(flow -> deletedFlow.getAlias().equals(flow.getAlias())).findFirst();

        // TODO update is not adding/updating/deleting authentication executions
        assertTrue(importedFlows.isEmpty());
    }

    // Helper method to create an AuthenticationFlowConfig
    private AuthenticationFlowConfig createFlowConfig(String alias) {
        AuthenticationFlowConfig flowConfig = new AuthenticationFlowConfig();
        flowConfig.setAlias(alias);
        flowConfig.setTopLevel(true);
        flowConfig.setProviderId("basic-flow");
        flowConfig.setAuthenticationExecutions(new ArrayList<>());
        // Set other properties as needed
        return flowConfig;
    }

    private void addAuthExecution(AuthenticationFlowConfig config, String authenticator) {
        config.getAuthenticationExecutions().add(createExecution(authenticator));
    }

    private void addAuthSubFLow(AuthenticationFlowConfig config, String subFlowAlias) {
        config.getAuthenticationExecutions().add(createSubFlow(subFlowAlias));
    }

    private AuthenticationExecutionExportConfig createSubFlow(String subFlowAlias) {
        AuthenticationExecutionExportConfig subFlow = new AuthenticationExecutionExportConfig();
        subFlow.setFlowAlias(subFlowAlias);
        subFlow.setAuthenticatorFlow(true);
        subFlow.setRequirement("DISABLED");
        return subFlow;
    }

    private AuthenticationExecutionExportConfig createExecution(String authenticator) {
        AuthenticationExecutionExportConfig execution = new AuthenticationExecutionExportConfig();
        execution.setFlowAlias(authenticator);
        execution.setAuthenticatorFlow(false);
        execution.setRequirement("DISABLED");
        return execution;
    }

}


