package com.github.borsch.mongomery.matching;

import static com.github.borsch.mongomery.matching.MatchingStrategy.assertCollectionsSize;
import static com.github.borsch.mongomery.matching.PatternMatchUtils.applyPropsAndGetResultObj;
import static com.github.borsch.mongomery.matching.PatternMatchUtils.findPatternPropertiesPaths;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.borsch.mongomery.exceptions.ComparisonException;

import net.minidev.json.JSONObject;

public class UnorderedMatchStrategy implements MatchingStrategy {

    @Override
    public void assertTheSame(
        final String collectionName, final List<JSONObject> expectedObjects, final List<JSONObject> actualObjects, final Set<String> ignorePath
    ) {
        assertCollectionsSize(collectionName, expectedObjects, actualObjects);

        final Set<JSONObject> patternMatchExpectedObjects = new HashSet<>(expectedObjects);
        final Set<JSONObject> unmatchedActualObjects = new HashSet<>();

        for (final JSONObject actualObject : actualObjects) {
            boolean isMatched = false;
            final Iterator<JSONObject> iterator = patternMatchExpectedObjects.iterator();

            while (iterator.hasNext() && !isMatched) {
                final JSONObject patternMatchObjAndPropsPaths = iterator.next();
                final JSONObject object = applyPropsAndGetResultObj(actualObject, findPatternPropertiesPaths(patternMatchObjAndPropsPaths));

                if (object != null && MatchingUtil.isMatch(object, patternMatchObjAndPropsPaths, ignorePath)) {
                    iterator.remove();
                    isMatched = true;
                }
            }

            if (!isMatched) {
                unmatchedActualObjects.add(actualObject);
            }
        }

        if (!unmatchedActualObjects.isEmpty() || !patternMatchExpectedObjects.isEmpty()) {
            throw new ComparisonException(
                "Collection %s doesn't match with expected.\nIgnore field(s): %s\nExpected don't match elements: %s\nActual don't match elements: %s",
                collectionName, ignorePath, patternMatchExpectedObjects, unmatchedActualObjects
            );
        }
    }
}
