package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticationFlowMapper;
import com.guihvicentini.keycloakconfigtool.models.*;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
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

        addFlows2(realm, toBeAdded);
        deleteFlows2(realm, toBeDeleted);
        updateFlows2(realm, toBeUpdated);

    }

    private void updateFlows2(String realm, List<AuthenticationFlow> flows) {
        flows.forEach(flow -> updateFlow(realm, flow));
    }

    private void updateFlow(String realm, AuthenticationFlow flow) {
        deleteFlow(realm, flow);
        addFlow(realm, flow);
    }

    private void deleteFlows2(String realm, List<AuthenticationFlow> flows) {
        flows.forEach(flow -> deleteFlow(realm, flow));
    }

    private void deleteFlow(String realm, AuthenticationFlow flow) {
        resourceAdapter.deleteFlow(realm, flow.getAlias());
    }

    private void addFlows2(String realm, List<AuthenticationFlow> authenticationFlows) {
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

    private void createExecution(String realm, String alias, FlowElement execution) {
        AuthenticationExecution authenticationExecution = (AuthenticationExecution) execution;
        resourceAdapter.addExecution(realm, alias, authenticationExecution);
        resourceAdapter.updateExecution(realm, alias, authenticationExecution);

    }

    private void createSubFlow(String realm, String alias, FlowElement subFlow) {
        AuthenticationSubFlow authenticationSubFlow = (AuthenticationSubFlow) subFlow;
        resourceAdapter.addExecutionFlow(realm, alias, authenticationSubFlow);
        resourceAdapter.updateExecution(realm, alias, authenticationSubFlow);
        // add executions for subFlow recursively
        addExecutionOrSubFlow(realm, authenticationSubFlow.getAlias(), authenticationSubFlow.getSubFlowsAndExecutions());
    }

}
