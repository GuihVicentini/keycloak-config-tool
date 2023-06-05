package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.ComponentExportConfig;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComponentExportConfigMapper {

    ComponentExportConfig mapToConfig(ComponentExportRepresentation componentExportRepresentation);

    List<ComponentExportConfig> mapToConfigList(List<ComponentExportRepresentation> value);

    List<ComponentExportRepresentation> mapToRepresentationList(List<ComponentExportConfig> value);
}
