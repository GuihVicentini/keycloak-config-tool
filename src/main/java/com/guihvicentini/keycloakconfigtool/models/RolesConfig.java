package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.exceptions.ConfigNotComparableException;
import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class RolesConfig implements Config {
    private List<RoleConfig> realm;
    private Map<String, List<RoleConfig>> client;


    @Override
    public void normalize(String realm) {
        normalizeRealm(realm);
        normalizeClient();
    }

    @Override
    public String identifier() {
        throw new ConfigNotComparableException("RolesConfig");
    }

    private void normalizeClient() {
        client = client == null ? new HashMap<>() : client;
        client.forEach((k, v) -> {
            v.forEach(roleConfig -> roleConfig.normalize(k));
            v.sort(Comparator.comparing(RoleConfig::getName));
        });
        MapUtil.sortMapByKey(client);
    }

    private void normalizeRealm(String realm) {
        this.realm = this.realm == null ? new ArrayList<>() : this.realm;
        this.realm.forEach(roleConfig -> roleConfig.normalize(realm));
        this.realm.sort(Comparator.comparing(RoleConfig::getName));
    }
}
