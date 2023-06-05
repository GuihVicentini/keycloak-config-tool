package com.guihvicentini.keycloakconfigtool.commands;

import com.guihvicentini.keycloakconfigtool.facade.ConfigCommandFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@CommandLine.Command(name = "config",
        mixinStandardHelpOptions = true,
        description = "Configuration automation commands",
        subcommands = {GetConfigCommand.class}
)
@Component
public class ConfigCommands {
}

@CommandLine.Command(name = "get",
        mixinStandardHelpOptions = true,
        description = "get config of a realm"
)
@Component
@Slf4j
class GetConfigCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "realm name")
    private String realmName;

    @CommandLine.Option(names = {"-o", "--output-file"}, description = "output file path", defaultValue = "")
    private String outputFileName;

    private final ConfigCommandFacade facade;

    GetConfigCommand(ConfigCommandFacade facade){
        this.facade = facade;
    }

    @Override
    public void run() {
        facade.exportConfig(realmName, outputFileName);
    }

}

@CommandLine.Command(name = "apply",
        mixinStandardHelpOptions = true,
        description = "apply config of a realm"
)
@Component
@Slf4j
class ApplyConfigCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "realm config file path and name")
    private String realmConfigFile;

    @CommandLine.Option(names = {"-v", "--vars"}, description = "environment variables file", defaultValue = "")
    private String variablesFile;

    private final ConfigCommandFacade facade;

    ApplyConfigCommand(ConfigCommandFacade facade){
        this.facade = facade;
    }

    @Override
    public void run() {
        facade.applyConfig(realmConfigFile, variablesFile);
    }


}

