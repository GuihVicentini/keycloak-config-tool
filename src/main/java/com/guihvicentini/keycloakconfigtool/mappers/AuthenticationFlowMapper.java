package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlow;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationFlowMapper {

    AuthenticationFlow mapToConfig(AuthenticationFlowRepresentation representation);

    AuthenticationFlowRepresentation mapToRepresentation(AuthenticationFlow authenticationFlow);

}
