package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class GroupExportServiceIT extends AbstractIntegrationTest {

    @Autowired
    GroupExportService groupExportService;

    @Test
    public void getAllGroupConfigs() {
        var groups = groupExportService.getGroupConfigs(TEST_REALM);

        assertThat(groups, is(notNullValue()));
        assertThat(groups.size(), equalTo(1));

        var group = groups.get(0);

        assertThat(group.getName(), equalTo("test-group"));
        assertThat(group.getPath(), equalTo("/test-group"));

        assertThat(group.getRealmRoles(), is(notNullValue()));
        assertThat(group.getRealmRoles().size(), equalTo(1));
        assertThat(group.getRealmRoles().get(0), equalTo("create-realm"));

        assertThat(group.getClientRoles().size(), equalTo(1));
        assertThat(group.getClientRoles().get("account").size(), equalTo(3));
        assertThat(group.getClientRoles().get("account").get(0), equalTo("manage-account-links"));
        assertThat(group.getClientRoles().get("account").get(1), equalTo("manage-account"));
        assertThat(group.getClientRoles().get("account").get(2), equalTo("delete-account"));

        assertThat(group.getSubGroups(), is(notNullValue()));
        assertThat(group.getSubGroups().size(), equalTo(1));

        assertThat(group.getAttributes(), is(notNullValue()));
        assertThat(group.getAttributes().size(), equalTo(2));
        assertThat(group.getAttributes().get("toto").get(0), equalTo("toto"));
        assertThat(group.getAttributes().get("test").get(0), equalTo("test"));

        assertThat(group.getAccess(), is(notNullValue()));
        assertThat(group.getAccess().size(), equalTo(3));
        assertThat(group.getAccess().get("view"), equalTo(true));
        assertThat(group.getAccess().get("manage"), equalTo(true));
        assertThat(group.getAccess().get("manageMembership"), equalTo(true));
    }

}
