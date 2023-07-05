package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.GroupResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.GroupConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import com.guihvicentini.keycloakconfigtool.services.export.ClientExportService;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupImportService {

    private final GroupResourceAdapter resourceAdapter;
    private final GroupConfigMapper configMapper;

    private final ClientExportService clientExportService;

    public GroupImportService(GroupResourceAdapter resourceAdapter,
                              GroupConfigMapper configMapper,
                              ClientExportService clientExportService) {

        this.resourceAdapter = resourceAdapter;
        this.configMapper = configMapper;
        this.clientExportService = clientExportService;
    }


    public void doImport(String realm, List<GroupConfig> actual, List<GroupConfig> target) {

        if(target.equals(actual)) {
            log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        List<GroupConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<GroupConfig> toDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<GroupConfig> toUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addGroups(realm, toBeAdded);
        deleteGroups(realm, toDeleted);
        updateGroups(realm, toUpdated);


        target.forEach(group -> updateRealmRoles(realm, group.getName(), group.getRealmRoles()));
        target.forEach(group -> {
            updateClientRoles(realm, group.getName(), group.getClientRoles());
        });
        target.forEach(group -> importSubGroups(realm, group));
    }

    private void importSubGroups(String realm, GroupConfig group) {
        List<GroupRepresentation> subGroupRepresentation = resourceAdapter.getSubGroups(realm, group.getName());
        List<GroupConfig> actualSubGroups = subGroupRepresentation.stream().map(configMapper::mapToConfig).toList();

        List<GroupConfig> toBeAdded = ListUtil.getMissingConfigElements(group.getSubGroups(), actualSubGroups);
        List<GroupConfig> toBeDeleted = ListUtil.getMissingConfigElements(actualSubGroups, group.getSubGroups());
        List<GroupConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(group.getSubGroups(), actualSubGroups);

        // TODO this only supports one level of subGroups. This has to be extended to support multiple levels of nested subGroups
        // TODO update subGroup realm and client roles
        toBeAdded.forEach(subGroup -> addSubGroup(realm, group.getName(), subGroup));
        toBeDeleted.forEach(subGroup -> deleteSubGroup(realm, getSubGroup(subGroup.getName(), subGroupRepresentation).getId()));
        toBeUpdated.forEach(subGroup -> updateSubGroup(realm, subGroup, getSubGroup(subGroup.getName(), subGroupRepresentation)));
    }

    private void addSubGroup(String realm, String parentGroupName, GroupConfig subGroup) {
        resourceAdapter.createSubGroup(realm, parentGroupName, configMapper.mapToRepresentation(subGroup));
    }

    private void updateSubGroup(String realm, GroupConfig subGroup, GroupRepresentation representation) {
        GroupRepresentation updated = configMapper.mapToRepresentation(subGroup);
        updated.setId(representation.getId());
        resourceAdapter.updateById(realm, updated);
    }

    private void deleteSubGroup(String realm, String subGroupId) {
        resourceAdapter.deleteById(realm, subGroupId);
    }

    private GroupRepresentation getSubGroup(String name, List<GroupRepresentation> subGroupRepresentation) {
        return subGroupRepresentation.stream().filter(subGroup -> name.equals(subGroup.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("SubGroup: %s does not exist.", name)));
    }

    private void updateClientRoles(String realm, String groupName, Map<String, List<String>> clientRoles) {
        clientRoles.keySet().forEach(clientId -> {
            String clientUuid = clientExportService.getClientUuid(realm, clientId);
            List<RoleRepresentation> available = resourceAdapter.getAvailableClientRoles(realm, groupName, clientUuid);
            List<RoleRepresentation> toBeAdded = available.stream()
                    .filter(role -> clientRoles.get(clientId).contains(role.getName())).toList();
            List<RoleRepresentation> toBeRemoved = available.stream()
                    .filter(role -> !clientRoles.get(clientId).contains(role.getName())).toList();

            resourceAdapter.addClientRoles(realm, groupName, clientUuid, toBeAdded);
            resourceAdapter.deleteClientRoles(realm, groupName, clientUuid, toBeRemoved);
        });
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
        resourceAdapter.updateByName(realm, configMapper.mapToRepresentation(group));
    }

    private void deleteGroups(String realm, List<GroupConfig> groups) {
        groups.forEach(group -> deleteGroup(realm, group));
    }

    private void deleteGroup(String realm, GroupConfig group) {
        resourceAdapter.deleteByName(realm, group.getName());
    }

    private void addGroups(String realm, List<GroupConfig> groups) {
        groups.forEach(group -> addGroup(realm, group));
    }

    private void addGroup(String realm, GroupConfig group) {
        resourceAdapter.create(realm, configMapper.mapToRepresentation(group));
    }
}
