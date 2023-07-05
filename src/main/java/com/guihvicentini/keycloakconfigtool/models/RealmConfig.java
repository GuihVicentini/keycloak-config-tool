package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Getter
@Setter
@Slf4j
public class RealmConfig implements Config {
    private String id;
    private String realm;
    private String displayName;
    private String displayNameHtml;
    private Integer notBefore;
    private String defaultSignatureAlgorithm;
    private Boolean revokeRefreshToken;
    private Integer refreshTokenMaxReuse;
    private Integer accessTokenLifespan;
    private Integer accessTokenLifespanForImplicitFlow;
    private Integer ssoSessionIdleTimeout;
    private Integer ssoSessionMaxLifespan;
    private Integer ssoSessionIdleTimeoutRememberMe;
    private Integer ssoSessionMaxLifespanRememberMe;
    private Integer offlineSessionIdleTimeout;
    private Boolean offlineSessionMaxLifespanEnabled;
    private Integer offlineSessionMaxLifespan;
    private Integer clientSessionIdleTimeout;
    private Integer clientSessionMaxLifespan;
    private Integer clientOfflineSessionIdleTimeout;
    private Integer clientOfflineSessionMaxLifespan;
    private Integer accessCodeLifespan;
    private Integer accessCodeLifespanUserAction;
    private Integer accessCodeLifespanLogin;
    private Integer actionTokenGeneratedByAdminLifespan;
    private Integer actionTokenGeneratedByUserLifespan;
    private Integer oauth2DeviceCodeLifespan;
    private Integer oauth2DevicePollingInterval;
    private Boolean enabled;
    private String sslRequired;
    private Boolean registrationAllowed;
    private Boolean registrationEmailAsUsername;
    private Boolean rememberMe;
    private Boolean verifyEmail;
    private Boolean loginWithEmailAllowed;
    private Boolean duplicateEmailsAllowed;
    private Boolean resetPasswordAllowed;
    private Boolean editUsernameAllowed;
    private Boolean bruteForceProtected;
    private Boolean permanentLockout;
    private Integer maxFailureWaitSeconds;
    private Integer minimumQuickLoginWaitSeconds;
    private Integer waitIncrementSeconds;
    private Long quickLoginCheckMilliSeconds;
    private Integer maxDeltaTimeSeconds;
    private Integer failureFactor;

    private RolesConfig roles;
    private List<GroupConfig> groups;

    private RoleConfig defaultRole;
    private List<String> defaultGroups;

    private String passwordPolicy;
    private String otpPolicyType;
    private String otpPolicyAlgorithm;
    private Integer otpPolicyInitialCounter;
    private Integer otpPolicyDigits;
    private Integer otpPolicyLookAheadWindow;
    private Integer otpPolicyPeriod;
    private Boolean otpPolicyCodeReusable;

    private List<String> otpSupportedApplications;

    private String webAuthnPolicyRpEntityName;

    private List<String> webAuthnPolicySignatureAlgorithms;

    private String webAuthnPolicyRpId;
    private String webAuthnPolicyAttestationConveyancePreference;
    private String webAuthnPolicyAuthenticatorAttachment;
    private String webAuthnPolicyRequireResidentKey;
    private String webAuthnPolicyUserVerificationRequirement;
    private Integer webAuthnPolicyCreateTimeout;
    private Boolean webAuthnPolicyAvoidSameAuthenticatorRegister;

    private List<String> webAuthnPolicyAcceptableAaguids;

    private String webAuthnPolicyPasswordlessRpEntityName;

    private List<String> webAuthnPolicyPasswordlessSignatureAlgorithms;

    private String webAuthnPolicyPasswordlessRpId;
    private String webAuthnPolicyPasswordlessAttestationConveyancePreference;
    private String webAuthnPolicyPasswordlessAuthenticatorAttachment;
    private String webAuthnPolicyPasswordlessRequireResidentKey;
    private String webAuthnPolicyPasswordlessUserVerificationRequirement;
    private Integer webAuthnPolicyPasswordlessCreateTimeout;
    private Boolean webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister;

    private List<String> webAuthnPolicyPasswordlessAcceptableAaguids;

