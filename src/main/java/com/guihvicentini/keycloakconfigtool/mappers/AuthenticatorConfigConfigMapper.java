package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.AuthenticatorConfigConfig;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticatorConfigConfigMapper {

    AuthenticatorConfigConfig mapToConfig(AuthenticatorConfigRepresentation authenticatorConfigRepresentation);

}
