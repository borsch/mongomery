package com.github.borsch.mongomery.strategy;

import java.util.Set;

import org.junit.Assert;

import net.minidev.json.JSONObject;

/**
 * Asserts collection state if every document of it DOES NOT contain any of placeholders:
 * $anyObject(), $anyString()
 */
public class StrictMatchStrategy implements AssertStrategy {

    @Override
    public void assertTheSame(final Set<JSONObject> expected, final Set<JSONObject> actual) {
        Assert.assertEquals("Documents in db collection are different from described in json file!", expected, actual);
    }

}
