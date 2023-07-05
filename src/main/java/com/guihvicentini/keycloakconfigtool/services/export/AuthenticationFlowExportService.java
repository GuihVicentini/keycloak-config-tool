package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticationFlowMapper;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticatorConfigConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.*;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationFlowExportService {

    private final AuthenticationManagementResourceAdapter resourceAdapter;

    private final AuthenticationFlowMapper flowMapper;

    private final AuthenticatorConfigConfigMapper configMapper;


    public AuthenticationFlowExportService(AuthenticationManagementResourceAdapter resourceAdapter,
                                           AuthenticationFlowMapper flowMapper,
                                           AuthenticatorConfigConfigMapper configMapper) {
        this.resourceAdapter = resourceAdapter;
        this.flowMapper = flowMapper;
        this.configMapper = configMapper;
    }

    public String getFlowAliasById(String realm, String uuid) {
        return resourceAdapter.getFlow(realm, uuid).getAlias();
    }

    public String getFlowIdByAlias(String realm, String flowAlias) {
        return resourceAdapter.getFlowIdByAlias(realm, flowAlias);
    }

    public List<AuthenticationFlow> getAllFlows(String realm) {
        List<AuthenticationFlowRepresentation> representations = resourceAdapter.getFlows(realm);

        return representations.stream()
                .filter(AuthenticationFlowRepresentation::isTopLevel)
                .filter(flow -> !flow.isBuiltIn())
                .map(flowMapper::mapToConfig)
                .peek(f -> f.setSubFlowsAndExecutions(getSubFlowOrExecution(realm, f.getAlias())))
                .collect(Collectors.toList());
    }

    private List<FlowElement> getSubFlowOrExecution(String realm, String alias) {
        List<AuthenticationExecutionInfoRepresentation> executions = resourceAdapter.getAuthenticationExecutions(realm, alias);

        return executions.stream()
                // only map the top level executions as the other ones will be mapped with the subflows.
                .filter(exec -> exec.getLevel() == 0)
                .map(exec -> {
                    if (exec.getAuthenticationFlow() != null && exec.getAuthenticationFlow()) {
                        return createSubFlow(realm, exec);
                    } else {
                        return createExecution(realm, exec);
                    }
                })
                .collect(Collectors.toList());
    }

    private FlowElement createExecution(String realm, AuthenticationExecutionInfoRepresentation exec) {
        AuthenticationExecution execution = new AuthenticationExecution();

        execution.setAlias(exec.getAlias());
        execution.setProviderId(exec.getProviderId());
        execution.setAuthenticationFlow(false);
        execution.setRequirement(exec.getRequirement());

        if(exec.getAuthenticationConfig() != null) {
            AuthenticatorConfigRepresentation executionConfig = resourceAdapter.getExecutionConfigById(realm, exec.getAuthenticationConfig());
            AuthenticatorConfigConfig execConfig = configMapper.mapToConfig(executionConfig);
            execution.setConfig(execConfig);
        }

        return execution;
    }

    private FlowElement createSubFlow(String realm, AuthenticationExecutionInfoRepresentation exec) {
        AuthenticationSubFlow authenticationSubFlow = new AuthenticationSubFlow();
        AuthenticationFlowRepresentation subFlowRepresentation = resourceAdapter.getFlow(realm, exec.getFlowId());

        authenticationSubFlow.setAlias(subFlowRepresentation.getAlias());
        authenticationSubFlow.setDescription(subFlowRepresentation.getDescription());
        authenticationSubFlow.setProviderId(subFlowRepresentation.getProviderId());
        authenticationSubFlow.setAuthenticationFlow(true);
        authenticationSubFlow.setRequirement(exec.getRequirement());
        authenticationSubFlow.setSubFlowsAndExecutions(getSubFlowOrExecution(realm, authenticationSubFlow.getAlias()));

        return authenticationSubFlow;
    }


}
