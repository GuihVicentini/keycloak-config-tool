package com.guihvicentini.keycloakconfigtool.utils;

import com.guihvicentini.keycloakconfigtool.models.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class MapUtil {
    MapUtil() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Filter the missing elements of the first map that doesn't exist in the second map or the values in the first map
     * are not equal the values in the second map.
     * @param first map containing the elements to be filtered
     * @param second the control map
     * @return a map containing the key value pair that exists in the first map but not in the second
     * @param <T> any object
     */
    public static <T extends Config> Map<String, List<T>> getMissingElements(Map<String, List<T>> first, Map<String, List<T>> second) {
        return first.entrySet().stream().flatMap(entry -> {
            if(second.containsKey(entry.getKey())) {
                List<Config> firstMigrated = entry.getValue().stream().map(Config.class::cast).toList();
                List<Config> secondMigrated = second.get(entry.getKey()).stream().map(Config.class::cast).toList();
                List<T> missing = firstMigrated.stream()
                        .filter(item -> secondMigrated.stream().noneMatch(config -> config.identifier().equals(item.identifier())))
                        .map(config -> (T) config).toList();
                if(!missing.isEmpty()) {
                    return Stream.of(new AbstractMap.SimpleEntry<>(entry.getKey(), missing));
                }
            }
            if(!second.containsKey(entry.getKey())) {
                return Stream.of(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
            return Stream.empty();
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <T> void sortMapByKey(Map<String, T> map) {
        Map<String, T> sortedMap = map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        map.clear();
        map.putAll(sortedMap);
    }

    /**
     * Rename each key of the @map based on the keyTransformer function.
     * @param map to have the keys renamed.
     * @param keyTransformer the function to manipulate the key string value.
     * @param <T> any object.
     */
    public static <T> void renameKeys(Map<String, T> map, Function<String, String> keyTransformer) {
        Set<String> oldKeys = new HashSet<>(map.keySet());
        oldKeys.forEach(key -> {
            var newKey = keyTransformer.apply(key);
            MapUtil.renameKey(map, key, newKey);
        });
    }

    /**
     * Rename the keys of the @map with the corresponding value of the @keyTransformer map.
     * Only renames if the @keyTransformer map contains the key of the @map.
     * @param map to have the keys renamed.
     * @param keyTransformer a map containing the new key string values.
     * @param <T> any object.
     */
    public static <T> void renameKeys(Map<String, T> map, Map<String, String> keyTransformer) {
        Set<String> oldKeys = new HashSet<>();
        oldKeys.addAll(map.keySet());
        oldKeys.forEach(key -> {
            if (keyTransformer.containsKey(key)) {
                var newKey = keyTransformer.get(key);
                MapUtil.renameKey(map, key, newKey);
            }
        });
    }

    private static <T> void renameKey(Map<String, T> map, String oldKey, String newKey) {
        if (map.containsKey(oldKey)) {
            T value = map.remove(oldKey);
            map.put(newKey, value);
        }
    }

    public static <K, V> void removeMatchingEntries(Map<K, V> firstMap, Map<K, V> secondMap) {
        firstMap.entrySet().removeIf(entry -> secondMap.containsKey(entry.getKey()) &&
                Objects.equals(entry.getValue(), secondMap.get(entry.getKey())));
    }


    public static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet().stream()
                .flatMap(MapUtil::flatten)
                .collect(LinkedHashMap::new, (m, e) -> m.put("/"+ e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {

        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            return ((Map<?, ?>) entry.getValue()).entrySet().stream()
                    .flatMap(e -> flatten(new AbstractMap.SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
                    .flatMap(MapUtil::flatten);
        }

        return Stream.of(entry);
    }
}
