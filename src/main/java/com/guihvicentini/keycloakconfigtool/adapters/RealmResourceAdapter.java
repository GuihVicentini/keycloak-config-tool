package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.providers.KeycloakProvider;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}
 *
 */

@Service
@Slf4j
public class RealmResourceAdapter {

    private final KeycloakProvider provider;

    RealmResourceAdapter(KeycloakProvider provider){
        this.provider = provider;
    }

    public boolean exists(String realmName) {
        try {
            get(realmName);
            log.debug("realm: {} already exists", realmName);
            return true;
        } catch (javax.ws.rs.NotFoundException e) {
            log.debug("realm: {} doesn't exists", realmName);
            return false;
        }
    }

    /**
     * GET /realms/{realm}
     */
    public RealmRepresentation get(String realmName) {
        return getResource(realmName).toRepresentation();
    }

    /**
     * POST /realms
     */
    public void create(RealmRepresentation realm) {
        Keycloak keycloak = provider.getInstance();
        RealmsResource realmsResource = keycloak.realms();

        try {
            realmsResource.create(realm);
        } catch (WebApplicationException error) {
            String errorMessage = ResponseUtil.getErrorMessage(error);
            throw new KeycloakAdapterException("Cannot create realm: %s: due to error: %s", error, realm.getRealm(), errorMessage);
        }
    }

    /**
     * PUT /realms/{realm}
     */
    public void update(RealmRepresentation realm) {
        try {
            getResource(realm.getRealm()).update(realm);
        } catch (WebApplicationException error) {
            String errorMessage = ResponseUtil.getErrorMessage(error);
            throw new KeycloakAdapterException(
                    String.format("Cannot update realm '%s': %s", realm.getRealm(), errorMessage),
                    error
            );
        }
    }

    /**
     * adaptation to add default client scope by name
     * PUT /realms/{realm}/default-default-client-scopes/{clientScopeId}
     */
    public void addDefaultClientScope(String realm, String scopeName) {
        getResource(realm).addDefaultDefaultClientScope(getDefaultClientScopeId(realm, scopeName));
    }

    /**
     * adaptation to remove default client scope by name
     * DELETE /realms/{realm}/default-default-client-scopes/{clientScopeId}
     */
    public void removeDefaultClientScope(String realm, String scopeName) {
        getResource(realm).removeDefaultDefaultClientScope(getDefaultClientScopeId(realm, scopeName));
    }

    /**
     * adaptation to get client-scope id based on client-scope name using
     * GET /realms/{realm}/client-scopes
     */
    public String getDefaultClientScopeId(String realm, String scopeName) {
        return getResource(realm).clientScopes().findAll()
                .stream().filter(scope -> scopeName.equals(scope.getName()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("Optional ClientScope: %s not found", scopeName))
                .getId();
    }

    /**
     * adaptation to add default client scope by name
     * PUT /realms/{realm}/default-optional-client-scopes/{clientScopeId}
     */
    public void addOptionalClientScope(String realm, String scopeName) {
        getResource(realm).addDefaultOptionalClientScope(getOptionalClientScopeId(realm, scopeName));
    }

    /**
     * adaptation to add default client scope by name
     * PUT /realms/{realm}/default-optional-client-scopes/{clientScopeId}
     */
    public void removeOptionalClientScope(String realm, String scopeName) {
        getResource(realm).removeDefaultOptionalClientScope(getOptionalClientScopeId(realm, scopeName));
    }

    /**
     * adaptation to get client-scope id based on client-scope name using
     * GET /realms/{realm}/client-scopes
     */
    private String getOptionalClientScopeId(String realm, String scopeName) {
        return getResource(realm).clientScopes().findAll()
                .stream().filter(scope -> scopeName.equals(scope.getName()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdapterException("Optional ClientScope: %s not found", scopeName))
                .getId();
    }

    /**
     * POST /realms/{realm}/partial-export
     */
    public RealmRepresentation getPartialExport(String realm) {
        return getResource(realm).partialExport(true, true);
    }

    /**
     * Create an enabled new realm.
     * @param realm the realm name.
     */
    public void init(String realm) {
        RealmRepresentation representation = new RealmRepresentation();
        representation.setRealm(realm);
        representation.setEnabled(true);
        create(representation);
    }

    /**
     * resource for path /realms/{realm}
     */
    public RealmResource getResource(String realmName) {
        return provider.getInstance().realms().realm(realmName);
    }
}
