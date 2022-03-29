package com.github.borsch.mongomery.matching;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.borsch.mongomery.exceptions.ComparisonException;

import net.minidev.json.JSONObject;

public interface MatchingStrategy {

    /**
     * Match actual objects and expected objects to check where {@code collectionName} has expected state
     *
     * @param collectionName - name of collections that is currently checking
     * @param expectedObjects - specifies by user expected objects
     * @param actualObjects - actual objects stored in DB
     * @param ignorePath - fields and/or nested fields that should be ignored
     */
    void assertTheSame(
        final String collectionName, final List<JSONObject> expectedObjects, final List<JSONObject> actualObjects, final Set<String> ignorePath
    );

    static void assertCollectionsSize(
        final String collectionName, final Collection<JSONObject> expectedObjects, final Collection<JSONObject> actualObjects
    ) {
        if (expectedObjects.size() != actualObjects.size()) {
            throw new ComparisonException(
                "Collection %s has different elements size. Expected size: %s, actual size: %s",
                collectionName, expectedObjects.size(), actualObjects.size()
            );
        }
    }

}
