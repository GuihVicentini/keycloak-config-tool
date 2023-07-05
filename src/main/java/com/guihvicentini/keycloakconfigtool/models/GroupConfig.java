package com.guihvicentini.keycloakconfigtool.models;

import com.guihvicentini.keycloakconfigtool.utils.MapUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class GroupConfig implements Config {

    private String name;
    private String path;
    private Map<String, Boolean> access;
    private Map<String, List<String>> attributes;
    private List<String> realmRoles;
    private Map<String, List<String>> clientRoles;
    private List<GroupConfig> subGroups;

    @Override
    public void normalize() {
        normalizeAttributes();
        normalizeClientRoles();
        normalizeAccess();
        normalizeRealmRoles();
        normalizeSubGroups();
    }

    private void normalizeSubGroups() {
        subGroups = subGroups == null ? new ArrayList<>() : subGroups;
        subGroups.sort(Comparator.comparing(GroupConfig::identifier));
    }

    private void normalizeRealmRoles() {
        realmRoles = realmRoles == null ? new ArrayList<>() : realmRoles;
        Collections.sort(realmRoles);
    }

    private void normalizeAccess() {
        access = access == null ? new HashMap<>() : access;
        access.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(access);
    }

    private void normalizeClientRoles() {
        clientRoles = clientRoles == null ? new HashMap<>() : clientRoles;
        clientRoles.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(clientRoles);
        clientRoles.values().forEach(Collections::sort);
    }

    private void normalizeAttributes() {
        attributes = attributes == null ? new HashMap<>() : attributes;
        attributes.keySet().removeAll(ConfigConstants.VALUES_TO_OMIT_FROM_CONFIG);
        MapUtil.sortMapByKey(attributes);
        attributes.values().forEach(Collections::sort);
    }

    @Override
    public String identifier() {
        return name;
    }
}
