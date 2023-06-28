package com.guihvicentini.keycloakconfigtool.services.export;

import com.guihvicentini.keycloakconfigtool.containers.AbstractIntegrationTest;
import com.guihvicentini.keycloakconfigtool.models.AuthenticationFlow;
import com.guihvicentini.keycloakconfigtool.utils.JsonMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class AuthenticationFlowExportServiceIT extends AbstractIntegrationTest {

    @Autowired
    AuthenticationFlowExportService exportService;

//    @Autowired
//    AuthenticationManagementResourceAdapter resourceAdapter;
//
//    @Test
//    public void getAllFlows() {
//        List<AuthenticationFlowConfig> flows = exportService.getAll(TEST_REALM);
//        log.info("Flows: {}", JsonMapperUtils.objectToJsonPrettyString(flows));
//
//        Optional<AuthenticationFlowConfig> testFlow = flows.stream().filter(flow -> flow.identifier().equals("test-flow"))
//                .findFirst();
//
//        assertTrue(testFlow.isPresent());
//
//        Optional<AuthenticationFlowConfig> testSubFlow = flows.stream().filter(flow -> flow.identifier().equals("test-sub-flow"))
//                .findFirst();
//
//        assertTrue(testSubFlow.isPresent());
//
//    }
//
//    @Test
//    public void getFlowsRepresentation(){
//        var flows = exportService.getAuthFlows(TEST_REALM);
//        log.info("Flows: {}", JsonMapperUtils.objectToJsonPrettyString(flows));
//
//        flows.forEach(flow -> {
//            var exec = resourceAdapter.getAuthenticationExecutions(TEST_REALM, flow.getAlias());
//            log.info("Subflow: {}", JsonMapperUtils.objectToJsonPrettyString(exec));
//        });
//    }
//
//    @Test
//    public void getFlowExecutions() {
//        var executions = exportService.getAllFlowExecutions(TEST_REALM, "test-sub-flow");
//        log.info("Flow Executions: {}", JsonMapperUtils.objectToJsonPrettyString(executions));
//    }

    @Test
    public void getFlows() {
        List<AuthenticationFlow> flows = exportService.getAllFlows(TEST_REALM);
        log.info("Flows: {}", JsonMapperUtils.objectToJsonPrettyString(flows));

        Optional<AuthenticationFlow> maybeTestFlow = flows.stream().filter(flow -> flow.identifier().equals("test-flow"))
                .findFirst();

        assertTrue(maybeTestFlow.isPresent());
        AuthenticationFlow testFlow = maybeTestFlow.get();

        assertFalse(testFlow.getSubFlowsAndExecutions().isEmpty());

    }

}
