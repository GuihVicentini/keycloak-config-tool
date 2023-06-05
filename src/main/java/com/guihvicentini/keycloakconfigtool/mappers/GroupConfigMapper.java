package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GroupConfigMapper {
    GroupConfig mapToConfig(GroupRepresentation groupRepresentation);

    GroupRepresentation mapToRepresentation(GroupConfig groupConfig);
}
