package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class IdentityProviderConfig implements Config {
    private String alias;
    private String displayName;
    private String providerId;
    private boolean enabled = true;
    private boolean trustEmail;
    private boolean storeToken;
    private boolean addReadTokenRoleOnCreate;
    private boolean authenticateByDefault;
    private boolean linkOnly;
    private String firstBrokerLoginFlowAlias;
    private String postBrokerLoginFlowAlias;
    private Map<String, String> config;

    private List<IdentityProviderMapperConfig> mappers;

    @Override
    public void normalize() {
        config = config == null ? new HashMap<>() : config;
        config.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.removeMatchingEntries(config, ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG_BASED_ON_VALUE);
        MapUtil.sortMapByKey(config);
    }

    @Override
    public String identifier() {
        return alias;
    }

}
