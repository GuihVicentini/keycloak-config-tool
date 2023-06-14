package com.guihvicentini.keycloakconfigtool.services.export;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RequiredActionsConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.RequiredActionConfig;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RequiredActionExportService {
    RequiredActionsConfigMapper mapper;
    AuthenticationManagementResourceAdapter adapter;

    public List<RequiredActionConfig> getAll(String realm){
        var resources = adapter.getRequiredActions(realm);
        if (resources == null){
            return new ArrayList<>();
        }
        return resources.stream().map(r -> mapper.mapToConfig(r)).collect(Collectors.toList());
    }
}
