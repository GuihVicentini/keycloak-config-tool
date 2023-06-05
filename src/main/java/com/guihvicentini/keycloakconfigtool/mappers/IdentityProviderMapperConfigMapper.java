package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.IdentityProviderMapperConfig;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IdentityProviderMapperConfigMapper {
    IdentityProviderMapperConfig mapToConfig(IdentityProviderMapperRepresentation identityProviderMapperRepresentation);

    IdentityProviderMapperRepresentation mapToRepresentation(IdentityProviderMapperConfig identityProviderMapperConfig);
}
