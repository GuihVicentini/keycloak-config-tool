package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.IdentityProviderConfig;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IdentityProviderConfigMapper {

    IdentityProviderConfig mapToConfig(IdentityProviderRepresentation identityProviderRepresentation);

    IdentityProviderRepresentation mapToRepresentation(IdentityProviderConfig identityProviderConfig);

}
