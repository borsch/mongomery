package com.github.borsch.mongomery.matching;

import java.util.Set;

import net.minidev.json.JSONObject;

/**
 * Asserts collection state if every document of it DOES NOT contain any of placeholders:
 * $anyObject(), $anyString()
 */
public class StrictMatchStrategy implements AssertStrategy {

    @Override
    public void assertTheSame(
        final String collectionName, final Set<JSONObject> expected, final Set<JSONObject> actual, final Set<String> ignorePath
    ) {
        MatchingUtil.removeSameElement(actual, expected, ignorePath);

        if (!expected.isEmpty() || !actual.isEmpty()) {
            throw new AssertionError(String.format(
                "Collection %s doesn't match with expected.\nIgnore field(s): %s\nExpected don't match elements: %s\nActual don't match elements: %s",
                collectionName, ignorePath, expected, actual
            ));
        }
    }

}
