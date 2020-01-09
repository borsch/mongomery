package com.github.borsch.mongomery.strategy;

import java.util.Set;

import net.minidev.json.JSONObject;

public abstract class AssertStrategy {

    public final static PatternMatchStrategy PATTERN_MATCH_STRATEGY = new PatternMatchStrategy();
    public final static StrictMatchStrategy STRICT_MATCH_STRATEGY = new StrictMatchStrategy();

    public abstract void assertTheSame(Set<JSONObject> expected, Set<JSONObject> actual);
}
