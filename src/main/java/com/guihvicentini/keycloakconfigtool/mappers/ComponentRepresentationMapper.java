package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.ComponentExportConfig;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComponentRepresentationMapper {

    ComponentRepresentation mapToRepresentation (ComponentExportRepresentation exportRepresentation);

    ComponentRepresentation mapToRepresentation (ComponentExportConfig exportConfig);

}
