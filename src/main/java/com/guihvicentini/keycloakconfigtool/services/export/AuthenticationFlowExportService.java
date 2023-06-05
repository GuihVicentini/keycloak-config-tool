package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticationFlowConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlowConfig;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class AuthenticationFlowExportService {

    private final AuthenticationManagementResourceAdapter resourceAdapter;

    private final AuthenticationFlowConfigMapper authenticationFlowConfigMapper;


    public AuthenticationFlowExportService(AuthenticationManagementResourceAdapter resourceAdapter,
                                           AuthenticationFlowConfigMapper authenticationFlowConfigMapper) {
        this.resourceAdapter = resourceAdapter;
        this.authenticationFlowConfigMapper = authenticationFlowConfigMapper;
    }


    public List<AuthenticationFlowConfig> getAll(String realm) {
        List<AuthenticationFlowConfig> flows = resourceAdapter.getFlows(realm)
                .stream()
                .map(authenticationFlowConfigMapper::mapToConfig)
                .collect(Collectors.toList());

        List<AuthenticationFlowConfig> subFlows = flows.stream()
                .flatMap(flow -> findSubFlows(realm, flow.getAlias())).toList();

        flows.addAll(subFlows);
        return flows;
    }

    private Stream<AuthenticationFlowConfig> findSubFlows(String realm, String flowAlias) {
        var subFlowIds = resourceAdapter.getAuthenticationExecutions(realm, flowAlias)
                .stream()
                .filter(exec -> exec.getAuthenticationFlow() != null && exec.getAuthenticationFlow())
                .map(AuthenticationExecutionInfoRepresentation::getFlowId)
                .toList();

        return subFlowIds.stream()
                .flatMap(id -> {
                    var subflow = resourceAdapter.getFlow(realm, id);
//                    log.info("Subflow: {}", JsonMapperUtils.objectToJsonPrettyString(subflow));
                    var subflowConfig = authenticationFlowConfigMapper.mapToConfig(subflow);
                    return Stream.concat(Stream.of(subflowConfig), findSubFlows(realm, subflow.getAlias()));
                });
    }

    public String getFlowAliasById(String realm, String uuid) {
        return resourceAdapter.getFlow(realm, uuid).getAlias();
    }
}
