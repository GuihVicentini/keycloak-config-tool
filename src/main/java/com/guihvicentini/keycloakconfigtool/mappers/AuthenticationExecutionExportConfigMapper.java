package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.AuthenticationExecutionExportConfig;
import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;
import org.mapstruct.Mapper;

@Mapper
public interface AuthenticationExecutionExportConfigMapper {

    AuthenticationExecutionExportConfig mapToConfig(AuthenticationExecutionExportRepresentation authenticationExecutionExportRepresentation);

}
