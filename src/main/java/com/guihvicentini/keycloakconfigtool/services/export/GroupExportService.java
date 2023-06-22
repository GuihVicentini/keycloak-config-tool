package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.GroupResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.GroupConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
        return retrieveGroupConfigsById(realm);
    }

    private List<GroupConfig> retrieveGroupConfigsById(String realm) {
        return Optional.ofNullable(adapter.getAll(realm))
                .orElse(new ArrayList<>())
                .stream()
                .map(group -> adapter.getGroupById(realm, group.getId()))
                .map(mapper::mapToConfig)
                .toList();
    }
}
