package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.GroupResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.GroupConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupExportService {

    private final GroupConfigMapper mapper;
    private final GroupResourceAdapter adapter;

    public GroupExportService(GroupConfigMapper mapper, GroupResourceAdapter adapter) {
        this.mapper = mapper;
        this.adapter = adapter;
    }

    public List<GroupConfig> getGroupConfigs(String realm) {
        var groups = retrieveGroupConfigs(realm);
        groups.forEach(group -> {
            var roleMappings = adapter.getGroupRoleMappings(realm, group.getName());
            group.setClientRoles(retrieveClientRoles(roleMappings));
            group.setRealmRoles(retrieveRealmRoles(roleMappings));
        });

        return groups;
    }

    private List<GroupConfig> retrieveGroupConfigs(String realm) {
        return Optional.ofNullable(adapter.getAll(realm))
                .orElse(new ArrayList<>())
                .stream()
                .map(mapper::mapToConfig)
                .toList();
    }

    private Map<String, List<String>> retrieveClientRoles(MappingsRepresentation mappingsRepresentation) {
        return Optional.ofNullable(mappingsRepresentation)
                .map(MappingsRepresentation::getClientMappings)
                .orElse(new HashMap<>()).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getMappings().stream().map(RoleRepresentation::getName).toList()));
    }

    private List<String> retrieveRealmRoles(MappingsRepresentation mappingsRepresentation) {
        return Optional.ofNullable(mappingsRepresentation)
                .map(MappingsRepresentation::getRealmMappings)
                .orElse(new ArrayList<>())
                .stream()
                .map(RoleRepresentation::getName)
                .toList();
    }
}
