package com.github.borsch.mongomery;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

class DBState {

    private final Map<String, Set<JSONObject>> collectionToDocuments = new HashMap<>();

    DBState(final JSONObject object) {
        for (final Map.Entry<String, Object> collections : object.entrySet()) {
            final JSONArray documents = (JSONArray) collections.getValue();
            collectionToDocuments.put(collections.getKey(), toJavaSet(documents));
        }
    }

    SortedSet<String> getCollectionNames() {
        return new TreeSet<>(collectionToDocuments.keySet());
    }

    Set<JSONObject> getDocuments(final String collectionName) {
        return collectionToDocuments.get(collectionName);
    }

    private Set<JSONObject> toJavaSet(final JSONArray documents) {
        final Set<JSONObject> objects = new HashSet<>();

        for (final Object doc : documents) {
            objects.add((JSONObject) doc);
        }

        return objects;
    }
}
