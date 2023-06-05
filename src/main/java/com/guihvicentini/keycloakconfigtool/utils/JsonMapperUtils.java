package com.guihvicentini.keycloakconfigtool.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.guihvicentini.keycloakconfigtool.exceptions.JsonParsingException;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class JsonMapperUtils {

    JsonMapperUtils() {
        throw new IllegalStateException("Utility class");
    }
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    public static <T> String objectToJsonString(T obj) {
        return objectToJsonNode(obj).toString();
    }

    public static <T> String objectToJsonPrettyString(T obj) {
        return objectToJsonNode(obj).toPrettyString();
    }

    public static <T> JsonNode objectToJsonNode(T obj) {
        return mapper.valueToTree(obj);
    }

    public static void logDifferences(Logger log, JsonNode expected, JsonNode actual, Level level) {
        TypeReference<HashMap<String, Object>> type = new TypeReference<>() {};

        Map<String, Object> expectedMap;
        Map<String, Object> actualMap;

        try {
           expectedMap = mapper.readValue(expected.traverse(), type);
           actualMap = mapper.readValue(actual.traverse(), type);
        } catch (IOException e) {
            throw new JsonParsingException(e);
        }

        expectedMap = MapUtil.flatten(expectedMap);
        actualMap = MapUtil.flatten(actualMap);

        var differences = Maps.difference(expectedMap, actualMap);

        if(!differences.entriesOnlyOnLeft().isEmpty()) {
            log.atLevel(level).log("Items found in the expected but not in the actual config:");
            differences.entriesOnlyOnLeft().forEach((key, value) -> log.atLevel(level).log(key+": "+value));
        }

        if(!differences.entriesOnlyOnRight().isEmpty()) {
            log.atLevel(level).log("Items found in the actual but not in the expected config:");
            differences.entriesOnlyOnRight().forEach((key, value) -> log.atLevel(level).log(key+": "+value));
        }

        if(!differences.entriesDiffering().isEmpty()) {
            log.atLevel(level).log("Items found different in the actual and expected config:");
            differences.entriesDiffering().forEach((key, value) -> log.atLevel(level).log(key+": "+value));
        }
    }
}
