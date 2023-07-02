package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.IdentityProviderResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.IdentityProviderConfigMapper;
import com.guihvicentini.keycloakconfigtool.mappers.IdentityProviderMapperConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.IdentityProviderConfig;
import com.guihvicentini.keycloakconfigtool.models.IdentityProviderMapperConfig;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IdentityProviderImportService {

    private final IdentityProviderResourceAdapter resourceAdapter;
    private final IdentityProviderConfigMapper identityProviderConfigMapper;
    private final IdentityProviderMapperConfigMapper identityProviderMapperConfigMapper;


    public IdentityProviderImportService(IdentityProviderResourceAdapter resourceAdapter,
                                         IdentityProviderConfigMapper identityProviderConfigMapper,
                                         IdentityProviderMapperConfigMapper identityProviderMapperConfigMapper) {
        this.resourceAdapter = resourceAdapter;
        this.identityProviderConfigMapper = identityProviderConfigMapper;
        this.identityProviderMapperConfigMapper = identityProviderMapperConfigMapper;
    }

    public void doImport(String realm,
                         List<IdentityProviderConfig> actual,
                         List<IdentityProviderConfig> target) {

        if(target.equals(actual)) {
           log.debug(ConfigConstants.UP_TO_DATE_MESSAGE);
           return;
        }
        List<IdentityProviderMapperConfig> actualMappers = actual.stream()
                .flatMap(idp -> idp.getMappers().stream()).collect(Collectors.toList());
        List<IdentityProviderMapperConfig> targetMappers = target.stream()
                .flatMap(idp -> idp.getMappers().stream()).collect(Collectors.toList());

        importIdentityProviders(realm, actual, target, actualMappers, targetMappers);
    }

    public void importIdentityProviders(String realm,
                                        List<IdentityProviderConfig> actual,
                                        List<IdentityProviderConfig> target,
                                        List<IdentityProviderMapperConfig> actualMappers,
                                        List<IdentityProviderMapperConfig> targetMappers) {


        List<IdentityProviderConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<IdentityProviderConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<IdentityProviderConfig> toBeUpdate = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        toBeAdded.forEach(idp -> {
            add(realm, idp);
            importMappers(realm, idp.getAlias(), actualMappers, targetMappers);
        });

        toBeDeleted.forEach(idp -> {
            // delete mappers before deleting idp
            importMappers(realm, idp.getAlias(), actualMappers, targetMappers);
            delete(realm, idp);
        });

        toBeUpdate.forEach(idp -> {
            update(realm, idp);
            importMappers(realm, idp.getAlias(), actualMappers, targetMappers);
        });

    }

    private void delete(String realm, IdentityProviderConfig idp) {
        log.debug("Deleting identity provider: {}", JsonMapperUtils.objectToJsonString(idp));
        resourceAdapter.delete(realm, idp.getAlias());
    }

    private void update(String realm, IdentityProviderConfig idp) {
        log.debug("Updating identity provider: {}", JsonMapperUtils.objectToJsonString(idp));
        resourceAdapter.update(realm, identityProviderConfigMapper.mapToRepresentation(idp));
    }

    private void add(String realm, IdentityProviderConfig idp) {
        log.debug("Creating new identity provider: {}", JsonMapperUtils.objectToJsonString(idp));
        resourceAdapter.create(realm, identityProviderConfigMapper.mapToRepresentation(idp));
    }

    private void importMappers(String realm, String alias,
                               List<IdentityProviderMapperConfig> actualMappers,
                               List<IdentityProviderMapperConfig> targetMappers) {

        List<IdentityProviderMapperConfig> actualIdpMappers = getIdpMappers(alias, actualMappers);
        List<IdentityProviderMapperConfig> targetIdpMappers = getIdpMappers(alias, targetMappers);

        List<IdentityProviderMapperConfig> toBeAdded = ListUtil.getMissingConfigElements(targetIdpMappers, actualIdpMappers);
        List<IdentityProviderMapperConfig> toBeDeleted = ListUtil.getMissingConfigElements(actualIdpMappers, targetIdpMappers);
        List<IdentityProviderMapperConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(targetIdpMappers, actualIdpMappers);

        addMappers(realm, alias, toBeAdded);
        deleteMappers(realm, alias, toBeDeleted);
        updateMappers(realm, alias, toBeUpdated);
    }


    private void updateMappers(String realm, String alias, List<IdentityProviderMapperConfig> idpMappers) {
        idpMappers.forEach(idpMapper -> updateMapper(realm, alias, idpMapper));
    }

    private void updateMapper(String realm, String alias, IdentityProviderMapperConfig idpMapper) {
        Optional<IdentityProviderMapperRepresentation> existingMapper = resourceAdapter.getMapperByName(realm, alias, idpMapper.getName());
        existingMapper.ifPresent(mapper -> {
            IdentityProviderMapperRepresentation toBeUpdated = identityProviderMapperConfigMapper.mapToRepresentation(idpMapper);
            toBeUpdated.setId(mapper.getId());
            resourceAdapter.updateMapper(realm, alias, toBeUpdated);
        });
    }

    private void deleteMappers(String realm, String idpAlias, List<IdentityProviderMapperConfig> idpMappers) {
        idpMappers.forEach(mapper -> deleteMapper(realm, idpAlias, mapper));
    }
    private void deleteMapper(String realm, String alias, IdentityProviderMapperConfig idpMapper) {
        var toBeDeleted = resourceAdapter
                .getMapperByName(realm, alias, idpMapper.getName());
        toBeDeleted.ifPresent(mapperRepresentation -> resourceAdapter
                .deleteMapper(realm, alias, mapperRepresentation.getId()));
    }

    private void addMappers(String realm, String alias, List<IdentityProviderMapperConfig> idpMappers) {
        idpMappers.forEach(idpMapper -> addMapper(realm, alias, idpMapper));
    }

    private void addMapper(String realm, String alias, IdentityProviderMapperConfig idpMapper) {
        resourceAdapter.addMapper(realm, alias, identityProviderMapperConfigMapper.mapToRepresentation(idpMapper));
    }

    private List<IdentityProviderMapperConfig> getIdpMappers(String idpAlias, List<IdentityProviderMapperConfig> mappers) {
        return mappers.stream().filter(mapper -> idpAlias.equals(mapper.getIdentityProviderAlias())).toList();
    }

}
