package com.guihvicentini.keycloakconfigtool.filehandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guihvicentini.keycloakconfigtool.exceptions.JsonParsingException;
import com.guihvicentini.keycloakconfigtool.exceptions.ReadConfigFileException;
import com.guihvicentini.keycloakconfigtool.models.RealmConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class ReadFileHandler {

    private final ObjectMapper mapper;

    public ReadFileHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public RealmConfig readRealmConfig(String realmConfigFile, String variablesFile) {
        try {
            JsonNode treeNode = mapper.readTree(Files.readAllBytes(Paths.get(realmConfigFile)));

            if(!variablesFile.equals("")) {
                // TODO replace placeholders
            }

            return mapper.treeToValue(treeNode, RealmConfig.class);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(realmConfigFile, e);
        } catch (IOException e) {
            throw new ReadConfigFileException(realmConfigFile, e);
        }
    }

}
