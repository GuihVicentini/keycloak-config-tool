package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticationFlowMapper;
import com.guihvicentini.keycloakconfigtool.models.*;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AuthenticationFlowImportService {

    private final AuthenticationManagementResourceAdapter resourceAdapter;

    private final AuthenticationFlowMapper flowMapper;

    public AuthenticationFlowImportService(AuthenticationManagementResourceAdapter resourceAdapter,
                                           AuthenticationFlowMapper flowMapper) {
        this.resourceAdapter = resourceAdapter;
        this.flowMapper = flowMapper;
    }

    public void doImport(String realm, List<AuthenticationFlow> actual, List<AuthenticationFlow> target) {

        if(target.equals(actual)) {
            log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        var toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        var toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        var toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addFlows(realm, toBeAdded);
        deleteFlows(realm, toBeDeleted);
        updateFlows(realm, toBeUpdated);

    }

    private void updateFlows(String realm, List<AuthenticationFlow> flows) {
        flows.forEach(flow -> updateFlow(realm, flow));
    }

    private void updateFlow(String realm, AuthenticationFlow flow) {
        deleteFlow(realm, flow);
        addFlow(realm, flow);
    }

    private void deleteFlows(String realm, List<AuthenticationFlow> flows) {
        flows.forEach(flow -> deleteFlow(realm, flow));
    }

    private void deleteFlow(String realm, AuthenticationFlow flow) {
        if(!flow.isBuiltIn()) {
            resourceAdapter.deleteFlow(realm, flow.getAlias());
        }
    }

    private void addFlows(String realm, List<AuthenticationFlow> authenticationFlows) {
        authenticationFlows.forEach(authenticationFlow -> addFlow(realm, authenticationFlow));
    }

    private void addFlow(String realm, AuthenticationFlow authenticationFlow) {
        resourceAdapter.createFlow(realm, flowMapper.mapToRepresentation(authenticationFlow));
        addExecutionOrSubFlow(realm, authenticationFlow.getAlias(), authenticationFlow.getSubFlowsAndExecutions());
    }

    private void addExecutionOrSubFlow(String realm, String alias, List<FlowElement> subFlowsAndExecutions) {
        subFlowsAndExecutions.forEach(subFlowOrExecution -> {
            if (subFlowOrExecution.isAuthenticationFlow()) {
                createSubFlow(realm, alias, subFlowOrExecution);
            } else {
                createExecution(realm, alias, subFlowOrExecution);
            }
        });
    }

    private void createExecution(String realm, String flowAlias, FlowElement execution) {
        AuthenticationExecution authenticationExecution = (AuthenticationExecution) execution;
        resourceAdapter.addExecution(realm, flowAlias, authenticationExecution);
        resourceAdapter.updateExecution(realm, flowAlias, authenticationExecution);
        if (authenticationExecution.getConfig() != null) {
            createOrUpdateAuthenticatorConfig(realm, flowAlias, authenticationExecution.getAlias(), authenticationExecution.getConfig());
        }
    }

    private void createOrUpdateAuthenticatorConfig(String realm, String flowAlias, String executionAlias,
                                                   AuthenticatorConfigConfig authenticatorConfig) {
        AuthenticationExecutionInfoRepresentation execution = resourceAdapter.getAuthenticationExecutionByName(realm, flowAlias,
                executionAlias);
        if(execution.getAuthenticationConfig() == null) {
            resourceAdapter.createNewExecutionConfig(realm, execution.getId(), flowMapper.mapToRepresentation(authenticatorConfig));
        } else {
            resourceAdapter.updateExecutionConfig(realm, execution.getAuthenticationConfig(), flowMapper.mapToRepresentation(authenticatorConfig));
        }
    }

    private void createSubFlow(String realm, String alias, FlowElement subFlow) {
        AuthenticationSubFlow authenticationSubFlow = (AuthenticationSubFlow) subFlow;
        resourceAdapter.addExecutionFlow(realm, alias, authenticationSubFlow);
        resourceAdapter.updateExecution(realm, alias, authenticationSubFlow);
        // add executions for subFlow recursively
        addExecutionOrSubFlow(realm, authenticationSubFlow.getAlias(), authenticationSubFlow.getSubFlowsAndExecutions());
    }

}
