package com.github.borsch.mongomery;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

class DBState {

    private final Map<String, List<JSONObject>> collectionToDocuments = new HashMap<>();

    DBState(final JSONObject object) {
        for (final Map.Entry<String, Object> collections : object.entrySet()) {
            final JSONArray documents = (JSONArray) collections.getValue();
            collectionToDocuments.put(collections.getKey(), toJavaList(documents));
        }
    }

    SortedSet<String> getCollectionNames() {
        return new TreeSet<>(collectionToDocuments.keySet());
    }

    List<JSONObject> getDocuments(final String collectionName) {
        return collectionToDocuments.get(collectionName);
    }

    private static List<JSONObject> toJavaList(final JSONArray documents) {
        final List<JSONObject> objects = new LinkedList<>();

        for (final Object doc : documents) {
            objects.add((JSONObject) doc);
        }

        return objects;
    }
}
