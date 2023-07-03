package com.guihvicentini.keycloakconfigtool.adapters;

import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Adapter to translate requests related to
 * @Path: /realms/{realm}/groups
 *
 */

@Service
@Slf4j
public class GroupResourceAdapter {

    private final RealmResourceAdapter realmResourceAdapter;

    public GroupResourceAdapter(RealmResourceAdapter realmResourceAdapter) {
        this.realmResourceAdapter = realmResourceAdapter;
    }

    /**
     * GET /groups
     */
    public List<GroupRepresentation> getAll(String realm) {
        return getResource(realm).groups();
    }

    /**
     * adaptation to get group by name using GET /groups
     */
    public GroupRepresentation getGroupByName(String realm, String groupName) {
        return getAll(realm).stream().filter(group -> groupName.equals(group.getName()))
                .findFirst()
                .orElseThrow(()-> new KeycloakAdapterException("Group: %s not found", groupName));
    }

    /**
     * Adaptation to get all subgroups of a group given the groupName
     * GET /groups/{id}
     */
    public List<GroupRepresentation> getSubGroups(String realm, String groupName) {
        return getGroupById(realm, getGroupByName(realm, groupName).getId()).getSubGroups();
    }

    /**
     * GET /groups/{id}
     */
    public GroupRepresentation getGroupById(String realm, String groupId) {
        return getGroupResource(realm, groupId).toRepresentation();
    }

    /**
     * POST /groups
     */
    public String create(String realm, GroupRepresentation representation) {
        try (Response response = getResource(realm).add(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create group: %s\n error message: %s",
                    e, representation.getName(), errorMessage);
        }
    }

    /**
     * POST /groups/{id}/children
     */
    public String createSubGroup(String realm,String groupName ,GroupRepresentation representation) {
        try (Response response = getGroupResourceByName(realm, groupName).subGroup(representation)) {
            return CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException e) {
            String errorMessage = ResponseUtil.getErrorMessage(e);
            throw new KeycloakAdapterException("Failed to create subGroup: %s\n error message: %s",
                    e, representation.getName(), errorMessage);
        }
    }

    /**
     * PUT /groups/{id}
     */
    public void updateByName(String realm, GroupRepresentation representation) {
        getGroupResourceByName(realm, representation.getName()).update(representation);
    }

    /**
     * PUT /groups/{id}
     */
    public void updateById(String realm, GroupRepresentation representation) {
        getGroupResource(realm, representation.getId()).update(representation);
    }

    /**
     * DELETE /groups/{id}
     */
    public void deleteByName(String realm, String groupName) {
        getGroupResourceByName(realm, groupName).remove();
    }

    /**
     * DELETE /groups/{id}
     */
    public void deleteById(String realm, String groupUuid) {
        getGroupResource(realm, groupUuid).remove();
    }

    /**
     * GET /groups/{id}/role-mappings/
     */
    public MappingsRepresentation getGroupRoleMappings(String realm, String groupName) {
        return getGroupResourceByName(realm, groupName).roles().getAll();
    }

    /**
     * GET /groups/{id}/role-mappings/realm/available
     */
    public List<RoleRepresentation> getAvailableRealmRoles(String realm, String groupName) {
        return getGroupResourceByName(realm, groupName).roles().realmLevel().listAvailable();
    }

    /**
     * GET /groups/{id}/role-mappings/realm/available
     */
    public List<RoleRepresentation> getRealmRoles(String realm) {
        return realmResourceAdapter.getResource(realm).roles().list();
    }


    /**
     * POST /groups/{id}/role-mappings/realm
     */
    public void addRealmRoles(String realm, String groupName, List<RoleRepresentation> roles) {
        getGroupResourceByName(realm, groupName).roles().realmLevel().add(roles);
    }

    /**
     * DELETE /groups/{id}/role-mappings/realm/available
     */
    public void deleteRealmRoles(String realm, String groupName, List<RoleRepresentation> roles) {
        getGroupResourceByName(realm, groupName).roles().realmLevel().remove(roles);
    }

    /**
     * GET /groups/{id}/role-mappings/clients/{clientUuid}/available
     */
    public List<RoleRepresentation> getAvailableClientRoles(String realm, String groupName, String clientUuid) {
        return getGroupResourceByName(realm, groupName).roles().clientLevel(clientUuid).listAvailable();
    }

    /**
     * POST /groups/{id}/role-mappings/clients/{clientUuid}/
     */
    public void addClientRoles(String realm, String groupName, String clientUuid, List<RoleRepresentation> roles) {
        getGroupResourceByName(realm, groupName).roles().clientLevel(clientUuid).add(roles);
    }

    /**
     * DELETE /groups/{id}/role-mappings/clients/{clientUuid}/
     */
    public void deleteClientRoles(String realm, String groupName, String clientUuid, List<RoleRepresentation> roles) {
        getGroupResourceByName(realm, groupName).roles().clientLevel(clientUuid).remove(roles);
    }

    /**
     * adaptation to get resource by group name
     */
    private GroupResource getGroupResourceByName(String realm, String groupName) {
        return getGroupResource(realm, getGroupByName(realm, groupName).getId());
    }

    /**
     * resource for path /groups/{id}
     */
    private GroupResource getGroupResource(String realm, String groupId) {
        return getResource(realm).group(groupId);
    }


    /**
     * resource for path /groups
     */
    private GroupsResource getResource(String realm) {
         return realmResourceAdapter.getResource(realm).groups();
    }

}
