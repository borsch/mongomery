package com.github.borsch.mongomery.matching;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.JSONObject;

class MatchingUtil {

    private static final Logger log = LoggerFactory.getLogger(MatchingUtil.class);

    static boolean isMatch(final JSONObject actual, final JSONObject expected, final Set<String> ignorePath) {
        return isMatch(actual, expected, ignorePath, "");
    }

    private static boolean isMatch(final JSONObject actual, final JSONObject expected, final Set<String> ignorePath, final String currentPath) {
        if (actual.equals(expected)) {
            return true;
        }

        if (ignorePath.isEmpty()) {
            return false;
        }

        final Set<String> actualKeys = removeIgnoredPath(actual.keySet(), ignorePath, currentPath);
        final Set<String> expectedKeys = removeIgnoredPath(expected.keySet(), ignorePath, currentPath);

        if (!actualKeys.equals(expectedKeys)) {
            log.info(
                "Actual keys and expected key are different.\nActual: {}\nExpected: {}\nIgnore path: {}\nUnder path: {}",
                actualKeys, expectedKeys, ignorePath, currentPath
            );
            return false;
        }

        for (final String key : actualKeys) {
            final Object actualValue = actual.get(key);
            final Object expectedValue = expected.get(key);
            final String nextPath = currentPath.isEmpty() ? key : currentPath + "." + key;

            if (actualValue == expectedValue) {
                continue;
            }
            if (actualValue.getClass() != expectedValue.getClass()) {
                return false;
            } else if (actualValue instanceof JSONObject) {
                if (!isMatch((JSONObject) actualValue, (JSONObject) expectedValue, ignorePath, nextPath)) {
                    return false;
                }
            } else if (!Objects.equals(actualValue, expectedValue)) {
                return false;
            }
        }

        return true;
    }

    static KeyValue splitByLast$(final String s) {
        final int $ = s.lastIndexOf("$");
        return new KeyValue(s.substring(0, $), s.substring($));
    }

    private static Set<String> removeIgnoredPath(final Set<String> keySey, final Set<String> ignorePath, final String currentPath) {
        return keySey.stream()
            .filter(keyIsNotIgnored(ignorePath, currentPath))
            .collect(Collectors.toSet());
    }

    private static Predicate<String> keyIsNotIgnored(final Set<String> ignorePath, final String currentPath) {
        return key -> !ignorePath.contains(key) && !ignorePath.contains(currentPath + "." + key);
    }

}
