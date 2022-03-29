package com.github.borsch.mongomery;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.borsch.mongomery.exceptions.ComparisonException;
import com.github.borsch.mongomery.matching.MatchingStrategy;
import com.github.borsch.mongomery.matching.OrderedMatchStrategy;
import com.github.borsch.mongomery.matching.UnorderedMatchStrategy;
import com.github.borsch.mongomery.type.MatchingStrategyType;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * Core instance of a library.
 */
public abstract class AbstractMongoDBTester {

    private final String expectedFilesRoot;
    private final String predefinedFilesRoot;
    private final Set<String> ignorePath;
    private final MatchingStrategy matchingStrategy;

    /**
     * @param db mongodb instance.
     */
    public AbstractMongoDBTester(final MatchingStrategyType strategyType) {
        this(strategyType, "", "");
    }

    /**
     * @param db                  mongodb instance.
     * @param strategyType        strategy to match objects in collections
     * @param expectedFilesRoot   expected files root. File path passed to setDBState() will be relative to this root.
     * @param predefinedFilesRoot predefined files root.
     */
    public AbstractMongoDBTester(final MatchingStrategyType strategyType, final String expectedFilesRoot, final String predefinedFilesRoot) {
        this.expectedFilesRoot = expectedFilesRoot;
        this.predefinedFilesRoot = predefinedFilesRoot;
        this.ignorePath = new HashSet<>();

        this.matchingStrategy = strategyType == MatchingStrategyType.ORDERED
            ? new OrderedMatchStrategy()
            : new UnorderedMatchStrategy();
    }

    public void addIgnorePaths(final String... ignorePath) {
        Collections.addAll(this.ignorePath, ignorePath);
    }

    public abstract void dropDataBase();

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

    protected abstract void populateDBCollection(final String collectionName, final JSONArray docs);

    private void assertDocumentsInCollectionAreEquals(final DBState dbState) {
        for (final String collectionName : dbState.getCollectionNames()) {
            final List<JSONObject> actualDocs = getAllDocumentsFromDb(collectionName);
            final List<JSONObject> expectedDocs = dbState.getDocuments(collectionName);
            matchingStrategy.assertTheSame(collectionName, expectedDocs, actualDocs, ignorePath);
        }
    }

    private void assertCollectionNamesAreEquals(final DBState dbState) {
        final List<String> dbCollections = getDbCollections();
        final List<String> systemCollections = getSystemCollections();
        boolean hasAbsentCollection = false;

        for (final String collection : dbState.getCollectionNames()) {
            if (!systemCollections.contains(collection) && !dbCollections.contains(collection)) {
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

    protected abstract List<String> getDbCollections();

    protected abstract List<JSONObject> getAllDocumentsFromDb(final String collectionName);

    protected abstract List<String> getSystemCollections();
}
