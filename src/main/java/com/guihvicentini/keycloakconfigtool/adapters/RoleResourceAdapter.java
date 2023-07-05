package com.guihvicentini.keycloakconfigtool.adapters;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
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
        return getResource(realm).list();
    }

    /**
     * GET /roles/{role-name}/composites/realm
     */
    public Set<String> getRoleRealmComposites(String realm, String roleName) {
        return getRoleResource(realm, roleName).getRealmRoleComposites()
                .stream().map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    /**
     * GET /roles/{role-name}/composites/realm
     */
    public List<RoleRepresentation> getRoleRealmCompositesRepresentation(String realm, String roleName) {
        return new ArrayList<>(getRoleResource(realm, roleName).getRealmRoleComposites());
    }

    /**
     * GET /roles/{role-name}/composites/client/{clientUuid}
     */
    public List<RoleRepresentation> getRoleClientComposites(String realm, String roleName, String clientUuid) {
        return new ArrayList<>(getRoleResource(realm, roleName).getClientRoleComposites(clientUuid));
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
     * POST /roles/{roleName}/composites
     */
    public void addComposites(String realm, String roleName, List<RoleRepresentation> composites) {
        getRoleResource(realm, roleName).addComposites(composites);
    }

    /**
     * DELETE /roles/{roleName}/composites
     */
    public void deleteComposites(String realm, String roleName, List<RoleRepresentation> composites) {
        getRoleResource(realm, roleName).deleteComposites(composites);
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
     * GET /clients/{clientUuid}/roles
     */
    public Map<String, List<RoleRepresentation>> getAllClientRoles(String realm) {
        return clientResourceAdapter.getClients(realm).stream()
                .collect(Collectors.toMap(ClientRepresentation::getClientId, client ->
                        realmResourceAdapter.getResource(realm).clients().get(client.getId()).roles().list()
                ));
    }

    public List<RoleRepresentation> getClientRoles(String realm, String clientUuid) {
        return getClientRolesResource(realm, clientUuid).list();

    }

    /**
     * GET clients/{clientUuid}/roles/{role-name}/composites/realm
     */
    public List<RoleRepresentation> getClientRoleRealmComposites(String realm, String clientUuid , String roleName) {
        return new ArrayList<>(getClientRoleResource(realm, clientUuid, roleName).getRealmRoleComposites());
    }

    /**
     * GET clients/{clientUuid}/roles/{role-name}/composites/client/{clientId}
     */
    public List<RoleRepresentation> getClientRoleClientComposites(String realm, String clientUuid, String roleName, String clientId) {
        String clientIdUuid = clientResourceAdapter.getClientByClientId(realm, clientId).getId();
        return new ArrayList<>(getClientRoleResource(realm, clientUuid, roleName)
                .getClientRoleComposites(clientIdUuid));
    }

    /**
     * POST clients/{clientUuid}/roles
     */
    public void createClientRole(String realm, String clientUuid, RoleRepresentation representation) {
        getClientRolesResource(realm, clientUuid).create(representation);
    }


    /**
     * POST clients/{clientUuid}/roles/{roleName}/composites
     */
    public void addComposites(String realm, String clientUuid, String roleName, List<RoleRepresentation> representations) {
        getClientRoleResource(realm, clientUuid, roleName).addComposites(representations);
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
     * DELETE clients/{clientUuid}/roles/{roleName}/composites
     */
    public void deleteComposites(String realm, String clientUuid, String roleName, List<RoleRepresentation> representations) {
        getClientRoleResource(realm, clientUuid, roleName).deleteComposites(representations);
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
