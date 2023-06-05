package com.guihvicentini.keycloakconfigtool.facade;

import com.guihvicentini.keycloakconfigtool.filehandlers.ReadFileHandler;
import com.guihvicentini.keycloakconfigtool.filehandlers.WriteFileHandler;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import com.guihvicentini.keycloakconfigtool.services.export.RealmExportService;
import com.guihvicentini.keycloakconfigtool.services.RealmImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigCommandFacade {

    private final RealmExportService exportService;
    private final RealmImportService importService;
    private final WriteFileHandler writeFileHandler;
    private final ReadFileHandler readFileHandler;

    public ConfigCommandFacade(RealmExportService exportService, RealmImportService importService,
                               WriteFileHandler writeFileHandler, ReadFileHandler readFileHandler) {
        this.exportService = exportService;
        this.importService = importService;
        this.writeFileHandler = writeFileHandler;
        this.readFileHandler = readFileHandler;
    }

    public void exportConfig(String realmName, String outputFilePath){
        log.info("Getting configuration definition for realm: {}", realmName);
        RealmConfig realmConfig = exportService.getRealm(realmName);
        writeFileHandler.writeFile(outputFilePath, realmConfig);
    }

    public void applyConfig(String realmConfigFile, String variablesFile) {
        RealmConfig realmConfig = readFileHandler.readRealmConfig(realmConfigFile, variablesFile);
        log.info("Importing configuration definition for realm: {}", realmConfig.getRealm());
        importService.importConfig(realmConfig);
    }

}