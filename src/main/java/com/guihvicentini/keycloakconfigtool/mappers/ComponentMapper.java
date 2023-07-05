package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.Component;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComponentMapper {

    Component mapToConfig(ComponentRepresentation representation);

    ComponentRepresentation mapToRepresentation(Component component);
}
