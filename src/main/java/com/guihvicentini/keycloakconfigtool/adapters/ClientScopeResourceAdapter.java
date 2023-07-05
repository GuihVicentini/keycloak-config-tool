package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.ClientScopeResource;
import org.keycloak.admin.client.resource.ClientScopesResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/client-scopes
 */

@Service
@Slf4j
public class ClientScopeResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;

    public ClientScopeResourceAdapter(RealmResourceAdapter realmResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
    }

    /**
     * GET /client-scopes
     */
    public List<ClientScopeRepresentation> getAllClientScopes(String realm) {
        return getResource(realm).findAll();
    }

    /**
     * adaptation to get clientScope by name using GET /client-scopes
     */
    public ClientScopeRepresentation getClientScopeByName(String realm, String name) {
        return getAllClientScopes(realm).stream()
                .filter(scope -> name.equals(scope.getName())).findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("ClientScope: %s not found", name));
    }

    /**
     * PUT /client-scopes
     */
    public String create(String realm, ClientScopeRepresentation representation) {
        try (Response response = getResource(realm).create(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create clientScope: %s\n error message: %s",
                    e, representation.getName(), errorMessage);
        }

    }

    /**
     * PUT /client-scopes/{id}
     */
    public void update(String realm, ClientScopeRepresentation representation) {
        getClientScopeResourceByName(realm, representation.getName()).update(representation);
    }

    /**
     * DELETE /client-scopes/{id}
     */
    public void delete(String realm, String name) {
        getClientScopeResourceByName(realm, name).remove();
    }

    /**
     * GET /client-scopes/{id}/protocol-mappers/models
     */
    public List<ProtocolMapperRepresentation> getAllProtocolMappers(String realm, String clientScopeName) {
        return getProtocolMappersResource(realm, clientScopeName).getMappers();
    }


    /**
     * adaptation to get protocolMapper by name using GET /client-scopes/{id}/protocol-mappers/models
     */
    public ProtocolMapperRepresentation getProtocolMapperByName(String realm, String clientScopeName, String mapperName) {
        return getAllProtocolMappers(realm, clientScopeName)
                .stream().filter(mapper -> mapperName.equals(mapper.getName()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("ProtocolMapper: %s not found.", mapperName));
    }

    /**
     * POST /client-scopes/{id}/protocol-mappers/models
     */
    public String createProtocolMapper(String realm, String clientScopeName, ProtocolMapperRepresentation representation){
        try (Response response = getProtocolMappersResource(realm, clientScopeName).createMapper(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create protocolMapper: %s for client: %s\n error message: %s",
                    e, representation.getName(), clientScopeName, errorMessage);
        }
    }

    /**
     * PUT /client-scopes/{id}/protocol-mappers/models/{id}
     */
    public void updateProtocolMapper(String realm, String clientScopeName, ProtocolMapperRepresentation representation) {
        String mapperId = getProtocolMapperByName(realm, clientScopeName, representation.getName()).getId();
        representation.setId(mapperId);
        getProtocolMappersResource(realm, clientScopeName).update(mapperId, representation);
    }

    /**
     * DELETE /client-scopes/{id}/protocol-mappers/models/{id}
     */
    public void deleteProtocolMapper(String realm, String clientScopeName, String mapperName) {
        String mapperId = getProtocolMapperByName(realm, clientScopeName, mapperName).getId();
        getProtocolMappersResource(realm, clientScopeName).delete(mapperId);
    }

    /**
     * resource for path /client-scopes/{id}/protocol-mappers
     */
    private ProtocolMappersResource getProtocolMappersResource(String realm, String name) {
        return getClientScopeResourceByName(realm, name).getProtocolMappers();
    }

    /**
     * adaptation to get resource for path /client-scopes/{id} by client-scope name
     */
    private ClientScopeResource getClientScopeResourceByName(String realm, String name) {
        return getClientScopeResource(realm, getClientScopeByName(realm, name).getId());
    }

    /**
     * resource for path /client-scopes/{id}
     */
    private ClientScopeResource getClientScopeResource(String realm, String id) {
        return getResource(realm).get(id);
    }

    /**
     * resource for path /client-scopes
     */
    private ClientScopesResource getResource(String realm) {
        return realmResourceAdapter.getResource(realm).clientScopes();
    }
}
