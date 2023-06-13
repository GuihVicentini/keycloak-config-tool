package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticationFlowConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationExecutionExportConfig;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlowConfig;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthenticationFlowImportService {

    private final AuthenticationManagementResourceAdapter resourceAdapter;
     private final AuthenticationFlowConfigMapper flowConfigMapper;

    public AuthenticationFlowImportService(AuthenticationManagementResourceAdapter resourceAdapter,
                                           AuthenticationFlowConfigMapper flowConfigMapper) {
        this.resourceAdapter = resourceAdapter;
        this.flowConfigMapper = flowConfigMapper;
    }


    public void doImport(String realm, List<AuthenticationFlowConfig> actual, List<AuthenticationFlowConfig> target) {

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

    private void deleteFlows(String realm, List<AuthenticationFlowConfig> flows) {
        flows.stream().filter(AuthenticationFlowConfig::isTopLevel)
                .forEach(flow -> deleteFlow(realm, flow));
    }

    private void deleteFlow(String realm, AuthenticationFlowConfig flow) {
        resourceAdapter.deleteFlow(realm, flowConfigMapper.mapToRepresentation(flow));
    }

    private void updateFlows(String realm, List<AuthenticationFlowConfig> flows) {
        flows.stream().filter(AuthenticationFlowConfig::isTopLevel)
                .forEach(flow -> updateFlow(realm, flow));
    }

    private void updateFlow(String realm, AuthenticationFlowConfig flow) {
        resourceAdapter.updateFlow(realm, flowConfigMapper.mapToRepresentation(flow));
    }

    private void addFlows(String realm, List<AuthenticationFlowConfig> flows) {
        List<AuthenticationFlowConfig> topFlows = flows.stream().filter(AuthenticationFlowConfig::isTopLevel).toList();
        flows.removeAll(topFlows);

        // create top flows and add subflows and executions for each flow
        topFlows.forEach(flow -> addFlow(realm, flow, flows));
    }

    private void addFlow(String realm, AuthenticationFlowConfig flow, List<AuthenticationFlowConfig> subFlows) {
        resourceAdapter.createFlow(realm, flowConfigMapper.mapToRepresentation(flow));
        addExecutionOrSubFlow(realm, flow, subFlows);
        updateFlow(realm, flow);
    }

    private void addExecutionOrSubFlow(String realm, AuthenticationFlowConfig flow, List<AuthenticationFlowConfig> subFlows) {
        flow.getAuthenticationExecutions().forEach(execution -> {
            if(execution.isAuthenticatorFlow()) {
                addExecutionFlow(realm, flow.getAlias(), execution, subFlows);
            } else {
                addExecution(realm, flow.getAlias(), execution);
            }
        });
    }

    private void addExecutionFlow(String realm, String flowAlias, AuthenticationExecutionExportConfig execution,
                                  List<AuthenticationFlowConfig> subFlows) {
        var subFlow = subFlows.stream()
                .filter(flow -> execution.getFlowAlias().equals(flow.getAlias()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("Subflow: %s not found", execution.getFlowAlias()));

        resourceAdapter.addExecutionFlow(realm, flowAlias, subFlow);

        // update subFlow execution requirements
        resourceAdapter.updateExecution(realm, flowAlias, execution);

        // add executions for subFlow
        addExecutionOrSubFlow(realm, subFlow, subFlows);
    }


    private AuthenticationExecutionInfoRepresentation findSubflowByAlias(String realm, String flowAlias, String subFlowAlias) {
        return resourceAdapter.getAuthenticationExecutions(realm, flowAlias)
                .stream().filter(execution -> subFlowAlias.equals(execution.getDisplayName()) || subFlowAlias.equals(execution.getProviderId()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("Subflow: %s not found", subFlowAlias));
    }


    private void addExecution(String realm, String alias, AuthenticationExecutionExportConfig execution) {
        resourceAdapter.addExecution(realm, alias, execution);
        // update execution to correct requirement and other values
        resourceAdapter.updateExecution(realm, alias, execution);
    }


    private List<AuthenticationFlowConfig> getFlowWithoutSubflows(List<AuthenticationFlowConfig> flows) {
        return flows.stream().filter(flow -> flow.getAuthenticationExecutions()
                .stream().noneMatch(AuthenticationExecutionExportConfig::isAuthenticatorFlow))
                .collect(Collectors.toList());
    }

    public String getFlowIdByAlias(String realm, String flowAlias) {
        return resourceAdapter.getFlowIdByAlias(realm, flowAlias);
    }

    public String getFlowAliasById(String realm, String uuid) {
        return resourceAdapter.getFlow(realm, uuid).getAlias();
    }

    public List<AuthenticationFlowConfig> getAllFlows(String realm) {
        return resourceAdapter.getFlows(realm).stream().map(flowConfigMapper::mapToConfig).toList();
    }

    public List<AuthenticationExecutionExportConfig> getAllFlowExecutions(String realm, String flowAlias) {
        return resourceAdapter.getAuthenticationExecutions(realm, flowAlias)
                .stream().map(this::mapToConfig).toList();
    }

    private AuthenticationExecutionExportConfig mapToConfig(AuthenticationExecutionInfoRepresentation representation) {
        AuthenticationExecutionExportConfig config = new AuthenticationExecutionExportConfig();
        config.setAuthenticatorConfig(representation.getAuthenticationConfig());
        config.setAuthenticator(representation.getProviderId());
        if(null == representation.getAuthenticationFlow()) {
            config.setAuthenticatorFlow(false);
        } else {
            config.setAuthenticatorFlow(representation.getAuthenticationFlow());
        }
        config.setRequirement(representation.getRequirement());
        config.setFlowAlias(representation.getDisplayName());
        return config;
    }
}