    private List<ScopeMappingConfig> scopeMappings;
    private Map<String, List<ScopeMappingConfig>> clientScopeMappings;
    private List<ClientConfig> clients;
    private List<ClientScopeConfig> clientScopes;

    private List<String> defaultClientScopes;

    private List<String> optionalClientScopes;
    private Map<String, String> browserSecurityHeaders;
    private Map<String, String> smtpServer;

    private String loginTheme;
    private String accountTheme;
    private String adminTheme;
    private String emailTheme;
    private Boolean eventsEnabled;
    private Long eventsExpiration;

    private List<String> eventsListeners;

    private List<String> enabledEventTypes;

    private Boolean adminEventsEnabled;
    private Boolean adminEventsDetailsEnabled;

    private List<IdentityProviderConfig> identityProviders;
    private List<Component> ldapProviders;
    private List<Component> keyProviders;

    private Boolean internationalizationEnabled;
    private Set<String> supportedLocales;
    private String defaultLocale;

    private List<AuthenticationFlow> authenticationFlows;

    private List<RequiredActionProviderConfig> requiredActions;

    private String browserFlow;
    private String registrationFlow;
    private String directGrantFlow;
    private String resetCredentialsFlow;
    private String clientAuthenticationFlow;
    private String dockerAuthenticationFlow;
    private Map<String, String> attributes;
    private Boolean userManagedAccessAllowed;

    /**
     * Replaces null objects with new empty objects and replace uuids with the corresponding object alias
     */
    @Override
    public void normalize() {
        normalizeRoles();
        normalizeGroups();
        normalizeDefaultGroups();
        normalizeOtpSupportedApplications();
        normalizeWebAuthnPolicySignatureAlgorithms();
        normalizeWebAuthnPolicyAcceptableAaguids();
        normalizeWebAuthnPolicyPasswordlessSignatureAlgorithms();
        normalizeWebAuthnPolicyPasswordlessAcceptableAaguids();

        normalizeDefaultRole();
        normalizeScopeMappings();
        normalizeClientScopeMappings();
        normalizeClients();
        normalizeClientScopes();
        normalizeDefaultClientScopes();
        normalizeOptionalClientScopes();
        normalizeBrowserSecurityHeaders();
        normalizeSmtpServer();
        normalizeEventsListeners();
        normalizeEnabledEventTypes();

        normalizeIdentityProviders();
        normalizeComponents();
        normalizeAuthenticationFlows();
//        normalizeAuthenticatorConfig();x
        normalizeRequiredActions();
        normalizeAttributes();
    }

    private void normalizeDefaultRole() {
        defaultRole =  defaultRole == null ? new RoleConfig() : defaultRole;
        defaultRole.normalize(this.realm);
    }

    private void normalizeRoles() {
        roles = roles == null ? new RolesConfig() : roles;
        roles.normalize(this.realm);
    }

    private void normalizeRequiredActions() {
        requiredActions = ListUtil.nullListToEmptyList(requiredActions);
        requiredActions.forEach(RequiredActionProviderConfig::normalize);
        requiredActions.sort(Comparator.comparing(RequiredActionProviderConfig::identifier));
    }

    private void normalizeEnabledEventTypes() {
        enabledEventTypes = ListUtil.nullListToEmptyList(enabledEventTypes);
        Collections.sort(enabledEventTypes);
    }

    private void normalizeEventsListeners() {
        eventsListeners = ListUtil.nullListToEmptyList(eventsListeners);
        Collections.sort(eventsListeners);
    }

    private void normalizeOptionalClientScopes() {
        optionalClientScopes = ListUtil.nullListToEmptyList(optionalClientScopes);
        Collections.sort(optionalClientScopes);
    }

    private void normalizeDefaultClientScopes() {
        defaultClientScopes = ListUtil.nullListToEmptyList(defaultClientScopes);
        Collections.sort(defaultClientScopes);
    }

    private void normalizeWebAuthnPolicyPasswordlessAcceptableAaguids() {
        webAuthnPolicyPasswordlessAcceptableAaguids = ListUtil.nullListToEmptyList(webAuthnPolicyPasswordlessAcceptableAaguids);
        Collections.sort(webAuthnPolicyPasswordlessAcceptableAaguids);
    }

