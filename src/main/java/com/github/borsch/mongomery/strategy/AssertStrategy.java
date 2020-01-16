package com.github.borsch.mongomery.strategy;

import java.util.Set;

import net.minidev.json.JSONObject;

public interface AssertStrategy {

    PatternMatchStrategy PATTERN_MATCH_STRATEGY = new PatternMatchStrategy();
    StrictMatchStrategy STRICT_MATCH_STRATEGY = new StrictMatchStrategy();

    void assertTheSame(String collectionName, Set<JSONObject> expected, Set<JSONObject> actual);
}
