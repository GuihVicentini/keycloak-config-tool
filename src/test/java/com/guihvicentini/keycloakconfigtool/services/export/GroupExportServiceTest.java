package com.guihvicentini.keycloakconfigtool.services.export;


import com.guihvicentini.keycloakconfigtool.adapters.GroupResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.GroupConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.GroupConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest.TEST_REALM;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupExportServiceTest {


    @Mock
    GroupConfigMapper mapper;
    @Mock
    GroupResourceAdapter adapter;
    @InjectMocks
    GroupExportService groupExportService;

    @Test
    public void whenGetGroupConfigs_groupConfigsAreReturned() {
        var groupRepresentations = getGroupRepresentations();

        when(adapter.getAll(TEST_REALM)).thenReturn(groupRepresentations);
        when(mapper.mapToConfig(any())).thenReturn(new GroupConfig());

        var groups = groupExportService.getGroupConfigs(TEST_REALM);

        assertThat(groups, isNotNull());
        assertThat(groups.size(), equalTo(2));
    }

    private List<GroupRepresentation> getGroupRepresentations() {
        return Arrays.asList(
                new GroupRepresentation(),
                new GroupRepresentation()
        );
    }

}
