package com.github.borsch.mongomery.matching;

import static com.github.borsch.mongomery.matching.MatchingStrategy.assertCollectionsSize;
import static com.github.borsch.mongomery.matching.PatternMatchUtils.applyPropsAndGetResultObj;
import static com.github.borsch.mongomery.matching.PatternMatchUtils.findPatternPropertiesPaths;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.borsch.mongomery.exceptions.ComparisonException;

import net.minidev.json.JSONObject;

public class OrderedMatchStrategy implements MatchingStrategy {

    private static final Logger log = LoggerFactory.getLogger(MatchingUtil.class);

    @Override
    public void assertTheSame(
        final String collectionName, final List<JSONObject> expectedObjects, final List<JSONObject> actualObjects, final Set<String> ignorePath
    ) {
        assertCollectionsSize(collectionName, expectedObjects, actualObjects);

        for (int i = 0; i < actualObjects.size(); ++i) {
            final JSONObject actualObject = actualObjects.get(i);
            final JSONObject expectedObject = expectedObjects.get(i);

            log.info("Compare actual object: {}\n with expected object: {}", actualObject, expectedObject);

            final JSONObject object = applyPropsAndGetResultObj(actualObject, findPatternPropertiesPaths(expectedObject));
            log.info("Actual object after applying replacing {}", object);
            if (object == null || !MatchingUtil.isMatch(object, expectedObject, ignorePath)) {
                throw new ComparisonException(
                    "Collection [%s] has different objects at index [%s].\nIgnore path: %s\nActual object: %s\nExpected object: %s",
                    collectionName, i, ignorePath, actualObject, expectedObject
                );
            }
        }
    }
}
