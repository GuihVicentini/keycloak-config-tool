package com.guihvicentini.keycloakconfigtool.utils;

import java.util.Arrays;
import java.util.function.Function;

public class StringUtil {

    StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Transform a string that contains dots and returns the last word of the doted string with the first char
     * to lowercase.
     * E.g. org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy -> clientRegistrationPolicy
     */
    public static Function<String, String> lastWordSplitByDotsToLower = input -> {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string is empty");
        }
        String transformedString =  Arrays.stream(input.split("\\."))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalArgumentException("Input string has no word separated by dots"));
        if(!transformedString.isEmpty()) {
            transformedString = Character.toLowerCase(transformedString.charAt(0)) + transformedString.substring(1);
        }
        return transformedString;
    };

    public static String removeSuffix(String string, String suffix) {
        return string.substring(0, string.length() - suffix.length());
    }

}
