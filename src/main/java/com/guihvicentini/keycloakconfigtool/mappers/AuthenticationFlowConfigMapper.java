package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlowConfig;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationFlowConfigMapper {

    AuthenticationFlowConfig mapToConfig(AuthenticationFlowRepresentation authenticationFlowRepresentation);

    AuthenticationFlowRepresentation mapToRepresentation(AuthenticationFlowConfig authenticationFlowConfig);

}
