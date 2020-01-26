package com.github.borsch.mongomery.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minidev.json.JSONObject;

public class PatternMatchStrategy {

    public static void assertTheSame(
        final String collectionName, final List<JSONObject> expectedObjects, final List<JSONObject> actualObjects, final Set<String> ignorePath
    ) {
        assertThat(expectedObjects)
            .withFailMessage(
                "Collection %s has different elements size. Expected size: %s, actual size: %s.\nExpected elements: %s\nActual elements: %s",
                collectionName, expectedObjects.size(), actualObjects.size(), expectedObjects, actualObjects
            )
            .hasSameSizeAs(actualObjects);

        final Map<JSONObject, Set<String>> patternMatchExpectedObjects = new HashMap<>();
        final List<JSONObject> strictMatchExpectedObjects = new LinkedList<>();

        for (final JSONObject object : expectedObjects) {
            final Set<String> patternPropertiesPaths = PatternMatchUtils.findPatternPropertiesPaths(object);

            if (patternPropertiesPaths == null) {
                strictMatchExpectedObjects.add(object);
            } else {
                patternMatchExpectedObjects.put(object, patternPropertiesPaths);
            }
        }

        final List<JSONObject> patternMatchCandidates = tryToMatchStrictly(
            collectionName, strictMatchExpectedObjects, patternMatchExpectedObjects.size(), actualObjects, ignorePath
        );
        tryToMatchOverPattern(collectionName, patternMatchExpectedObjects, patternMatchCandidates, ignorePath);
    }

    private static List<JSONObject> tryToMatchStrictly(
        final String collectionName, final List<JSONObject> strictMatchExpectedObjects, final int patternMatchExpectedObjectsSize,
        final List<JSONObject> actualObjects, final Set<String> ignorePath
    ) {
        final List<JSONObject> actualObjectsCopy = new LinkedList<>(actualObjects);
        MatchingUtil.removeSameElement(actualObjectsCopy, strictMatchExpectedObjects, ignorePath);

        if (actualObjectsCopy.size() != patternMatchExpectedObjectsSize) {
            strictMatchExpectedObjects.removeAll(actualObjects);
            throw new AssertionError(String.format(
                "Can't find pattern match for %s element(s) in collection %s.\nUnmatched objects: %s",
                strictMatchExpectedObjects.size(), collectionName, strictMatchExpectedObjects
            ));
        }

        return actualObjectsCopy;
    }

    private static void tryToMatchOverPattern(
        final String collectionName, final Map<JSONObject, Set<String>> expectedObjects, final List<JSONObject> actualObjects, final Set<String> ignorePath
    ) {
        final Map<JSONObject, Set<String>> patternMatchExpectedObjects = new HashMap<>(expectedObjects);
        final List<JSONObject> unmatchedActualObjects = new LinkedList<>();

        for (final JSONObject actualObject : actualObjects) {
            boolean isMatched = false;
            final Iterator<Map.Entry<JSONObject, Set<String>>> iterator
                    = patternMatchExpectedObjects.entrySet().iterator();

            while (iterator.hasNext() && !isMatched) {
                final Map.Entry<JSONObject, Set<String>> patternMatchObjAndPropsPaths = iterator.next();
                final JSONObject object = PatternMatchUtils.applyPropsAndGetResultObj(
                    actualObject, patternMatchObjAndPropsPaths.getValue()
                );

                if (object != null && MatchingUtil.isMatch(object, patternMatchObjAndPropsPaths.getKey(), ignorePath)) {
                    iterator.remove();
                    isMatched = true;
                }
            }

            if (!isMatched) {
                unmatchedActualObjects.add(actualObject);
            }
        }

        if (!unmatchedActualObjects.isEmpty() || !patternMatchExpectedObjects.isEmpty()) {
            throw new AssertionError(String.format(
                "Collection %s doesn't match with expected.\nIgnore field(s): %s\nExpected don't match elements: %s\nActual don't match elements: %s",
                collectionName, ignorePath, unmatchedActualObjects, patternMatchExpectedObjects.keySet()
            ));
        }
    }
}
