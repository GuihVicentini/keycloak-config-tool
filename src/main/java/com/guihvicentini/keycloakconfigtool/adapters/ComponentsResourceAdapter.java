package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.ComponentResource;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;


/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/components
 *
 */
@Service
@Slf4j
public class ComponentsResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;

    public ComponentsResourceAdapter(RealmResourceAdapter realmResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
    }


    /**
     * GET /realms/{realm}/components
     */
    public List<ComponentRepresentation> getAll(String realm) {
        return getResource(realm).query();
    }

    /**
     * GET /realms/{realm}/components/{id}
     */
    public ComponentRepresentation getById(String realm, String id) {
        try {
            return getResource(realm).component(id).toRepresentation();
        } catch (WebApplicationException e) {
            throw new KeycloakAdapterException("Cannot find component by id '%s' in realm '%s' ", e, id, realm);
        }
    }

    /**
     * adaptation to get component by name using GET /realms/{realm}/components/{id}
     */
    public Optional<ComponentRepresentation> getByName(String realm, String name) {
        try {
            return getAll(realm).stream()
                    .filter(component -> name.equals(component.getName()))
                    .findFirst();
        } catch (WebApplicationException e) {
            throw new KeycloakAdapterException("Cannot find component by name. Realm %s probably doesn't exist", e, realm);
        }
    }

    /**
     * POST /realms/{realm}/components
     */
    public String create(String realm, ComponentRepresentation representation) {
        try(Response response = getResource(realm).add(representation)){
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create component: %s\n error message: %s",
                    e, representation.getName(), errorMessage);
        }
    }

    /**
     * PUT /realms/{realm}/components/{id}
     */
    public void update(String realm, ComponentRepresentation representation) {
        getComponentResource(realm, representation.getId()).update(representation);
    }

    /**
     * DELETE /realms/{realm}/components/{id}
     */
    public void delete(String realm, ComponentRepresentation representation) {
        if (representation.getProviderId().equals("ldap")) {
            throw new KeycloakAdapterException("It is not allowed to delete the LDAP provider." +
                    "Do it manually and re-import the realm config" +
                    "Current internal user IDs will be lost!" +
                    "component name: %s", representation.getName());
        }
        getComponentResource(realm, representation.getId()).remove();
    }

    /**
     * resource for path /components/{id}
     */
    private ComponentResource getComponentResource(String realm, String componentId) {
        return getResource(realm).component(componentId);
    }

    /**
     * resource for path /components/
     */
    private ComponentsResource getResource(String realm) {
        return realmResourceAdapter.getResource(realm).components();
    }

    public String getRealmId(String realm) {
        return realmResourceAdapter.get(realm).getId();
    }

}
