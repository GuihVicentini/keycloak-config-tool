package com.guihvicentini.keycloakconfigtool.models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigConstants {
    public static final Set<String> VALUES_TO_OMIT_FROM_CONFIG = new HashSet<>(
            Arrays.asList(
                    "password",
                    "bindCredential",
                    "org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy",
                    "org.keycloak.userprofile.UserProfileProvider"
            ));

    public static final Map<String, String> VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE = Map.of(
            "display.on.consent.screen", "false",
            "saml.assertion.signature","false"
    );

    public static final Map<String, String> KEYCLOAK_PROVIDERS_NAME = Map.of(
            "userStorageProvider", "org.keycloak.storage.UserStorageProvider",
            "lDAPStorageMapper","org.keycloak.storage.ldap.mappers.LDAPStorageMapper",
            "clientRegistrationPolicy","org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy",
            "keyProvider","org.keycloak.keys.KeyProvider",
            "userProfileProvider", "org.keycloak.userprofile.UserProfileProvider"
    );

    public static final String UP_TO_DATE_MESSAGE = "Up-to-date. No change needed";

    public static final String MASTER_REALM_NAME = "master";

    public static final String REALM_AS_CLIENT_SUFFIX = "-realm";

    public static final String USER_STORAGE_PROVIDER_TYPE = "org.keycloak.storage.UserStorageProvider";

    public static final String KEY_PROVIDER_TYPE = "org.keycloak.keys.KeyProvider";

    public static final String LDAP_MAPPER_TYPE = "org.keycloak.storage.ldap.mappers.LDAPStorageMapper";

}
