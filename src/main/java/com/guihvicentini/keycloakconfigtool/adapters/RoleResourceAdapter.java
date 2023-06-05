package com.guihvicentini.keycloakconfigtool.adapters;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/roles
 * and
 * @Path: /realms/{realm}/clients/{clientUuid}/roles
 *
 */
@Service
@Slf4j
public class RoleResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;
    private final ClientResourceAdapter clientResourceAdapter;

    public RoleResourceAdapter(RealmResourceAdapter realmResourceAdapter, ClientResourceAdapter clientResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
        this.clientResourceAdapter = clientResourceAdapter;
    }

    /**
     * GET /roles
     */
    public List<RoleRepresentation> getRealmRoles(String realm) {
        List<RoleRepresentation> realmRoles = getResource(realm).list();
        realmRoles.forEach(role -> {
            Optional<RoleRepresentation> mappedRole = searchRealmRole(realm, role.getName());
            mappedRole.ifPresent(roleRepresentation -> role.setComposites(roleRepresentation.getComposites()));
        });

        return realmRoles;
    }
    /**
     * GET /clients/{clientUuid}/roles
     */
    public Map<String, List<RoleRepresentation>> getClientRoles(String realmName) {
        return realmResourceAdapter.getResource(realmName).clients().findAll().stream()
                .collect(Collectors.toMap(
                        ClientRepresentation::getClientId,
                        client -> realmResourceAdapter.getResource(realmName).clients()
                                .get(client.getId()).roles().list()
                ));
    }

    /**
     * GET /roles/{roleName}
     */
    public Optional<RoleRepresentation> searchRealmRole(String realm, String name) {
        Optional<RoleRepresentation> maybeRole;

        RoleResource roleResource = getRoleResource(realm, name);
        var composites = roleResource.getRoleComposites();
        composites.forEach(c -> {
            log.debug("Composite: {}", c.getName());
        });

        try {
            maybeRole = Optional.of(roleResource.toRepresentation());
        } catch (javax.ws.rs.NotFoundException e) {
            maybeRole = Optional.empty();
        }

        return maybeRole;
    }


    /**
     * GET /roles
     */
    public List<RoleRepresentation> getAll(String realm) {
        return getResource(realm).list();
    }


    /**
     * POST /roles
     */
    public void create(String realm, RoleRepresentation representation) {
        getResource(realm).create(representation);
    }

    /**
     * PUT /roles/{roleName}
     */
    public void update(String realm, RoleRepresentation representation) {
        getRoleResource(realm, representation.getName()).update(representation);
    }

    /**
     * DELETE /roles/{roleName}
     */
    public void delete(String realm, String roleName) {
        getRoleResource(realm, roleName).remove();
    }


    /**
     * resource for path /roles/{roleName}
     */
    private RoleResource getRoleResource(String realm, String roleName) {
        return getResource(realm).get(roleName);
    }

    /**
     * resource for path /roles
     */
    private RolesResource getResource(String realm) {
        return realmResourceAdapter.getResource(realm).roles();
    }

    // TODO create 2 classes or move this to client resource adapter
    // ----------------- Clients Role Resource ----------------

    /**
     * POST clients/{clientUuid}/roles
     */
    public void createClientRole(String realm, String clientUuid, RoleRepresentation representation) {
        getClientRolesResource(realm, clientUuid).create(representation);
    }

    /**
     * PUT clients/{clientUuid}/roles/{roleName}
     */
    public void updateClientRole(String realm, String clientUuid, RoleRepresentation representation) {
        getClientRoleResource(realm, clientUuid, representation.getName()).update(representation);
    }

    /**
     * DELETE clients/{clientUuid}/roles/{roleName}
     */
    public void deleteClientRole(String realm, String clientUuid, String roleName) {
        getClientRoleResource(realm, clientUuid, roleName).remove();
    }

    /**
     * resource for path /clients/{clientUuid}/roles/{roleName}
     */
    private RoleResource getClientRoleResource(String realm, String clientUuid, String roleName) {
        return getClientRolesResource(realm, clientUuid).get(roleName);
    }

    /**
     * resource for path /clients/{clientUuid}/roles
     */
    private RolesResource getClientRolesResource(String realm, String clientUuid) {
        return clientResourceAdapter.getClientRolesResource(realm, clientUuid);
    }
}
