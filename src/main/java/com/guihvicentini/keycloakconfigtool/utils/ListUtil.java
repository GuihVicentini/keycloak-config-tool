package com.guihvicentini.keycloakconfigtool.utils;

import com.guihvicentini.keycloakconfigtool.models.Config;

import java.util.*;
import java.util.stream.Collectors;

public class ListUtil {

    ListUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Filter the elements of the first collection that doesn't exist in the second collection
     * @param first collection containing the elements to be filtered
     * @param second the control collection
     * @return a collection containing elements that exist in the first collection but not in the second.
     * @param <T> any object
     */
    public static <T> Collection<T> getMissingElements(Collection<? extends Collection<T>> first,
                                                  Collection<? extends Collection<T>> second) {
        return first.stream()
                .flatMap(Collection::stream)
                .filter(item -> second.stream().noneMatch(list -> list.contains(item)))
                .collect(Collectors.toList());
    }

    /**
     * Filter the config elements of the first collection that doesn't exist in the second collection based on the
     * config {@link Config#identifier() identifier}
     * @param first any list of objects that implements the {@link Config} interface
     * @param second any list of objects that implements the {@link Config} interface
     * @return a list of the missing objects
     * @param <T> any object that implements the {@link Config} interface
     */
    public static <T extends Config> List<T> getMissingConfigElements(List<T> first, List<T> second) {
        List<Config> firstMigrated = first.stream().map(Config.class::cast).toList();
        List<Config> secondMigrated = second.stream().map(Config.class::cast).toList();
        return firstMigrated.stream()
                .filter(item -> secondMigrated.stream().noneMatch(config -> config.identifier().equals(item.identifier())))
                .map(config -> (T) config)
                .collect(Collectors.toList());
    }

    public static List<String> getMissingElements(List<String> first, List<String> second) {
        List<String> missing = new ArrayList<>(first);
        missing.removeAll(second);
        return missing;
    }

    /**
     * Filter the config elements of the first list that exist in the second list based on the
     * config {@link Config#identifier() identifier} but are not equal
     * @param first any list of objects that implements the {@link Config} interface
     * @param second any list of objects that implements the {@link Config} interface
     * @return a list with non-equal objects that have the same identifier
     * @param <T> any object that implements the {@link Config} interface
     */
    public static <T extends Config> List<T> getNonEqualConfigsWithSameIdentifier(List<T> first, List<T> second) {
        return first.stream().filter(config -> nonEqualWithSameIdentifier(config, second)).collect(Collectors.toList());
    }

    private static <T extends Config> boolean nonEqualWithSameIdentifier(T config, List<T> configList) {
        var match = configList.stream().filter(c -> c.identifier().equals(config.identifier())).findFirst();
        return match.filter(t -> !t.equals(config)).isPresent();
    }


    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("The list is null or empty.");
        }
        return list.get(new Random().nextInt(list.size()));
    }

    /**
     * Returns an empty list if the given list is null. Otherwise, return the list itself.
     * @param list null or a list of any objects
     * @return the same list if not empty. Otherwise, an {@link ArrayList} list.
     * @param <T> any object
     */
    public static <T> List<T> nullListToEmptyList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

}
