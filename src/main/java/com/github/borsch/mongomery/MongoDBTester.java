package com.github.borsch.mongomery;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.github.borsch.mongomery.exceptions.ComparisonException;
import com.github.borsch.mongomery.matching.MatchingStrategy;
import com.github.borsch.mongomery.matching.OrderedMatchStrategy;
import com.github.borsch.mongomery.matching.UnorderedMatchStrategy;
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
public class MongoDBTester {

    private static final Set<String> SYSTEM_COLLECTIONS_NAMES = new HashSet<String>() {{
        add("system.namespaces");
        add("system.indexes");
        add("system.profile");
        add("system.js");
    }};

    private final String expectedFilesRoot;
    private final String predefinedFilesRoot;
    private final MongoDatabase db;
    private final Set<String> ignorePath;
    private final MatchingStrategy matchingStrategy;

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
        this.expectedFilesRoot = expectedFilesRoot;
        this.predefinedFilesRoot = predefinedFilesRoot;
        this.db = db;
        this.ignorePath = new HashSet<>();

        this.matchingStrategy = strategyType == MatchingStrategyType.ORDERED
            ? new OrderedMatchStrategy()
            : new UnorderedMatchStrategy();
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
            final List<JSONObject> actualDocs = getAllDocumentsFromDb(collectionName);
            final List<JSONObject> expectedDocs = dbState.getDocuments(collectionName);
            matchingStrategy.assertTheSame(collectionName, expectedDocs, actualDocs, ignorePath);
        }
    }

    private void assertCollectionNamesAreEquals(final DBState dbState) {
        final List<String> dbCollections = getDbCollections();
        boolean hasAbsentCollection = false;

        for (final String collection : dbState.getCollectionNames()) {
            if (!SYSTEM_COLLECTIONS_NAMES.contains(collection) && !dbCollections.contains(collection)) {
                hasAbsentCollection = true;
                break;
            }
        }

        if (hasAbsentCollection) {
            throw new ComparisonException(
                "Names of collections in db is different from described in json file!\nExpected: %s\nActual: %s",
                dbState.getCollectionNames(), dbCollections
            );
        }
    }

    private List<String> getDbCollections() {
        return StreamSupport.stream(db.listCollectionNames().spliterator(), false)
            .filter(collection -> !SYSTEM_COLLECTIONS_NAMES.contains(collection))
            .collect(Collectors.toList());
    }

    private List<JSONObject> getAllDocumentsFromDb(final String collectionName) {
        final List<JSONObject> documents = new ArrayList<>();
        final FindIterable<Document> cursor = db.getCollection(collectionName).find();

        for (final Document document : cursor) {
            documents.add(JSONValue.parse(document.toJson(), JSONObject.class));
        }

        return documents;
    }
}
