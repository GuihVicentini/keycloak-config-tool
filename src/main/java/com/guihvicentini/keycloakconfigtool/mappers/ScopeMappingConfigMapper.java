package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.ScopeMappingConfig;
import org.keycloak.representations.idm.ScopeMappingRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ScopeMappingConfigMapper {

    ScopeMappingConfig mapToConfig(ScopeMappingRepresentation scopeMappingRepresentation);

}
