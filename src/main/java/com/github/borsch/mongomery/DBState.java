package com.github.borsch.mongomery;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.github.borsch.mongomery.strategy.AssertStrategy;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class DBState {

    private final Map<String, Set<JSONObject>> collectionToDocuments = new HashMap<>();
    private final Map<String, AssertStrategy> collectionToMatchStrategy = new HashMap<>();

    public DBState(final JSONObject object) {
        for (final Map.Entry<String, Object> collections : object.entrySet()) {
            collectionToMatchStrategy.put(collections.getKey(), AssertStrategy.STRICT_MATCH_STRATEGY);

            for (final Pattern pattern : Placeholders.getContainPatterns()) {
                final String val = collections.getValue().toString();

                if (pattern.matcher(val).find()) {
                    collectionToMatchStrategy.put(collections.getKey(), AssertStrategy.PATTERN_MATCH_STRATEGY);
                    break;
                }
            }

            final JSONArray documents = (JSONArray) collections.getValue();
            collectionToDocuments.put(collections.getKey(), toJavaSet(documents));
        }
    }

    public boolean containsCollection(final String name) {
        return collectionToDocuments.containsKey(name);
    }

    public int getCollectionNumber() {
        return collectionToDocuments.size();
    }

    public SortedSet<String> getCollectionNames() {
        return new TreeSet<>(collectionToDocuments.keySet());
    }

    public Set<JSONObject> getDocuments(final String collectionName) {
        return collectionToDocuments.get(collectionName);
    }

    public AssertStrategy getMatchStrategy(final String collectionName) {
        return collectionToMatchStrategy.get(collectionName);
    }

    private Set<JSONObject> toJavaSet(final JSONArray documents) {
        final Set<JSONObject> objects = new HashSet<JSONObject>();

        for (final Object doc : documents) {
            objects.add((JSONObject) doc);
        }

        return objects;
    }
}
