package com.github.borsch.mongomery.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.borsch.mongomery.PatternMatchUtils;

import net.minidev.json.JSONObject;

public class PatternMatchStrategy implements AssertStrategy {

    @Override
    public void assertTheSame(final String collectionName, final Set<JSONObject> expectedObjects, final Set<JSONObject> actualObjects) {
        assertThat(expectedObjects)
            .withFailMessage(
                "Collection %s has different elements size. Expected size: %s, actual size: %s.\nExpected elements: %s\nActual elements: %s",
                collectionName, expectedObjects.size(), actualObjects.size(), expectedObjects, actualObjects
            )
            .hasSameSizeAs(actualObjects);

        final Map<JSONObject, Set<String>> patternMatchExpectedObjects = new HashMap<>();
        final Set<JSONObject> strictMatchExpectedObjects = new HashSet<>();

        for (final JSONObject object : expectedObjects) {
            final Set<String> patternPropertiesPaths = PatternMatchUtils.findPatternPropertiesPaths(object);

            if (patternPropertiesPaths == null) {
                strictMatchExpectedObjects.add(object);
            } else {
                patternMatchExpectedObjects.put(object, patternPropertiesPaths);
            }
        }

        final Set<JSONObject> patternMatchCandidates = tryToMatchStrictly(strictMatchExpectedObjects,
                patternMatchExpectedObjects.size(), actualObjects);
        tryToMatchOverPattern(patternMatchExpectedObjects, patternMatchCandidates);
    }

    private Set<JSONObject> tryToMatchStrictly(final Set<JSONObject> strictMatchExpectedObjects,
                                               final int patternMatchExpectedObjectsSize, final Set<JSONObject> actualObjects) {
        final Set<JSONObject> actualObjectsCopy = new HashSet<>(actualObjects);
        actualObjectsCopy.removeAll(strictMatchExpectedObjects);

        if (actualObjectsCopy.size() != patternMatchExpectedObjectsSize) {
            strictMatchExpectedObjects.removeAll(actualObjects);
            throw new AssertionError(String.format(
                "Can't find pattern match for %s element(s).\nUnmatched objects: %s", strictMatchExpectedObjects.size(), strictMatchExpectedObjects
            ));
        }

        return actualObjectsCopy;
    }

    private void tryToMatchOverPattern(final Map<JSONObject, Set<String>> expectedObjects,
                                       final Set<JSONObject> actualObjects) {
        final Map<JSONObject, Set<String>> patternMatchExpectedObjects = new HashMap<>(expectedObjects);
        final Set<JSONObject> unmatchedActualObjects = new HashSet<>();

        for (final JSONObject actualObject : actualObjects) {
            boolean isMatched = false;
            final Iterator<Map.Entry<JSONObject, Set<String>>> iterator
                    = patternMatchExpectedObjects.entrySet().iterator();

            while (iterator.hasNext() && !isMatched) {
                final Map.Entry<JSONObject, Set<String>> patternMatchObjAndPropsPaths = iterator.next();
                final JSONObject object = PatternMatchUtils.applyPropsAndGetResultObj(actualObject,
                        patternMatchObjAndPropsPaths.getValue());

                if (object != null && object.equals(patternMatchObjAndPropsPaths.getKey())) {
                    iterator.remove();
                    isMatched = true;
                }
            }

            if (!isMatched) {
                unmatchedActualObjects.add(actualObject);
            }
        }

        assertThat(unmatchedActualObjects)
            .withFailMessage("Can't find pattern match for %s element(s).\nUnmatched objects: %s", unmatchedActualObjects.size(), unmatchedActualObjects)
            .isEmpty();
    }
}
