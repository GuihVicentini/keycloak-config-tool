package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.GroupResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.GroupConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupImportService {

    private final GroupResourceAdapter resourceAdapter;
    private final GroupConfigMapper configMapper;

    public GroupImportService(GroupResourceAdapter resourceAdapter, GroupConfigMapper configMapper) {
        this.resourceAdapter = resourceAdapter;
        this.configMapper = configMapper;
    }


    public void doImport(String realm, List<GroupConfig> actual, List<GroupConfig> target) {

        if(target.equals(actual)) {
            log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        List<GroupConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<GroupConfig> toDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<GroupConfig> toUpdated = ListUtil.getMissingConfigElements(target, actual);

        addGroups(realm, toBeAdded);
        deleteGroups(realm, toDeleted);
        updateGroups(realm, toUpdated);

        target.forEach(group -> updateRealmRoles(realm, group.getName(), group.getRealmRoles()));

    }

    private void updateRealmRoles(String realm, String groupName, List<String> realmRoles) {
        List<RoleRepresentation> available = resourceAdapter.getAvailableRealmRoles(realm, groupName);
        List<RoleRepresentation> toBeAdded = available.stream().filter(role -> realmRoles.contains(role.getName())).toList();
        List<RoleRepresentation> toBeRemoved = resourceAdapter.getRealmRoles(realm).stream()
                .filter(role -> !realmRoles.contains(role.getName())).collect(Collectors.toList());

        resourceAdapter.addRealmRoles(realm, groupName, toBeAdded);
        resourceAdapter.deleteRealmRoles(realm, groupName, toBeRemoved);
    }

    private void updateGroups(String realm, List<GroupConfig> groups) {
        groups.forEach(group -> updateGroup(realm, group));
    }

    private void updateGroup(String realm, GroupConfig group) {
        resourceAdapter.update(realm, configMapper.mapToRepresentation(group));
    }

    private void deleteGroups(String realm, List<GroupConfig> groups) {
        groups.forEach(group -> deleteGroup(realm, group));
    }

    private void deleteGroup(String realm, GroupConfig group) {
        resourceAdapter.delete(realm, group.getName());
    }

    private void addGroups(String realm, List<GroupConfig> groups) {
        groups.forEach(group -> addGroup(realm, group));
    }

    private void addGroup(String realm, GroupConfig group) {
        resourceAdapter.create(realm, configMapper.mapToRepresentation(group));
    }

    // TODO migrate it to an GroupExportService
    public List<GroupConfig> getAllGroups(String realm) {
        var groups = resourceAdapter.getAll(realm).stream().map(configMapper::mapToConfig).toList();
        groups.forEach(group -> {
            var roleMappings = resourceAdapter.getGroupRoleMappings(realm, group.getName());
            var realmRoles = roleMappings.getRealmMappings().stream().map(RoleRepresentation::getName).toList();
//            var clientRoles = roleMappings.getClientMappings().entrySet().stream().map(role -)
            group.setRealmRoles(realmRoles);
        });
        return groups;
    }
}
