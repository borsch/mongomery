package com.github.borsch.mongomery.strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.github.borsch.mongomery.PatternMatchUtils;

import net.minidev.json.JSONObject;

public class PatternMatchStrategy implements AssertStrategy {

    @Override
    public void assertTheSame(final Set<JSONObject> expectedObjects, final Set<JSONObject> actualObjects) {
        Assert.assertEquals("Size of collection in db is different from described in json file",
                expectedObjects.size(), actualObjects.size());

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
            throw new AssertionError("Can't find strict match for " + strictMatchExpectedObjects.size()
                    + " EXPECTED object(s): " + strictMatchExpectedObjects);
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

        if (!unmatchedActualObjects.isEmpty()) {
            throw new AssertionError("Can't find pattern match for "
                    + unmatchedActualObjects.size() + " ACTUAL object(s): " + unmatchedActualObjects);
        }
    }
}
