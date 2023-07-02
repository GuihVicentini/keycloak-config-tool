package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.adapters.RealmResourceAdapter;
import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.mappers.RealmConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RealmExportService {


    private final RealmResourceAdapter realmResourceAdapter;

    private final IdentityProviderExportService identityProviderExportService;
    private final AuthenticationFlowExportService authenticationFlowExportService;
    private final ClientExportService clientExportService;
    private final ClientScopeExportService clientScopeExportService;
    private final GroupExportService groupExportService;
    private final RequiredActionExportService requiredActionExportService;
    private final RoleExportService roleExportService;
    private final ComponentExportService componentExportService;

    private final RealmConfigMapper mapper;


    public RealmExportService(RealmResourceAdapter realmResourceAdapter,
                              IdentityProviderExportService identityProviderExportService,
                              AuthenticationFlowExportService authenticationFlowExportService,
                              ClientExportService clientExportService,
                              ClientScopeExportService clientScopeExportService,
                              GroupExportService groupExportService,
                              RequiredActionExportService requiredActionExportService,
                              RoleExportService roleExportService,
                              ComponentExportService componentExportService,
                              RealmConfigMapper mapper) {
        this.realmResourceAdapter = realmResourceAdapter;
        this.identityProviderExportService = identityProviderExportService;
        this.authenticationFlowExportService = authenticationFlowExportService;
        this.clientExportService = clientExportService;
        this.clientScopeExportService = clientScopeExportService;
        this.requiredActionExportService = requiredActionExportService;
        this.roleExportService = roleExportService;
        this.groupExportService = groupExportService;
        this.componentExportService = componentExportService;
        this.mapper = mapper;
    }

    public RealmConfig getRealm(String realm) {
        RealmConfig realmConfig = getFullRealm(realm);
        if (realmConfig != null) {
            realmConfig.normalize();
        }
        return realmConfig;
    }

    private RealmRepresentation getPartialExportRealm(String realm) {
        RealmRepresentation representation = realmResourceAdapter.exists(realm) ? realmResourceAdapter.getPartialExport(realm) : null;

        if (representation == null) {
            log.warn("Realm: {} doesn't exist, returning null value instead.", realm);
            return null;
        }

        replaceFlowUuidWithFlowAlias(representation);

        return representation;
    }

    public RealmConfig getFullRealm(String realm) {
        if (!realmResourceAdapter.exists(realm)) {
            log.warn("Realm: {} doesn't exist, returning null value instead.", realm);
            return null;
        }

        RealmRepresentation representation = realmResourceAdapter.get(realm);
        RealmConfig config = mapper.mapToConfig(representation);
        config.setDefaultClientScopes(realmResourceAdapter.getDefaultClientScopes(realm));
        config.setOptionalClientScopes(realmResourceAdapter.getOptionalClientScopes(realm));

        config.setAuthenticationFlows(authenticationFlowExportService.getAllFlows(realm));
        config.setIdentityProviders(identityProviderExportService.getAll(realm));
        config.setClients(clientExportService.getAllClients(realm));
        config.setClientScopes(clientScopeExportService.getAllClientScopes(realm));
        config.setRoles(roleExportService.getRealmAndClientRoles(realm));
        config.setRequiredActions(requiredActionExportService.getAll(realm));
        config.setGroups(groupExportService.getGroupConfigs(realm));
        config.setLdapProviders(componentExportService.getAll(realm, ConfigConstants.USER_STORAGE_PROVIDER_TYPE));
        config.setKeyProviders(componentExportService.getAll(realm, ConfigConstants.KEY_PROVIDER_TYPE));

        config.normalize();

        return config;
    }



    private void replaceFlowUuidWithFlowAlias(RealmRepresentation representation) {
        representation.getClients().forEach(client -> {
            client.getAuthenticationFlowBindingOverrides().forEach((key, value) -> {
                 var flowRepresentation = representation
                         .getAuthenticationFlows().stream().filter(flow -> flow.getId().equals(value))
                         .findFirst()
                         .orElseThrow(() -> new KeycloakAdapterException("Flow not found id: %s", value));

                 client.getAuthenticationFlowBindingOverrides().put(key,flowRepresentation.getAlias());
            });
        });
    }

}
