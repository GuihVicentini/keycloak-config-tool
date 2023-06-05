package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.RequiredActionProviderConfig;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequiredActionProviderConfigMapper {

    RequiredActionProviderConfig mapToConfig(RequiredActionProviderRepresentation requiredActionProviderRepresentation);
    RequiredActionProviderRepresentation mapToRepresentation(RequiredActionProviderConfig requiredActionProviderConfig);
}
