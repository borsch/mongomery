package com.github.borsch.mongomery.matching;

import static com.github.borsch.mongomery.matching.MatchingStrategy.assertCollectionsSize;
import static com.github.borsch.mongomery.matching.PatternMatchUtils.applyPropsAndGetResultObj;
import static com.github.borsch.mongomery.matching.PatternMatchUtils.findPatternPropertiesPaths;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.github.borsch.mongomery.exceptions.ComparisonException;

import lombok.extern.java.Log;
import net.minidev.json.JSONObject;

@Log
public class OrderedMatchStrategy implements MatchingStrategy {

    @Override
    public void assertTheSame(
        final String collectionName, final List<JSONObject> expectedObjects, final List<JSONObject> actualObjects, final Set<String> ignorePath
    ) {
        assertCollectionsSize(collectionName, expectedObjects, actualObjects);

        for (int i = 0; i < actualObjects.size(); ++i) {
            final JSONObject actualObject = actualObjects.get(i);
            final JSONObject expectedObject = expectedObjects.get(i);

            log.log(Level.INFO, "Compare actual object: {0}\n with expected object: {1}", new Object[] { actualObject, expectedObject });

            final JSONObject object = applyPropsAndGetResultObj(actualObject, findPatternPropertiesPaths(expectedObject));
            log.log(Level.INFO, "Actual object after applying replacing {0}", object);
            if (object == null || !MatchingUtil.isMatch(object, expectedObject, ignorePath)) {
                throw new ComparisonException(
                    "Collection [%s] has different objects at index [%s].\nIgnore path: %s\nActual object: %s\nExpected object: %s",
                    collectionName, i, ignorePath, actualObject, expectedObject
                );
            }
        }
    }
}
