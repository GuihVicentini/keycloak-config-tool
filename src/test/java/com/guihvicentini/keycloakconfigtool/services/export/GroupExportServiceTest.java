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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        verify(adapter, times(1)).getAll(TEST_REALM);
        verify(mapper, times(2)).mapToConfig((any()));
        assertThat(groups,  is(notNullValue()));
        assertThat(groups.size(), equalTo(2));
    }

    @Test
    public void whenAdapterReturnNulls_emptyListIsReturned() {
        when(adapter.getAll(TEST_REALM)).thenReturn(null);

        var groups = groupExportService.getGroupConfigs(TEST_REALM);

        verify(adapter, times(1)).getAll(TEST_REALM);
        verify(mapper, never()).mapToConfig((any()));
        assertThat(groups,  is(notNullValue()));
        assertThat(groups.size(), equalTo(0));
    }

    private List<GroupRepresentation> getGroupRepresentations() {
        return Arrays.asList(
                new GroupRepresentation(),
                new GroupRepresentation()
        );
    }

}
