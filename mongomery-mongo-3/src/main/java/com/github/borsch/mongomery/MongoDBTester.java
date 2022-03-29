package com.github.borsch.mongomery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.github.borsch.mongomery.type.MatchingStrategyType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * Core instance of a library.
 */
public class MongoDBTester extends AbstractMongoDBTester {

    private static final List<String> SYSTEM_COLLECTIONS_NAMES = Arrays.asList("system.namespaces", "system.indexes", "system.profile", "system.js");

    private final MongoDatabase db;

    /**
     * @param db mongodb instance.
     */
    public MongoDBTester(final MongoDatabase db, final MatchingStrategyType strategyType) {
        this(db, strategyType, "", "");
    }

    /**
     * @param db                  mongodb instance.
     * @param strategyType        strategy to match objects in collections
     * @param expectedFilesRoot   expected files root. File path passed to setDBState() will be relative to this root.
     * @param predefinedFilesRoot predefined files root.
     */
    public MongoDBTester(final MongoDatabase db, final MatchingStrategyType strategyType, final String expectedFilesRoot, final String predefinedFilesRoot) {
        super(strategyType, expectedFilesRoot, predefinedFilesRoot);
        this.db = db;
    }

    /**
     *
     */
    @Override
    public void dropDataBase() {
        for (String collectionName : db.listCollectionNames()) {
            db.getCollection(collectionName).drop();
        }
    }

    @Override
    protected void populateDBCollection(final String collectionName, final JSONArray docs) {
        final MongoCollection<Document> dbCollection = db.getCollection(collectionName, Document.class);

        for (final Object document : docs) {
            final JSONObject jsonObject = (JSONObject) document;
            InsertUtils.replaceInsertPlaceholders(jsonObject);
            dbCollection.insertOne(new Document(jsonObject));
        }
    }

    @Override
    protected List<String> getDbCollections() {
        return StreamSupport.stream(db.listCollectionNames().spliterator(), false)
            .filter(collection -> !SYSTEM_COLLECTIONS_NAMES.contains(collection))
            .collect(Collectors.toList());
    }

    @Override
    protected List<JSONObject> getAllDocumentsFromDb(final String collectionName) {
        final List<JSONObject> documents = new ArrayList<>();
        final FindIterable<Document> cursor = db.getCollection(collectionName).find();

        for (final Document document : cursor) {
            documents.add(JSONValue.parse(document.toJson(), JSONObject.class));
        }

        return documents;
    }

    @Override
    protected List<String> getSystemCollections() {
        return SYSTEM_COLLECTIONS_NAMES;
    }
}
