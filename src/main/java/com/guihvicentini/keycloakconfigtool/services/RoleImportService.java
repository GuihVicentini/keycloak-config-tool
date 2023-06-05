package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.RealmResourceAdapter;
import com.guihvicentini.keycloakconfigtool.adapters.RoleResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RolesConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.RoleConfig;
import com.guihvicentini.keycloakconfigtool.models.RolesConfig;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RoleImportService {

    private final RoleResourceAdapter resourceAdapter;
    private final RolesConfigMapper configMapper;
    private final ClientImportService clientImportService;
    private final RealmResourceAdapter realmResourceAdapter;

    public RoleImportService(RoleResourceAdapter resourceAdapter, RolesConfigMapper configMapper,
                             ClientImportService clientImportService, RealmResourceAdapter realmResourceAdapter) {
        this.resourceAdapter = resourceAdapter;
        this.configMapper = configMapper;
        this.clientImportService = clientImportService;
        this.realmResourceAdapter = realmResourceAdapter;
    }

    public void doImport(String realm, RolesConfig actual, RolesConfig target) {

        if(target.equals(actual)) {
            log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        importRealmRoles(realm, actual.getRealm(), target.getRealm());
        importClientRoles(realm, actual.getClient(), target.getClient());
    }

    // ---------------------- CLIENT ROLES -------------------------------------------

    private void importClientRoles(String realm, Map<String, List<RoleConfig>> actual, Map<String, List<RoleConfig>> target) {
        var toBeAdded = MapUtil.getMissingElements(target, actual);
        var toBeDeleted = MapUtil.getMissingElements(actual, target);

        addClientRoles(realm, toBeAdded);

    }

    private void addClientRoles(String realm, Map<String, List<RoleConfig>> roles) {
        roles.values().forEach(clientRoles -> addClientRoles(realm, clientRoles));
    }

    private void addClientRoles(String realm, List<RoleConfig> roles) {
        replaceContainerId(realm, roles, true);
        List<RoleConfig> compositeRoles = roles.stream().filter(RoleConfig::isComposite).toList();
        List<RoleConfig> plainRoles = roles.stream().filter(role -> !role.isComposite()).toList();

        // create basic roles first
        plainRoles.forEach(role -> addClientRole(realm, role));

        // create composite roles
        compositeRoles.forEach(role -> addClientRole(realm, role));
    }

    private void addClientRole(String realm, RoleConfig role) {
        resourceAdapter.createClientRole(realm, role.getContainerId(), configMapper.mapToRepresentation(role));
    }

    public Map<String, List<RoleRepresentation>> getClientRoles(String realm) {
        return resourceAdapter.getClientRoles(realm);
    }

    // ---------------------- REALM ROLES -------------------------------------------

    private void importRealmRoles(String realm, List<RoleConfig> actual, List<RoleConfig> target) {
        var toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        var toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        var toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addRealmRoles(realm, toBeAdded);
        deleteRealmRoles(realm, toBeDeleted);
        updateRealmRoles(realm, toBeUpdated);
    }

    private void updateRealmRoles(String realm, List<RoleConfig> roles) {
        roles.forEach(role -> updateRole(realm, role));
    }

    private void updateRole(String realm, RoleConfig role) {
        resourceAdapter.update(realm, configMapper.mapToRepresentation(role));
    }

    private void deleteRealmRoles(String realm, List<RoleConfig> roles) {
        roles.forEach(role -> deleteRole(realm, role));
    }

    private void deleteRole(String realm, RoleConfig role) {
        resourceAdapter.delete(realm, role.getName());
    }

    private void addRealmRoles(String realm, List<RoleConfig> roles) {
        replaceContainerId(realm, roles, false);
        List<RoleConfig> compositeRoles = roles.stream().filter(RoleConfig::isComposite).toList();
        roles.removeAll(compositeRoles);

        // create basic roles first
        roles.forEach(role -> addRealmRole(realm, role));

        // create composite roles
        compositeRoles.forEach(role -> addRealmRole(realm, role));
    }

    private void addRealmRole(String realm, RoleConfig role) {
        resourceAdapter.create(realm, configMapper.mapToRepresentation(role));
    }


    private void replaceContainerId(String realm, List<RoleConfig> roles, boolean isClientRole) {
        roles.forEach(role -> replaceContainerId(realm, role.getContainerId(), role, isClientRole));
    }

    private void replaceContainerId(String realm, String containerId, RoleConfig role, boolean isClientRole) {
        String containerUuid = isClientRole ? clientImportService.getClientUuid(realm, containerId) :
                realmResourceAdapter.get(realm).getId();
        role.setContainerId(containerUuid);
    }

    public RolesConfig getRealmAndClientRoles(String realm) {
        RolesRepresentation representation = new RolesRepresentation();
        representation.setRealm(getRealmRoles(realm));
        representation.setClient(getClientRoles(realm));
        RolesConfig config = configMapper.mapToConfig(representation);
        config.normalize(realm);
        return config;
    }

    public List<RoleRepresentation> getRealmRoles(String realm) {
        return resourceAdapter.getRealmRoles(realm);
    }

}
