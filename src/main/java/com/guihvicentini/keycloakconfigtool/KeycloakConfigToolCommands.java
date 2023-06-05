package com.guihvicentini.keycloakconfigtool;

import com.guihvicentini.keycloakconfigtool.commands.ConfigCommands;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "keycloak-config-tool",
        mixinStandardHelpOptions = true,
        version = "2.0.0",
        description = "CLI tool to import and export keycloak configuration",
        subcommands = {
                ConfigCommands.class
        }
)

@Component
public class KeycloakConfigToolCommands implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
