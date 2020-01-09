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

import org.junit.Assert;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

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
    private final DB db;

    /**
     * @param db mongodb instance.
     */
    public MongoDBTester(DB db) {
        this.expectedFilesRoot = "/";
        this.predefinedFilesRoot = "/";
        this.db = db;
    }

    /**
     * @param db                  mongodb instance.
     * @param expectedFilesRoot   expected files root. File path passed to setDBState() will be relative to this root.
     * @param predefinedFilesRoot predefined files root.
     */
    public MongoDBTester(DB db, String expectedFilesRoot, String predefinedFilesRoot) {
        this.expectedFilesRoot = expectedFilesRoot;
        this.predefinedFilesRoot = predefinedFilesRoot;
        this.db = db;
    }

    /**
     *
     */
    public void dropDataBase() {
        db.dropDatabase();
    }

    /**
     * @param filePath file path with json data which describes expected DB state.
     */
    public void assertDBStateEquals(String filePath) {
        final DBState dbState = new DBState(Utils.readJsonFile(expectedFilesRoot + filePath));
        assertCollectionNamesAreEquals(dbState);
        assertDocumentsInCollectionAreEquals(dbState);
    }

    /**
     * @param fileStream stream with json data which describes how to populate DB.
     */
    public void setDBState(InputStream fileStream) {
        populateDB(JSONValue.parse(new InputStreamReader(fileStream, StandardCharsets.UTF_8), JSONObject.class));
    }

    /**
     * @param filePath file path relative to predefinedFilesRoot. File should contain json data
     *                 which describes how to populate DB.
     */
    public void setDBState(String filePath) {
        populateDB(Utils.readJsonFile(predefinedFilesRoot + filePath));
    }

    /**
     * @param collectionName name of mongoDB collection to create
     * @param docArgs        documents to populate collection
     */
    public void setDBState(String collectionName, JSONObject... docArgs) {
        final JSONArray docs = new JSONArray();
        Collections.addAll(docs, docArgs);
        populateDBCollection(collectionName, docs);
    }

    private void populateDB(JSONObject object) {
        for (Map.Entry<String, Object> collection : object.entrySet()) {
            populateDBCollection(collection.getKey(), (JSONArray) collection.getValue());
        }
    }

    private void populateDBCollection(String collectionName, JSONArray docs) {
        final DBCollection dbCollection = db.getCollection(collectionName);

        for (Object document : docs) {
            final DBObject doc = (DBObject) JSON.parse(document.toString());
            dbCollection.insert(doc);
        }
    }

    private void assertDocumentsInCollectionAreEquals(DBState dbState) {
        for (String collectionName : dbState.getCollectionNames()) {
            final Set<JSONObject> actualDocs = getAllDocumentsFromDb(collectionName);
            final Set<JSONObject> expectedDocs = dbState.getDocuments(collectionName);
            dbState.getMatchStrategy(collectionName).assertTheSame(expectedDocs, actualDocs);
        }
    }

    private void assertCollectionNamesAreEquals(DBState dbState) {
        final Set<String> collections = db.getCollectionNames();
        final SortedSet<String> collectionsInDb = new TreeSet<String>();

        for (String collection : collections) {
            if (!SYSTEM_COLLECTIONS_NAMES.contains(collection)) {
                collectionsInDb.add(collection);
            }
        }

        Assert.assertEquals("Names of collections in db is different from described in json file!",
                dbState.getCollectionNames(), collectionsInDb);
    }

    private Set<JSONObject> getAllDocumentsFromDb(String collectionName) {
        final Set<JSONObject> documents = new HashSet<JSONObject>();
        final DBCursor cursor = db.getCollection(collectionName).find();

        while (cursor.hasNext()) {
            documents.add(JSONValue.parse(cursor.next().toString(), JSONObject.class));
        }

        return documents;
    }
}
