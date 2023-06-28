package com.guihvicentini.keycloakconfigtool.mappers;

import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import com.guihvicentini.keycloakconfigtool.models.ScopeMappingConfig;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.ScopeMappingRepresentation;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
//            AuthenticationExecutionExportConfigMapper.class,
//                AuthenticationFlowConfigMapper.class,
                ClientConfigMapper.class,
                ClientScopeConfigMapper.class,
                ComponentExportConfigMapper.class,
                GroupConfigMapper.class,
                IdentityProviderConfigMapper.class,
                IdentityProviderMapperConfigMapper.class,
                ProtocolMapperConfigMapper.class,
                RequiredActionProviderConfigMapper.class,
                RolesConfigMapper.class,
                ScopeMappingConfigMapper.class
        })
public interface RealmConfigMapper {

    @Mapping(target = "id", source = "realm")
    @Mapping(target = "defaultClientScopes", source = "defaultDefaultClientScopes")
    @Mapping(target = "optionalClientScopes", source = "defaultOptionalClientScopes")
    // For some reason MapStruct expects the first 2 characters to be uppercase.
    @Mapping(target = "oauth2DeviceCodeLifespan", source = "OAuth2DeviceCodeLifespan")
    @Mapping(target = "oauth2DevicePollingInterval", source = "OAuth2DevicePollingInterval")
    RealmConfig mapToConfig(RealmRepresentation representation);


    // RealmRepresentation expects and UUID as id instead of realm name
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "defaultDefaultClientScopes", source = "defaultClientScopes")
    @Mapping(target = "defaultOptionalClientScopes", source = "optionalClientScopes")
    // For some reason MapStruct expects the first 2 characters to be uppercase.
    @Mapping(target = "OAuth2DeviceCodeLifespan", source = "oauth2DeviceCodeLifespan")
    @Mapping(target = "OAuth2DevicePollingInterval", source = "oauth2DevicePollingInterval")
    RealmRepresentation mapToRepresentation(RealmConfig config);

    List<ScopeMappingConfig> mapToConfigList(List<ScopeMappingRepresentation> representations);
    List<ScopeMappingRepresentation> mapToRepresentationList(List<ScopeMappingConfig> value);

    Map<String,List<ScopeMappingConfig>> mapScopeMappingList(Map<String,List<ScopeMappingRepresentation>> value);
    Map<String,List<ScopeMappingRepresentation>> mapScopeMappingToRepresentation(Map<String,List<ScopeMappingConfig>> value);

}
