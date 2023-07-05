package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlow;
import com.guihvicentini.keycloakconfigtool.models.AuthenticatorConfigConfig;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationFlowMapper {

    AuthenticationFlow mapToConfig(AuthenticationFlowRepresentation representation);

    AuthenticationFlowRepresentation mapToRepresentation(AuthenticationFlow authenticationFlow);

    AuthenticatorConfigRepresentation mapToRepresentation(AuthenticatorConfigConfig authenticatorConfigConfig);

}
