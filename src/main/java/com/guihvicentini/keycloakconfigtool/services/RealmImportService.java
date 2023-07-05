package com.guihvicentini.keycloakconfigtool.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guihvicentini.keycloakconfigtool.adapters.RealmResourceAdapter;
import com.guihvicentini.keycloakconfigtool.exceptions.KeycloakAdapterException;
import com.guihvicentini.keycloakconfigtool.exceptions.RealmConfigCheckerException;
import com.guihvicentini.keycloakconfigtool.mappers.RealmConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import com.guihvicentini.keycloakconfigtool.services.export.RealmExportService;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RealmImportService {

    private final RealmConfigMapper realmConfigMapper;
    private final RealmResourceAdapter realmResourceAdapter;
    private final AuthenticationFlowImportService authenticationFlowImportService;
    private final ComponentImportService componentImportService;
    private final IdentityProviderImportService identityProviderImportService;
    private final ClientImportService clientImportService;
    private final RequiredActionImportService requiredActionImportService;
    private final ClientScopeImportService clientScopeImportService;
    private final GroupImportService groupImportService;
    private final RoleImportService roleImportService;
    private final RealmExportService realmExportService;
    private final ObjectMapper objectMapper;

    public RealmImportService(RealmConfigMapper realmConfigMapper,
                              RealmResourceAdapter realmResourceAdapter,
                              AuthenticationFlowImportService authenticationFlowImportService,
                              ComponentImportService componentImportService,
                              IdentityProviderImportService identityProviderImportService,
                              ClientImportService clientImportService,
                              RequiredActionImportService requiredActionImportService,
                              ClientScopeImportService clientScopeImportService,
                              GroupImportService groupImportService,
                              RoleImportService roleImportService,
                              RealmExportService realmExportService,
                              ObjectMapper objectMapper) {
        this.realmConfigMapper = realmConfigMapper;
        this.realmResourceAdapter = realmResourceAdapter;
        this.authenticationFlowImportService = authenticationFlowImportService;
        this.componentImportService = componentImportService;
        this.identityProviderImportService = identityProviderImportService;
        this.clientImportService = clientImportService;
        this.requiredActionImportService = requiredActionImportService;
        this.clientScopeImportService = clientScopeImportService;
        this.groupImportService = groupImportService;
        this.roleImportService = roleImportService;
        this.realmExportService = realmExportService;
        this.objectMapper = objectMapper;
    }

    public void importConfig(RealmConfig realmConfig) {

        RealmConfig actualConfig = realmExportService.getRealm(realmConfig.getRealm());

        log.info("Current config: {}", objectMapper.valueToTree(actualConfig).toPrettyString());
        log.info("Target config: {}", objectMapper.valueToTree(realmConfig).toPrettyString());

        if(actualConfig == null) {
            log.info("Realm {} doesn't exist, creating realm.", realmConfig.getRealm());
            createRealm(realmConfig);
            return;
        }

        if (realmConfig.equals(actualConfig)) {
            log.info("Target configuration is equal to current configuration. nothing to change.");
            return;
        }

        log.info("Realm {} already exits, updating realm.", realmConfig.getRealm());
        update(actualConfig, realmConfig);
    }



    private void createRealm(RealmConfig realmConfig) {
        // first create an empty realm and then update the realm accordingly.
        realmResourceAdapter.init(realmConfig.getRealm());

        // get the default values after the realm is created.
        RealmConfig actualConfig = realmExportService.getRealm(realmConfig.getRealm());
        update(actualConfig, realmConfig);
    }

    private void update(RealmConfig actualConfig, RealmConfig targetConfig) {

        authenticationFlowImportService.doImport(targetConfig.getRealm(), actualConfig.getAuthenticationFlows(),
                targetConfig.getAuthenticationFlows());

        // work with config models and only convert to keycloak representations before making requests to the adapters.

        componentImportService.doImport(targetConfig.getRealm(), actualConfig.getLdapProviders(), targetConfig.getLdapProviders());
        componentImportService.doImport(targetConfig.getRealm(), actualConfig.getKeyProviders(), targetConfig.getKeyProviders());

        identityProviderImportService.doImport(targetConfig.getRealm(), actualConfig.getIdentityProviders(),
                targetConfig.getIdentityProviders());

        requiredActionImportService.doImport(targetConfig.getRealm(),
                actualConfig.getRequiredActions(),
                targetConfig.getRequiredActions());

        clientScopeImportService.doImport(targetConfig.getRealm(), actualConfig.getClientScopes(), targetConfig.getClientScopes());

        clientImportService.doImport(targetConfig.getRealm(), actualConfig.getClients(), targetConfig.getClients());

        roleImportService.doImport(targetConfig.getRealm(), actualConfig.getRoles(), targetConfig.getRoles());

        groupImportService.doImport(targetConfig.getRealm(), actualConfig.getGroups(), targetConfig.getGroups());

        RealmRepresentation targetRepresentation = realmConfigMapper.mapToRepresentation(targetConfig);

        setRepresentationScopeMappings(targetConfig, targetRepresentation);

        realmResourceAdapter.update(targetRepresentation);

        importDefaultClientScopes(targetConfig.getRealm(), actualConfig.getDefaultClientScopes(), targetConfig.getDefaultClientScopes());
        importOptionalClientScopes(targetConfig.getRealm(), actualConfig.getOptionalClientScopes(), targetConfig.getOptionalClientScopes());

        performCheck(targetConfig);
    }

    private void importOptionalClientScopes(String realm, List<String> actual, List<String> target) {
        List<String> toBeAdded = ListUtil.getMissingElements(target, actual);
        List<String> toBeDeleted = ListUtil.getMissingElements(actual, target);

        toBeAdded.forEach(scope -> realmResourceAdapter.addOptionalClientScope(realm, scope));
        toBeDeleted.forEach(scope -> realmResourceAdapter.removeOptionalClientScope(realm, scope));
    }

    private void importDefaultClientScopes(String realm, List<String> actual, List<String> target) {
        List<String> toBeAdded = ListUtil.getMissingElements(target, actual);
        List<String> toBeDeleted = ListUtil.getMissingElements(actual, target);

        toBeAdded.forEach(scope -> realmResourceAdapter.addDefaultClientScope(realm, scope));
        toBeDeleted.forEach(scope -> realmResourceAdapter.removeDefaultClientScope(realm, scope));
    }


    /**
     * The @RealmRepresentation don't have a method to set the list of @ScopeMappingRepresentation.
     * Instead, there are 2 methods to set the clientScope scopes and the client scopes.
     * These methods add the created scope mappings directly to the scopeMappings list.
     * Nevertheless, the method create the @ScopeMappingRepresentation only with the name.
     * Therefore, the roles of that scope must be added explicitly.
     * @param realmConfig the RealConfig object containing the ScopeMappings list.
     * @param representation the @RealmRepresentation that needs to create the ScopeMappings list.
     */
    private void setRepresentationScopeMappings(RealmConfig realmConfig, RealmRepresentation representation) {
        realmConfig.getScopeMappings().forEach(scopeMappingConfig -> {
            if (scopeMappingConfig.getClientScope() != null){
                representation.clientScopeScopeMapping(scopeMappingConfig.getClientScope());
                representation.getScopeMappings().stream().filter(scope -> scope.getClientScope()
                        .equals(scopeMappingConfig.getClientScope()))
                        .findFirst()
                        .orElseThrow(() -> new KeycloakAdapterException("ScopeMapping %s was not added " +
                                "to representation", scopeMappingConfig.getClientScope()))
                        .setRoles(scopeMappingConfig.getRoles());
            }
            if (scopeMappingConfig.getClient() != null){
                representation.clientScopeMapping(scopeMappingConfig.getClient());
                representation.getScopeMappings().stream().filter(scope -> scope.getClient()
                        .equals(scopeMappingConfig.getClient())).findFirst()
                        .orElseThrow(() -> new KeycloakAdapterException("ScopeMapping %s was not added " +
                                "to representation", scopeMappingConfig.getClient()))
                        .setRoles(scopeMappingConfig.getRoles());
            }
        });
    }

    private void performCheck(RealmConfig realmConfig) {
        RealmConfig actualConfig = realmExportService.getRealm(realmConfig.getRealm());
        check(actualConfig, realmConfig);
    }

    private  void check(RealmConfig actualConfig, RealmConfig targetConfig) {
        if (actualConfig == null && targetConfig == null) {
            log.debug("Nothing to check, objects are null");
            return;
        }

        if(targetConfig == null) {
            throw new IllegalStateException("targetConfig realm config is null and actualConfig not");
        }

        if(actualConfig != null) {
            actualConfig.normalize();
        }
        targetConfig.normalize();

        JsonNode expected = objectMapper.valueToTree(targetConfig);
        JsonNode actual = objectMapper.valueToTree(actualConfig);

        log.info("Expected:");
        log.info(expected.toPrettyString());
        log.info("Actual:");
        log.info(actual.toPrettyString());

        if(!expected.equals(actual)) {
            JsonMapperUtils.logDifferences(log, expected, actual, Level.ERROR);
            throw new RealmConfigCheckerException(expected, actual);
        }

    }
}
