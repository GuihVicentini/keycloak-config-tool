package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.RequiredActionConfig;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequiredActionsConfigMapper {
    RequiredActionConfig mapToConfig(RequiredActionProviderRepresentation requiredActionProviderRepresentation);
}
