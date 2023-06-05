package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.ProtocolMapperConfig;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProtocolMapperConfigMapper {
    ProtocolMapperConfig mapToConfig(ProtocolMapperRepresentation protocolMapperRepresentation);

    List<ProtocolMapperConfig> mapToConfigList(List<ProtocolMapperRepresentation> protocolMapperRepresentations);

    ProtocolMapperRepresentation mapToRepresentation(ProtocolMapperConfig protocolMapperConfig);
}
