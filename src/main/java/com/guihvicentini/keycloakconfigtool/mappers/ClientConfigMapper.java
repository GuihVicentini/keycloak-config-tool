package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.ClientConfig;
import org.keycloak.representations.idm.ClientRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientConfigMapper {
    ClientConfig mapToConfig(ClientRepresentation clientRepresentation);

    ClientRepresentation mapToRepresentation(ClientConfig clientConfig);
}
