package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RequiredActionProviderConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.RequiredActionProviderConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RequiredActionExportService {
    private final RequiredActionProviderConfigMapper mapper;
    private final AuthenticationManagementResourceAdapter adapter;

    public RequiredActionExportService(RequiredActionProviderConfigMapper mapper,
                                       AuthenticationManagementResourceAdapter adapter) {
        this.mapper = mapper;
        this.adapter = adapter;
    }

    public List<RequiredActionProviderConfig> getAll(String realm){
        return Optional.ofNullable(adapter.getRequiredActions(realm))
                .orElse(new ArrayList<>())
                .stream()
                .map(mapper::mapToConfig)
                .collect(Collectors.toList());
    }
}
