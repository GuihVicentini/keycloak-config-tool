package com.guihvicentini.keycloakconfigtool.services;

import com.guihvicentini.keycloakconfigtool.adapters.AuthenticationManagementResourceAdapter;
import com.guihvicentini.keycloakconfigtool.mappers.RequiredActionProviderConfigMapper;
import com.guihvicentini.keycloakconfigtool.models.ConfigConstants;
import com.guihvicentini.keycloakconfigtool.models.RequiredActionProviderConfig;
import com.guihvicentini.keycloakconfigtool.utils.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RequiredActionImportService {

    private final AuthenticationManagementResourceAdapter resourceAdapter;

    private final RequiredActionProviderConfigMapper actionsConfigMapper;

    public RequiredActionImportService(AuthenticationManagementResourceAdapter resourceAdapter,
                                       RequiredActionProviderConfigMapper actionsConfigMapper) {
        this.resourceAdapter = resourceAdapter;
        this.actionsConfigMapper = actionsConfigMapper;
    }

    public void doImport(String realm,
                         List<RequiredActionProviderConfig> actual,
                         List<RequiredActionProviderConfig> target) {

        if(target.equals(actual)) {
            log.info(ConfigConstants.UP_TO_DATE_MESSAGE);
            return;
        }

        importRequiredActions(realm, actual, target);

    }

    private void importRequiredActions(String realm,
                                      List<RequiredActionProviderConfig> actual,
                                      List<RequiredActionProviderConfig> target) {

        List<RequiredActionProviderConfig> toBeAdded = ListUtil.getMissingConfigElements(target, actual);
        List<RequiredActionProviderConfig> toBeDeleted = ListUtil.getMissingConfigElements(actual, target);
        List<RequiredActionProviderConfig> toBeUpdated = ListUtil.getNonEqualConfigsWithSameIdentifier(target, actual);

        addRequiredActions(realm, toBeAdded);
        updateRequiredActions(realm, toBeUpdated);
        deleteRequiredActions(realm, toBeDeleted);

    }

    private void deleteRequiredActions(String realm, List<RequiredActionProviderConfig> actions) {
        actions.forEach(action -> deleteRequiredAction(realm, action));
    }

    private void deleteRequiredAction(String realm, RequiredActionProviderConfig action) {
        resourceAdapter.deleteRequiredAction(realm, action.getAlias());
    }

    private void updateRequiredActions(String realm, List<RequiredActionProviderConfig> actions) {
        actions.forEach(action -> updateRequiredAction(realm, action));
    }

    private void updateRequiredAction(String realm, RequiredActionProviderConfig action) {
        resourceAdapter.updateRequiredAction(realm, action.getAlias(), actionsConfigMapper.mapToRepresentation(action));
    }

    private void addRequiredActions(String realm, List<RequiredActionProviderConfig> actions) {
        actions.forEach(action -> addRequiredAction(realm, action));
    }

    private void addRequiredAction(String realm, RequiredActionProviderConfig action) {
        resourceAdapter.createRequiredAction(realm, actionsConfigMapper.mapToRepresentation(action));
    }

}
