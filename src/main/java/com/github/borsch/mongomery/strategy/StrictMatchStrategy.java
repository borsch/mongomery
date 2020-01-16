package com.github.borsch.mongomery.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import net.minidev.json.JSONObject;

/**
 * Asserts collection state if every document of it DOES NOT contain any of placeholders:
 * $anyObject(), $anyString()
 */
public class StrictMatchStrategy implements AssertStrategy {

    @Override
    public void assertTheSame(String collectionName, final Set<JSONObject> expected, final Set<JSONObject> actual) {
        assertThat(expected)
            .withFailMessage(
                "Collection %s doesn't match with expected.\nExpected elements: %s\nActual elements: %s",
                collectionName, expected, actual
            )
            .isEqualTo(actual);
    }

}
