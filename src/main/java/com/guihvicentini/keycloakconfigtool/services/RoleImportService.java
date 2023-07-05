package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.RealmResourceAdapter;
import com.guihvicentini.keycloakconfigtool.adapters.RoleResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RolesConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.RoleConfig;
import com.guihvicentini.keycloakconfigtool.models.RolesConfig;
import com.guihvicentini.keycloakconfigtool.services.export.ClientExportService;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleImportService {

    private final RoleResourceAdapter resourceAdapter;
    private final RolesConfigMapper configMapper;
    private final ClientExportService clientExportService;
    private final RealmResourceAdapter realmResourceAdapter;

    public RoleImportService(RoleResourceAdapter resourceAdapter,
                             RolesConfigMapper configMapper,
                             ClientExportService clientExportService,
                             RealmResourceAdapter realmResourceAdapter) {

        this.resourceAdapter = resourceAdapter;
        this.configMapper = configMapper;
        this.clientExportService = clientExportService;
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

        target.keySet().forEach(clientId -> {
            String clientUuid = clientExportService.getClientUuid(realm, clientId);
            if (!actual.containsKey(clientId)) {
                addClientRoles(realm, clientUuid, target.get(clientId));
                return;
            }
            List<RoleConfig> actualRoles = actual.get(clientId);
            List<RoleConfig> targetRoles = target.get(clientId);

            List<RoleConfig> toBeAdded = ListUtil.getMissingConfigElements(targetRoles, actualRoles);
            List<RoleConfig> toDeleted = ListUtil.getMissingConfigElements(actualRoles, targetRoles);
            List<RoleConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(targetRoles, actualRoles);

            addClientRoles(realm, clientUuid, toBeAdded);
            deleteClientRoles(realm, clientUuid, toDeleted);
            updateClientRoles(realm, clientUuid, toBeUpdated);

        });
    }

    private void updateClientRoles(String realm, String clientUuid, List<RoleConfig> roles) {
        roles.forEach(role -> updateClientRole(realm, clientUuid, role));
    }

    private void updateClientRole(String realm, String clientUuid, RoleConfig role) {
        resourceAdapter.updateClientRole(realm, clientUuid, configMapper.mapToRepresentation(role));
        updateClientRoleRealmComposite(realm, clientUuid, role.getName(), role.getComposites().getRealm());
        updateClientRoleClientComposite(realm, clientUuid, role.getName(), role.getComposites().getClient());
    }

    private void updateClientRoleClientComposite(String realm, String clientUuid, String roleName, Map<String, List<String>> clientComposites) {
        clientComposites.keySet().forEach(clientId -> {

            String innerClientUuid = clientExportService.getClientUuid(realm, clientId);

            List<RoleRepresentation> actualClientComposites = resourceAdapter
                    .getClientRoleClientComposites(realm, clientUuid, roleName, clientId);

            List<RoleRepresentation> updatedClientComposites = resourceAdapter.getClientRoles(realm, innerClientUuid).stream()
                    .filter(composite -> clientComposites.get(clientId).contains(composite.getName())).collect(Collectors.toList());

            updateClientRoleComposites(realm, clientUuid, roleName, actualClientComposites, updatedClientComposites);

        });
    }

    private void updateClientRoleComposites(String realm, String clientUuid, String roleName,
                                            List<RoleRepresentation> actualClientComposites,
                                            List<RoleRepresentation> updatedClientComposites) {

        List<RoleRepresentation> toBeAdded = updatedClientComposites.stream()
                .filter(newComposite -> actualClientComposites.stream()
                        .noneMatch(actualComposite -> Objects.equals(actualComposite.getId(), newComposite.getId())))
                .collect(Collectors.toList());

        List<RoleRepresentation> toBeDeleted = actualClientComposites.stream()
                .filter(newComposite -> updatedClientComposites.stream()
                        .noneMatch(actualComposite -> Objects.equals(actualComposite.getId(), newComposite.getId())))
                .collect(Collectors.toList());

        resourceAdapter.deleteComposites(realm, clientUuid, roleName, toBeDeleted);
        resourceAdapter.addComposites(realm, clientUuid, roleName, toBeAdded);
    }

    private void updateClientRoleRealmComposite(String realm, String clientUuid, String roleName, Set<String> composites) {
        var actualClientRoleRealmComposite = resourceAdapter.getClientRoleRealmComposites(realm, clientUuid, roleName);
        var updatedClientRoleRealmComposite = resourceAdapter.getClientRoles(realm, clientUuid).stream()
                .filter(composite -> composites.contains(composite.getName())).collect(Collectors.toList());

        updateClientRoleComposites(realm, clientUuid, roleName, actualClientRoleRealmComposite, updatedClientRoleRealmComposite);
    }


    private void deleteClientRoles(String realm, String clientUuid, List<RoleConfig> roles) {
        roles.forEach(role -> deleteClientRole(realm, clientUuid, role));
    }

    private void deleteClientRole(String realm, String clientUuid, RoleConfig role) {
        resourceAdapter.deleteClientRole(realm, clientUuid, role.getName());
    }

    private void addClientRoles(String realm, String clientUuid, List<RoleConfig> roles) {
        replaceContainerId(realm, roles, true);
        List<RoleConfig> compositeRoles = roles.stream().filter(RoleConfig::isComposite).toList();
        List<RoleConfig> nonCompositeRoles = new ArrayList<>(roles);
        nonCompositeRoles.removeAll(compositeRoles);

        // create basic roles first
        nonCompositeRoles.forEach(role -> addClientRole(realm, clientUuid, role));

        // create composite roles
        compositeRoles.forEach(role -> addClientRole(realm, clientUuid, role));
    }

    private void addClientRole(String realm, String clientUuid, RoleConfig role) {
        log.debug("Creating Client Role: {}/{}", clientUuid, role.getName());
        resourceAdapter.createClientRole(realm, clientUuid, configMapper.mapToRepresentation(role));
    }

    // ---------------------- REALM ROLES -------------------------------------------

    private void importRealmRoles(String realm, List<RoleConfig> actual, List<RoleConfig> target) {

        List<RoleConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<RoleConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<RoleConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addRealmRoles(realm, toBeAdded);
        deleteRealmRoles(realm, toBeDeleted);
        updateRealmRoles(realm, toBeUpdated);
    }

    private void updateRealmRoles(String realm, List<RoleConfig> roles) {
        roles.forEach(role -> updateRole(realm, role));
    }

    private void updateRole(String realm, RoleConfig role) {
        resourceAdapter.update(realm, configMapper.mapToRepresentation(role));
        updateRoleRealmComposites(realm, role.getName(), role.getComposites().getRealm());
        updateRoleClientComposites(realm, role.getName(), role.getComposites().getClient());
    }

    private void updateRoleClientComposites(String realm, String roleName, Map<String, List<String>> clientComposites) {
        clientComposites.keySet().forEach(clientId -> {
            String clientUuid = clientExportService.getClientUuid(realm, clientId);
            List<RoleRepresentation> actualClientComposites = resourceAdapter.getRoleClientComposites(realm, roleName, clientUuid);
            List<RoleRepresentation> updatedClientComposites = resourceAdapter.getClientRoles(realm, clientUuid).stream()
                    .filter(composite -> clientComposites.get(clientId).contains(composite.getName())).collect(Collectors.toList());

            updateRoleComposite(realm, roleName, actualClientComposites, updatedClientComposites);

        });
    }

    private void updateRoleRealmComposites(String realm, String roleName, Set<String> realmComposites) {
        List<RoleRepresentation> actualRealmComposites = resourceAdapter.getRoleRealmCompositesRepresentation(realm, roleName);
        List<RoleRepresentation> updatedComposites = resourceAdapter.getRealmRoles(realm).stream()
                .filter(composite -> realmComposites.contains(composite.getName())).toList();

        updateRoleComposite(realm, roleName, actualRealmComposites, updatedComposites);
    }


    private void updateRoleComposite(String realm, String roleName,
                                     List<RoleRepresentation> actualClientComposites,
                                     List<RoleRepresentation> updatedClientComposites) {

        List<RoleRepresentation> toBeAdded = updatedClientComposites.stream()
                .filter(newComposite -> actualClientComposites.stream()
                        .noneMatch(actualComposite -> Objects.equals(actualComposite.getId(), newComposite.getId())))
                .collect(Collectors.toList());

        List<RoleRepresentation> toBeDeleted = actualClientComposites.stream()
                .filter(newComposite -> updatedClientComposites.stream()
                        .noneMatch(actualComposite -> Objects.equals(actualComposite.getId(), newComposite.getId())))
                .collect(Collectors.toList());

        resourceAdapter.deleteComposites(realm, roleName, toBeDeleted);
        resourceAdapter.addComposites(realm, roleName, toBeAdded);
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
        List<RoleConfig> nonCompositeRoles = new ArrayList<>(roles);
        nonCompositeRoles.removeAll(compositeRoles);

        // create basic roles first
        nonCompositeRoles.forEach(role -> addRealmRole(realm, role));

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
        String containerUuid = isClientRole ? clientExportService.getClientUuid(realm, containerId) :
                realmResourceAdapter.get(realm).getId();
        role.setContainerId(containerUuid);
    }

}
