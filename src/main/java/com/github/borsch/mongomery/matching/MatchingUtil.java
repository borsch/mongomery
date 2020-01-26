package com.github.borsch.mongomery.matching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.JSONObject;

class MatchingUtil {

    private static final Logger log = LoggerFactory.getLogger(MatchingUtil.class);

    static void removeSameElement(final List<JSONObject> actualObjects, final List<JSONObject> expectedObjects, final Set<String> ignorePath) {
        final Iterator<JSONObject> actualObjectsIterator = actualObjects.iterator();
        while (actualObjectsIterator.hasNext()) {
            final JSONObject actual = actualObjectsIterator.next();
            final Iterator<JSONObject> expectedObjectsIterator = expectedObjects.iterator();

            while (expectedObjectsIterator.hasNext()) {
                final JSONObject expected = expectedObjectsIterator.next();

                if (isMatch(actual, expected, ignorePath)) {
                    actualObjectsIterator.remove();
                    expectedObjectsIterator.remove();
                    break;
                }
            }
        }
    }

    static boolean isMatch(final JSONObject actual, final JSONObject expected, final Set<String> ignorePath) {
        return isMatch(actual, expected, ignorePath, "");
    }

    private static boolean isMatch(final JSONObject actual, final JSONObject expected, final Set<String> ignorePath, final String currentPath) {
        if (actual.equals(expected)) {
            log.trace("Actual and expected are equals without ignoring fields.\nActual: {}\nExpected: {}", actual, expected);
            return true;
        }

        if (ignorePath.isEmpty()) {
            return false;
        }

        final Set<String> actualKeys = cleanKeySet(actual.keySet(), ignorePath, currentPath);
        final Set<String> expectedKeys = cleanKeySet(expected.keySet(), ignorePath, currentPath);

        if (!actualKeys.equals(expectedKeys)) {
            log.error(
                "Actual keys and expected key are different.\nActual: {}\nExpected: {}\nIgnore path: {}\nUnder path: {}",
                actualKeys, expectedKeys, ignorePath, currentPath
            );
            return false;
        }

        for (final String key : actualKeys) {
            final Object actualValue = actual.get(key);
            final Object expectedValue = expected.get(key);
            final String nextPath = currentPath.isEmpty() ? key : currentPath + "." + key;

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

    private static Set<String> cleanKeySet(final Set<String> keySey, final Set<String> ignorePath, final String currentPath) {
        final Set<String> result = new HashSet<>(keySey);
        final Iterator<String> iterator = result.iterator();

        while (iterator.hasNext()) {
            final String key = iterator.next();

            for (final String ignorePathItem : ignorePath) {
                if (key.matches(ignorePathItem) || (currentPath + "." + key).matches(ignorePathItem)) {
                    iterator.remove();
                    break;
                }
            }
        }

        return result;
    }

}
