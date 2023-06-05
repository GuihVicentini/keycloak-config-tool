package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.RoleConfig;
import com.guihvicentini.keycloakconfigtool.models.RolesConfig;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RolesConfigMapper {

    RolesConfig mapToConfig(RolesRepresentation rolesRepresentation);

    RoleRepresentation mapToRepresentation(RoleConfig roleConfig);

    Map<String, List<RoleConfig>> mapToRolesConfigMap(Map<String, List<RoleRepresentation>> map);

    Map<String,List<RoleRepresentation>> mapToRolesRepresentationMap(Map<String,List<RoleConfig>> value);

    List<RoleConfig> mapToRoleConfigList(List<RoleRepresentation> value);

    List<RoleRepresentation> mapToRoleRepresentationList(List<RoleConfig> value);

}
