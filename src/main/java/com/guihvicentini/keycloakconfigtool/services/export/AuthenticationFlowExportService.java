package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.AuthenticationFlowConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationExecutionExportConfig;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlowConfig;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
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

    public List<AuthenticationFlowRepresentation> getAuthFlows(String realm) {
        return resourceAdapter.getFlows(realm);
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
