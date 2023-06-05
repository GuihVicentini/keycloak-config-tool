package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.ClientScopeConfig;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientScopeConfigMapper {

    ClientScopeConfig mapToConfig(ClientScopeRepresentation clientScopeRepresentation);

    ClientScopeRepresentation mapToRepresentation(ClientScopeConfig clientScopeConfig);
}
