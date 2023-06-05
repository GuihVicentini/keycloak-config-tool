package com.guihvicentini.keycloakconfigtool.filehandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guihvicentini.keycloakconfigtool.exceptions.WriteConfigFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class WriteFileHandler {

    private final ObjectMapper mapper;

    public WriteFileHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void writeFile(@NonNull String fileName, Object jsonFile) {
        JsonNode node = mapper.valueToTree(jsonFile);
        if (fileName.equals("")){
            log.info(node.toPrettyString());
        } else {
            try {
                Files.writeString(Path.of(fileName), node.toPrettyString());
            } catch (IOException e) {
                throw new WriteConfigFileException(fileName, e);
            }
        }
    }
}
