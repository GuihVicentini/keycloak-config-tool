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
        // remove admin role from master realm. This role is sensible and should not be changed.
        if(realm.equals("master")) {
            Optional<RoleConfig> adminRole = this.realm.stream().filter(role -> role.getName().equals("admin")).findFirst();
            adminRole.ifPresent(role -> this.realm.remove(role));
        }
        this.realm.sort(Comparator.comparing(RoleConfig::getName));
    }
}
