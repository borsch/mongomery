package com.github.borsch.mongomery.matching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;

class MatchingUtil {

    private static final Logger log = java.util.logging.Logger.getLogger(MatchingUtil.class.getCanonicalName());

    static void removeSameElement(final Set<JSONObject> actualObjects, final Set<JSONObject> expectedObjects, final Set<String> ignorePath) {
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
            return true;
        }

        if (ignorePath.isEmpty()) {
            return false;
        }

        final Set<String> actualKeys = cleanKeySet(actual.keySet(), ignorePath, currentPath);
        final Set<String> expectedKeys = cleanKeySet(expected.keySet(), ignorePath, currentPath);

        if (!actualKeys.equals(expectedKeys)) {
            log.log(
                Level.FINE,
                "Actual keys and expected key are different.\nActual: {1}\nExpected: {2}\nIgnore path: {2}\nUnder path: {2}",
                new Object[] { actualKeys, expectedKeys, ignorePath, currentPath }
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

    static KeyValue splitByLast$(final String s) {
        final int $ = s.lastIndexOf("$");
        return KeyValue.of(s.substring(0, $), s.substring($));
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
