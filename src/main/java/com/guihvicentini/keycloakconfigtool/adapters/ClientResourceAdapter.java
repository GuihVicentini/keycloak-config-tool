package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/clients
 *
 */

@Service
@Slf4j
public class ClientResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;

    public ClientResourceAdapter(RealmResourceAdapter realmResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
    }

    /**
     * GET /clients
     */
    public List<ClientRepresentation> getClients(String realm) {
        return getResource(realm).findAll();
    }

    /**
     * GET /clients?clientId={clientId}
     */
    public ClientRepresentation getClientByClientId(String realm, String clientId) {
        List<ClientRepresentation> clients = getResource(realm).findByClientId(clientId);
        if(clients.size() > 1) {
            throw new KeycloakAdapterException("Multiple clients found with clientId: %s", clientId);
        }

        Optional<ClientRepresentation> client = clients.stream().findFirst();

        if(client.isEmpty()) {
            throw new KeycloakAdapterException("Client: %s not found", clientId);
        }

        return client.get();
    }

    /**
     * POST /clients
     */
    public String create(String realm, ClientRepresentation representation) {
        try(Response response = getResource(realm).create(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create client: %s\n error message: %s",
                    e, representation.getClientId(), errorMessage);
        }
    }

    /**
     * PUT /clients/{id}
     */
    public void update(String realm, ClientRepresentation representation) {
        getClientResourceByClientId(realm, representation.getClientId()).update(representation);
    }

    /**
     * DELETE /clients/{id}
     */
    public void delete(String realm, String clientId) {
        getClientResourceByClientId(realm, clientId).remove();
    }


    /**
     * GET /clients/{id}/protocol-mappers/models
     */
    public List<ProtocolMapperRepresentation> getAllProtocolMappers(String realm, String clientId) {
        return getProtocolMapperResource(realm, clientId).getMappers();
    }

    /**
     * adaptation to get protocolMapper by name using GET /clients/{id}/protocol-mappers/models
     */
    public ProtocolMapperRepresentation getProtocolMapperByName(String realm, String clientId, String mapperName) {
        return getAllProtocolMappers(realm, clientId)
                .stream().filter(mapper -> mapperName.equals(mapper.getName()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("ProtocolMapper: %s not found.", mapperName));
    }

    /**
     * POST /clients/{id}/protocol-mappers/models
     */
    public String createProtocolMapper(String realm, String clientId, ProtocolMapperRepresentation representation) {
        try(Response response = getProtocolMapperResource(realm, clientId).createMapper(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create protocolMapper: %s for client: %s\n error message: %s",
                    e, representation.getName(), clientId, errorMessage);
        }
    }

    /**
     * PUT /clients/{id}/protocol-mappers/models/{id}
     */
    public void updateProtocolMapper(String realm, String clientId, ProtocolMapperRepresentation representation) {
        String mapperId = getProtocolMapperByName(realm, clientId, representation.getName()).getId();
        representation.setId(mapperId);
        getProtocolMapperResource(realm, clientId).update(mapperId, representation);
    }

    /**
     * DELETE /clients/{id}/protocol-mappers/models/{id}
     */
    public void deleteProtocolMapper(String realm, String clientScopeName, String mapperName) {
        String mapperId = getProtocolMapperByName(realm, clientScopeName, mapperName).getId();
        getProtocolMapperResource(realm, clientScopeName).delete(mapperId);
    }


    /**
     * resource for path /clients/{id}/protocol-mappers
     */
    private ProtocolMappersResource getProtocolMapperResource(String realm, String clientId) {
        return getClientResourceByClientId(realm, clientId).getProtocolMappers();
    }

    /**
     * adaptation to get resource for path /clients/{id} based on clientId
     */
    private ClientResource getClientResourceByClientId(String realm, String clientId) {
        return getClientResource(realm, getClientByClientId(realm, clientId).getId());
    }

    /**
     * resource for path /clients/{id}
     */
    private ClientResource getClientResource(String realm, String id) {
        return getResource(realm).get(id);
    }

    /**
     * resource for path /clients/
     */
    private ClientsResource getResource(String realm) {
        return realmResourceAdapter.getResource(realm).clients();
    }


    /**
     * resource for path /clients/{id}/roles/
     */
    public RolesResource getClientRolesResource(String realm, String clientUuid) {
        return getClientResource(realm, clientUuid).roles();
    }

    /**
     * PUT /clients/{id}/default-client-scopes/{clientScopeId}
     */
    public void addDefaultClientScope(String realm, String clientId, String clientScope) {
        getClientResourceByClientId(realm, clientId).addDefaultClientScope(realmResourceAdapter.getDefaultClientScopeId(realm, clientScope));
    }

    /**
     * DELETE /clients/{id}/default-client-scopes/{clientScopeId}
     */
    public void removeDefaultClientScope(String realm, String clientId, String clientScope) {
        getClientResourceByClientId(realm, clientId).removeDefaultClientScope(realmResourceAdapter.getDefaultClientScopeId(realm, clientScope));
    }

    /**
     * PUT /clients/{id}/optional-client-scopes/{clientScopeId}
     */
    public void addOptionalClientScope(String realm, String clientId, String clientScope) {
        getClientResourceByClientId(realm, clientId).addOptionalClientScope(realmResourceAdapter.getDefaultClientScopeId(realm, clientScope));
    }

    /**
     * DELETE /clients/{id}/optional-client-scopes/{clientScopeId}
     */
    public void removeOptionalClientScope(String realm, String clientId, String clientScope) {
        getClientResourceByClientId(realm, clientId).removeOptionalClientScope(realmResourceAdapter.getDefaultClientScopeId(realm, clientScope));
    }

}
