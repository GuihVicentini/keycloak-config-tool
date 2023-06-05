package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.IdentityProviderResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.IdentityProviderConfigMapper;
import com.guihvicentini.keycloakconfigtool.mappers.IdentityProviderMapperConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.IdentityProviderConfig;
import com.guihvicentini.keycloakconfigtool.models.IdentityProviderMapperConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IdentityProviderExportService {

    private final IdentityProviderResourceAdapter resourceAdapter;

    private final IdentityProviderConfigMapper configMapper;
    private final IdentityProviderMapperConfigMapper mapperConfigMapper;

    public IdentityProviderExportService(IdentityProviderResourceAdapter resourceAdapter,
                                         IdentityProviderConfigMapper configMapper,
                                         IdentityProviderMapperConfigMapper mapperConfigMapper) {
        this.resourceAdapter = resourceAdapter;
        this.configMapper = configMapper;
        this.mapperConfigMapper = mapperConfigMapper;
    }

    public List<IdentityProviderConfig> getIdps(String realm) {
        return resourceAdapter.getAll(realm).stream().map(configMapper::mapToConfig)
                .peek(idp -> idp.setMappers(getIdpMappers(realm, idp.getAlias())))
                .collect(Collectors.toList());
    }

    private List<IdentityProviderMapperConfig> getIdpMappers(String realm, String idpAlias) {
        return resourceAdapter.getMappers(realm, idpAlias).stream().map(mapperConfigMapper::mapToConfig).toList();
    }

}
