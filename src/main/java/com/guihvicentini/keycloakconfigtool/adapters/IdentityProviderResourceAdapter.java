package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.IdentityProvidersResource;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/identity-provider
 *
 */

@Service
@Slf4j
public class IdentityProviderResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;

    public IdentityProviderResourceAdapter(RealmResourceAdapter realmResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
    }

    /**
     * GET /identity-provider/instances/{alias}
     */
    public IdentityProviderRepresentation getByAlias(String real, String alias) {
        return getResource(real).get(alias).toRepresentation();
    }

    /**
     * GET /identity-provider/instances/
     */
    public List<IdentityProviderRepresentation> getAll(String realm) {
        return getResource(realm).findAll();
    }


    /**
     * POST /identity-provider/instances
     */
    public String create(String realm, IdentityProviderRepresentation representation) {
        try(Response response = getResource(realm).create(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create identity-provider: %s\n error message: %s",
                    e, representation.getAlias(), errorMessage);
        }
    }

    /**
     * PUT /identity-provider/instances/{alias}
     */
    public void update(String realm, IdentityProviderRepresentation representation) {
        getResource(realm).get(representation.getAlias()).update(representation);
    }

    /**
     * DELETE /identity-provider/instances/{alias}
     */
    public void delete(String realm, String alias) {
        getResource(realm).get(alias).remove();
    }

    /**
     * GET /identity-provider/instances/{alias}/mappers
     */
    public List<IdentityProviderMapperRepresentation> getMappers(String realm, String alias) {
        return getResource(realm).get(alias).getMappers();
    }

    /**
     * GET /identity-provider/instances/{alias}/mappers/{id}
     */
    public IdentityProviderMapperRepresentation getMapperById(String realm, String alias, String id) {
        return getResource(realm).get(alias).getMapperById(id);
    }

    /**
     * adaptation to get optional mapper using  GET /identity-provider/instances/{alias}/mappers
     */
    public Optional<IdentityProviderMapperRepresentation> getMapperByName(String realm, String alias, String name) {
        return getResource(realm).get(alias).getMappers().stream()
                .filter(mapper -> name.equals(mapper.getName())).findFirst();
    }

    /**
     * POST /identity-provider/instances/{alias}/mappers
     */
    public String addMapper(String realm, String alias , IdentityProviderMapperRepresentation mapperRepresentation) {
        try(Response response = getResource(realm).get(alias).addMapper(mapperRepresentation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create mapper: %s for identity provider: %s\n error message: %s",
                    e, mapperRepresentation.getName(), alias, errorMessage);
        }
    }

    /**
     * PUT /identity-provider/instances/{alias}/mappers/{id}
     */
    public void updateMapper(String realm, String alias, IdentityProviderMapperRepresentation mapperRepresentation) {
        getResource(realm).get(alias).update(mapperRepresentation.getId(),mapperRepresentation);
    }

    /**
     * DELETE /identity-provider/instances/{alias}/mappers/{id}
     */
    public void deleteMapper(String realm, String alias, String mapperId) {
        getResource(realm).get(alias).delete(mapperId);
    }

    /**
     * resource for path /identity-provider
     */
    private IdentityProvidersResource getResource(String realm) {
        return realmResourceAdapter.getResource(realm).identityProviders();
    }
}