    private void normalizeWebAuthnPolicyPasswordlessSignatureAlgorithms() {
        webAuthnPolicyPasswordlessSignatureAlgorithms = ListUtil.nullListToEmptyList(webAuthnPolicyPasswordlessSignatureAlgorithms);
        Collections.sort(webAuthnPolicyPasswordlessSignatureAlgorithms);
    }

    private void normalizeWebAuthnPolicyAcceptableAaguids() {
        webAuthnPolicyAcceptableAaguids = webAuthnPolicyAcceptableAaguids == null ? Collections.emptyList() : webAuthnPolicyAcceptableAaguids;
        Collections.sort(webAuthnPolicyAcceptableAaguids);
    }

    private void normalizeWebAuthnPolicySignatureAlgorithms() {
        webAuthnPolicySignatureAlgorithms = webAuthnPolicySignatureAlgorithms == null ? Collections.emptyList() : webAuthnPolicySignatureAlgorithms;
        Collections.sort(webAuthnPolicySignatureAlgorithms);
    }

    private void normalizeOtpSupportedApplications() {
        otpSupportedApplications = otpSupportedApplications == null ? Collections.emptyList() : otpSupportedApplications;
        Collections.sort(otpSupportedApplications);
    }

    private void normalizeDefaultGroups() {
        defaultGroups = defaultGroups == null ? Collections.emptyList() : defaultGroups;
        Collections.sort(defaultGroups);
    }

    private void normalizeAttributes() {
        attributes = attributes == null ? Collections.emptyMap() : attributes;
        attributes.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(attributes, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(attributes);
    }

    private void normalizeBrowserSecurityHeaders() {
        browserSecurityHeaders = browserSecurityHeaders == null ? Collections.emptyMap() : browserSecurityHeaders;
        browserSecurityHeaders.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(browserSecurityHeaders, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(browserSecurityHeaders);
    }

    private void normalizeAuthenticationFlows() {
        authenticationFlows = authenticationFlows == null ? Collections.emptyList() : authenticationFlows;
        authenticationFlows.forEach(AuthenticationFlow::normalize);
        authenticationFlows.sort(Comparator.comparing(AuthenticationFlow::identifier));
    }

    private void normalizeComponents() {
        ldapProviders = ldapProviders == null ? new ArrayList<>() : ldapProviders;
        ldapProviders.forEach(Component::normalize);
        ldapProviders.sort(Comparator.comparing(Component::getName));

        keyProviders = keyProviders == null ? new ArrayList<>() : keyProviders;
        keyProviders.forEach(Component::normalize);
        keyProviders.sort(Comparator.comparing(Component::getName));
    }

    private void normalizeIdentityProviders() {
        identityProviders = identityProviders == null ? Collections.emptyList() : identityProviders;
        identityProviders.forEach(IdentityProviderConfig::normalize);
        identityProviders.sort(Comparator.comparing(IdentityProviderConfig::identifier));
    }

    private void normalizeSmtpServer() {
        smtpServer = smtpServer == null ? Collections.emptyMap() : smtpServer;
        // remove password from config definition.
        smtpServer.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(smtpServer, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(smtpServer);
    }

    private void normalizeClientScopes() {
        clientScopes.forEach(ClientScopeConfig::normalize);
        clientScopes.sort(Comparator.comparing(ClientScopeConfig::identifier));
    }

    private void normalizeClients() {
        clients.forEach(ClientConfig::normalize);
        clients.sort(Comparator.comparing(ClientConfig::identifier));
    }

    private void normalizeClientScopeMappings() {
        clientScopeMappings = clientScopeMappings == null ? Collections.emptyMap() : clientScopeMappings;
        clientScopeMappings.values().forEach(list -> {
            list.forEach(ScopeMappingConfig::normalize);
            list.sort(Comparator.comparing(ScopeMappingConfig::identifier));
        });
    }

    private void normalizeGroups() {
        groups = groups == null ? Collections.emptyList() : groups;
        groups.forEach(GroupConfig::normalize);
        groups.sort(Comparator.comparing(GroupConfig::identifier));
    }

    private void normalizeScopeMappings() {
        scopeMappings.forEach(ScopeMappingConfig::normalize);
        scopeMappings.sort(Comparator.comparing(ScopeMappingConfig::identifier));
    }

    @Override
    public String identifier() {
        return id;
    }
}
