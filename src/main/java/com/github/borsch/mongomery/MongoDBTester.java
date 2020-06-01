package com.github.borsch.mongomery;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bson.Document;

import com.github.borsch.mongomery.exceptions.ComparisonException;
import com.github.borsch.mongomery.matching.PatternMatchStrategy;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * Core instance of a library.
 */
public class MongoDBTester {

    private Set<String> SYSTEM_COLLECTIONS_NAMES = new HashSet<String>() {{
        add("system.namespaces");
        add("system.indexes");
        add("system.profile");
        add("system.js");
    }};

    private final String expectedFilesRoot;
    private final String predefinedFilesRoot;
    private final MongoDatabase db;
    private final Set<String> ignorePath;

    /**
     * @param db mongodb instance.
     */
    public MongoDBTester(final MongoDatabase db) {
        this(db, "/", "/");
    }

    /**
     * @param db                  mongodb instance.
     * @param expectedFilesRoot   expected files root. File path passed to setDBState() will be relative to this root.
     * @param predefinedFilesRoot predefined files root.
     */
    public MongoDBTester(final MongoDatabase db, final String expectedFilesRoot, final String predefinedFilesRoot) {
        this.expectedFilesRoot = expectedFilesRoot;
        this.predefinedFilesRoot = predefinedFilesRoot;
        this.db = db;
        this.ignorePath = new HashSet<>();
    }

    public void addIgnorePaths(final String... ignorePath) {
        Collections.addAll(this.ignorePath, ignorePath);
    }

    /**
     *
     */
    public void dropDataBase() {
        for (String collectionName : db.listCollectionNames()) {
            db.getCollection(collectionName).drop();
        }
    }

    /**
     * reset ignore path list
     */
    public void cleanIgnorePath() {
        ignorePath.clear();
    }

    /**
     * @param filePath file path with json data which describes expected DB state.
     */
    public void assertDBStateEquals(final String filePath) {
        final DBState dbState = new DBState(Utils.readJsonFile(expectedFilesRoot + filePath));
        assertCollectionNamesAreEquals(dbState);
        assertDocumentsInCollectionAreEquals(dbState);
    }

    /**
     * @param fileStream stream with json data which describes how to populate DB.
     */
    public void setDBState(final InputStream fileStream) {
        populateDB(JSONValue.parse(new InputStreamReader(fileStream, StandardCharsets.UTF_8), JSONObject.class));
    }

    /**
     * @param filePath file path relative to predefinedFilesRoot. File should contain json data
     *                 which describes how to populate DB.
     */
    public void setDBState(final String filePath) {
        populateDB(Utils.readJsonFile(predefinedFilesRoot + filePath));
    }

    /**
     * @param collectionName name of mongoDB collection to create
     * @param docArgs        documents to populate collection
     */
    public void setDBState(final String collectionName, final JSONObject... docArgs) {
        final JSONArray docs = new JSONArray();
        Collections.addAll(docs, docArgs);
        populateDBCollection(collectionName, docs);
    }

    private void populateDB(final JSONObject object) {
        for (final Map.Entry<String, Object> collection : object.entrySet()) {
            populateDBCollection(collection.getKey(), (JSONArray) collection.getValue());
        }
    }

    private void populateDBCollection(final String collectionName, final JSONArray docs) {
        final MongoCollection<Document> dbCollection = db.getCollection(collectionName, Document.class);

        for (final Object document : docs) {
            final JSONObject jsonObject = (JSONObject) document;
            InsertUtils.replaceInsertPlaceholders(jsonObject);
            dbCollection.insertOne(new Document(jsonObject));
        }
    }

    private void assertDocumentsInCollectionAreEquals(final DBState dbState) {
        for (final String collectionName : dbState.getCollectionNames()) {
            final Set<JSONObject> actualDocs = getAllDocumentsFromDb(collectionName);
            final Set<JSONObject> expectedDocs = dbState.getDocuments(collectionName);
            PatternMatchStrategy.assertTheSame(collectionName, expectedDocs, actualDocs, ignorePath);
        }
    }

    private void assertCollectionNamesAreEquals(final DBState dbState) {
        final MongoIterable<String> collections = db.listCollectionNames();
        final SortedSet<String> collectionsInDb = new TreeSet<>();

        for (final String collection : collections) {
            if (!SYSTEM_COLLECTIONS_NAMES.contains(collection)) {
                collectionsInDb.add(collection);
            }
        }

        if (!dbState.getCollectionNames().equals(collectionsInDb)) {
            throw new ComparisonException(
                "Names of collections in db is different from described in json file!\nExpected: %s\nActual: %s",
                dbState.getCollectionNames(), collectionsInDb
            );
        }
    }

    private Set<JSONObject> getAllDocumentsFromDb(final String collectionName) {
        final Set<JSONObject> documents = new HashSet<>();
        final FindIterable<Document> cursor = db.getCollection(collectionName).find();

        for (final Document document : cursor) {
            documents.add(JSONValue.parse(document.toJson(), JSONObject.class));
        }

        return documents;
    }
}
