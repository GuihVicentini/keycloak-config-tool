package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.models.*;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.*;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/authentication
 *
 */

@Service
@Slf4j
public class AuthenticationManagementResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;

    public AuthenticationManagementResourceAdapter(RealmResourceAdapter realmResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
    }

    // ----------------- Required Actions ----------------------------

    /**
     * GET /authentication/register-required-action/
     */
    public List<RequiredActionProviderRepresentation> getRequiredActions(String realm) {
        return getResource(realm).getRequiredActions();
    }

    /**
     * GET /authentication/register-required-action/{alias}
     */
    public RequiredActionProviderRepresentation getRequiredActionByAlias(String realm, String alias) {
        return getResource(realm).getRequiredAction(alias);
    }


    /**
     * POST /authentication/register-required-action
     */
    public void createRequiredAction(String realm, RequiredActionProviderRepresentation representation) {
        RequiredActionProviderSimpleRepresentation simpleRepresentation = new RequiredActionProviderSimpleRepresentation();
        simpleRepresentation.setName(representation.getAlias());
        simpleRepresentation.setProviderId(representation.getProviderId());

        // only name and providerId are required to create.
        getResource(realm).registerRequiredAction(simpleRepresentation);

        // TODO maybe move this logic to service
        // updated the new created RequiredAction to fully configure it
        updateRequiredAction(realm, representation.getAlias(), representation);
    }

    /**
     * PUT /authentication/register-required-action/{alias}
     */
    public void updateRequiredAction(String realm, String alias, RequiredActionProviderRepresentation representation) {
        getResource(realm).updateRequiredAction(alias, representation);
    }

    /**
     * DELETE /authentication/register-required-action/{alias}
     */
    public void deleteRequiredAction(String realm, String alias ) {
        getResource(realm).removeRequiredAction(alias);
    }


    // ----------------- FLOWS ----------------------------

    /**
     * GET /authentication/flows
     */
    public List<AuthenticationFlowRepresentation> getFlows(String realm) {
        return getResource(realm).getFlows();
    }

    /**
     * GET /authentication/flows/{id}
     */
    public AuthenticationFlowRepresentation getFlow(String realm, String id) {
        return getResource(realm).getFlow(id);
    }

    /**
     * Adaptation to get flow UUID by alias using the GET /authentication/flows/{id}
     */
    public String getFlowIdByAlias(String realm, String flowAlias) {
        return getFlowByAlias(realm, flowAlias).getId();
    }

    /**
     * Adaptation to get flow by alias using the GET /authentication/flows/{id}
     */
    public AuthenticationFlowRepresentation getFlowByAlias(String realm, String flowAlias) {
        return getFlows(realm).stream().filter(flow -> flow.getAlias().equals(flowAlias))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("Authentication Flow %s not found", flowAlias));
    }

    /**
     * POST /authentication/flows
     */
    public String createFlow(String realm, AuthenticationFlowRepresentation representation) {
        try(Response response = getResource(realm).createFlow(representation)){
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create authentication flow: %s\n error message: %s",
                    e, representation.getAlias(), errorMessage);
        }
    }

    /**
     * PUT /authentication/flows/{id}
     */
    public void updateFlow(String realm, AuthenticationFlowRepresentation representation) {
        String flowId = getFlowIdByAlias(realm, representation.getAlias());
        representation.setId(flowId);
        getResource(realm).updateFlow(representation.getId(), representation);
    }

    /**
     * DELETE /authentication/flows/{id}
     */
    public void deleteFlow(String realm, String flowAlias) {
        String flowId = getFlowIdByAlias(realm, flowAlias);
        getResource(realm).deleteFlow(flowId);
    }

    // ----------------- FLOW EXECUTIONS ----------------------------

    /**
     * GET /flows/{flowAlias}/executions
     */
    public List<AuthenticationExecutionInfoRepresentation> getAuthenticationExecutions(String realm, String flowAlias) {
        return getResource(realm).getExecutions(flowAlias);
    }

    /**
     * GET /executions/{executionId}
     */
    public AuthenticationExecutionRepresentation getExecutionById(String realm, String executionId) {
        return getResource(realm).getExecution(executionId);
    }

    /**
     * POST /executions/{executionId}/config
     */
    public String createNewExecutionConfig(String realm, String executionId ,AuthenticatorConfigRepresentation representation) {
        try(Response response = getResource(realm).newExecutionConfig(executionId, representation)){
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create execution config: %s\n error message: %s",
                    e, representation.getAlias(), errorMessage);
        }
    }

    /**
     * GET /config/{id}
     */
    public AuthenticatorConfigRepresentation getExecutionConfigById(String realm, String configId) {
        return getResource(realm).getAuthenticatorConfig(configId);
    }

    /**
     * PUT /config/{id}
     */
    public void updateExecutionConfig(String realm, String configId,
                                                                   AuthenticatorConfigRepresentation representation) {
        getResource(realm).updateAuthenticatorConfig(configId, representation);
    }

    /**
     * DELETE /config/{id}
     */
    public void removeExecutionConfigById(String realm, String configId) {
        getResource(realm).removeAuthenticatorConfig(configId);
    }

    /**
     * adaptation to get execution representation based on name GET /flows/{flowAlias}/executions
     */
    public AuthenticationExecutionInfoRepresentation getAuthenticationExecutionByName(String realm, String flowAlias, String executionAlias) {
        return getAuthenticationExecutions(realm, flowAlias)
                // if subflow it should match the display name else it matches the providerId
                .stream().filter(exec -> executionAlias.equals(exec.getDisplayName()) || executionAlias.equals(exec.getProviderId()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("Execution: %s not found", executionAlias));
    }

    /**
     * POST /flows/{flowAlias}/executions/flows
     */
    public void addExecutionFlow(String realm, String flowAlias, AuthenticationSubFlow subFlow) {
        Map<String, String> request = new HashMap<>();
        request.put("alias", subFlow.getAlias());
        request.put("type", subFlow.getProviderId());
        request.put("description", subFlow.getDescription());
        getResource(realm).addExecutionFlow(flowAlias, request);
    }

    /**
     * POST /flows/{flowAlias}/executions/execution
     */
    public void addExecution(String realm, String flowAlias, AuthenticationExecution config) {
        Map<String, String> request = new HashMap<>();
        request.put("provider", config.getProviderId());
        getResource(realm).addExecution(flowAlias, request);
    }

    /**
     * PUT /flows/{flowAlias}/executions
     */
    public void updateExecution(String realm, String flowAlias, FlowElement config) {
        String executionSearch = config.getAlias();
        AuthenticationExecutionInfoRepresentation execution = getAuthenticationExecutionByName(realm, flowAlias, executionSearch);
        execution.setRequirement(config.getRequirement());
        updateExecution(realm, flowAlias, execution);
    }

    /**
     * PUT /flows/{flowAlias}/executions
     */
    private void updateExecution(String realm, String flowAlias, AuthenticationExecutionInfoRepresentation representation) {
        getResource(realm).updateExecutions(flowAlias, representation);
    }

    /**
     * adaptation to remove authentication execution based on name
     * DELETE /executions/{executionId}
     */
    public void removeExecution(String realm, String flowAlias, String executionAlias) {
        AuthenticationExecutionInfoRepresentation execution = getAuthenticationExecutionByName(realm, flowAlias, executionAlias);
        getResource(realm).removeExecution(execution.getId());
    }

    /**
     * DELETE /executions/{executionId}
     */
    public void removeExecution(String realm, String executionId) {
        getResource(realm).removeExecution(executionId);
    }


    /**
     * resource for path /authentication
     */
    private AuthenticationManagementResource getResource(String realm) {
        return realmResourceAdapter.getResource(realm).flows();
    }

}
