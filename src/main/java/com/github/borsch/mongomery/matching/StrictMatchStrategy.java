package com.github.borsch.mongomery.matching;

import static org.assertj.core.api.Assertions.assertThat;

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
        MatchingUtil.match(actual, expected, ignorePath);

        assertThat(expected)
            .withFailMessage(
                "Collection %s doesn't match with expected.\nExpected elements: %s\nActual elements: %s",
                collectionName, expected, actual
            )
            .isEqualTo(actual);
    }

}
