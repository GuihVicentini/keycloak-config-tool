package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import com.guihvicentini.keycloakconfigtool.utils.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ClientConfig implements Config {
    private String clientId;
    private String name;
    private String description;
    private String rootUrl;
    private String baseUrl;
    private String adminUrl;
    private boolean surrogateAuthRequired;
    private boolean enabled;
    private boolean alwaysDisplayInConsole;
    private String clientAuthenticatorType;
    private List<String> defaultRoles;
    private List<String> redirectUris;
    private List<String> webOrigins;
    private Integer notBefore;
    private boolean bearerOnly;
    private boolean consentRequired;
    private boolean standardFlowEnabled;
    private boolean implicitFlowEnabled;
    private boolean directAccessGrantsEnabled;
    private boolean serviceAccountsEnabled;
    private boolean publicClient;
    private boolean frontchannelLogout;
    private String protocol;
    private Map<String, String> attributes;

    // Authentication flow has flow alias instead of flow uuid
    private Map<String, String> authenticationFlowBindingOverrides;

    private boolean fullScopeAllowed;
    private Integer nodeReRegistrationTimeout;
    private List<ProtocolMapperConfig> protocolMappers;

    private List<String> defaultClientScopes;
    private List<String> optionalClientScopes;
    private Map<String,Boolean> access;


    @Override
    public void normalize(){
        normalizeAttributes();
        normalizeAccess();
        normalizeMappers();
        normalizeLists();
        normalizeAuthenticationFlowBindings();
    }

    private void normalizeAuthenticationFlowBindings() {
        authenticationFlowBindingOverrides = authenticationFlowBindingOverrides == null ? new HashMap<>() : authenticationFlowBindingOverrides;
        MapUtil.sortMapByKey(authenticationFlowBindingOverrides);
    }

    private void normalizeLists() {
        defaultRoles = ListUtil.nullListToEmptyList(defaultRoles);
        Collections.sort(defaultRoles);

        redirectUris = ListUtil.nullListToEmptyList(redirectUris);
        Collections.sort(redirectUris);

        webOrigins = ListUtil.nullListToEmptyList(webOrigins);
        Collections.sort(webOrigins);

        defaultClientScopes = ListUtil.nullListToEmptyList(defaultClientScopes);
        Collections.sort(defaultClientScopes);

        optionalClientScopes = ListUtil.nullListToEmptyList(optionalClientScopes);
        Collections.sort(optionalClientScopes);
    }

    private void normalizeAttributes() {
        attributes = attributes == null ? new HashMap<>() : attributes;
        attributes.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(attributes, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(attributes);
    }

    private void normalizeMappers() {
        protocolMappers = protocolMappers == null ? new ArrayList<>() : protocolMappers;
        protocolMappers.forEach(ProtocolMapperConfig::normalize);
        protocolMappers.sort(Comparator.comparing(ProtocolMapperConfig::getName));
    }

    private void normalizeAccess() {
        access = access == null ? new HashMap<>() : access;
        access.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(access);
    }

    public boolean hasRealmSuffix(){
        return clientId.endsWith(ConfigConstants.REALM_AS_CLIENT_SUFFIX);
    }

    public String clientIdWithoutRealmSuffix(){
        return hasRealmSuffix() ? StringUtil.removeSuffix(clientId, ConfigConstants.REALM_AS_CLIENT_SUFFIX) : clientId;
    }

    @Override
    public String identifier() {
        return clientId;
    }

}
