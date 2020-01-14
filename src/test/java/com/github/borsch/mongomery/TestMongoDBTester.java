package com.github.borsch.mongomery;

import java.io.IOException;
import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import net.minidev.json.JSONObject;

public class TestMongoDBTester {

    private static MongoDBTester mongoDBTester;
    private static MongodForTestsFactory factory = null;

    @BeforeClass
    public static void init() throws IOException {
        factory = MongodForTestsFactory.with(Version.Main.V3_3);

        final MongoClient mongo = factory.newMongo();
        final MongoDatabase db = mongo.getDatabase("test");
        mongoDBTester = new MongoDBTester(db, "/expected/", "/predefined/");
    }

    @Before
    public void beforeMethod() {
        mongoDBTester.dropDataBase();
    }

    @AfterClass
    public static void shutdown() {
        if (factory != null)
            factory.shutdown();
    }

    @Test
    public void strictMatchSimplestTestShouldPass() {
        mongoDBTester.setDBState("strictMatch/simplestTestDataSet.json");
        mongoDBTester.assertDBStateEquals("strictMatch/simplestTestShouldPassExpectedDataSet.json");
    }

    @Test(expected = AssertionError.class)
    public void strictMatchSimplestTestShouldFail() {
        mongoDBTester.setDBState("strictMatch/simplestTestDataSet.json");
        mongoDBTester.assertDBStateEquals("strictMatch/simplestTestShouldFailExpectedDataSet.json");
    }

    @Test
    public void patternMatchOnlySimplestTestFor$AnyObjectShouldPass() {
        mongoDBTester.setDBState("patternMatch/simplestTest$anyObjectDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectDataSet.json");
    }

    @Test
    public void simplestTest$anyObjectMixedWith$anyString() {
        mongoDBTester.setDBState("patternMatch/simplestTest$anyObjectMixedWith$anyStringDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectMixedWith$anyStringDataSet.json");
    }

    @Test(expected = AssertionError.class)
    public void patternMatch$anyObjectShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectCantBeNullDataSet.json");
    }

    @Test(expected = AssertionError.class)
    public void patternMatch$anyStringShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyStringCantBeNullDataSet.json");
    }

    @Test
    public void complexTestShouldPass() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/complexTestDataSet.json");
    }

    @Test(expected = AssertionError.class)
    public void complexTestShouldFailIfCantFindAllStrictMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllStrictMatches.json");
    }

    @Test(expected = AssertionError.class)
    public void complexTestShouldFailIfCantFindAllPatternMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllPatternMatches.json");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfUserUsesPlaceholderMixedWithCharInString() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("_id", "$anyObject()");
        jsonObject.put("email", " $anyString()");
        jsonObject.put("firstName", "firstName)");
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/placeholderCantBeMixedWithCharacter.json");
    }

    @Test
    public void shouldCompareLongViaPatternMatch() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 12345623534L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch.json");
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToCompareLongViaPatternMatch() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 1L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch.json");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailIfPlaceholderIsInsideArray() {
        final InputStream fileStream = this.getClass()
                .getResourceAsStream("/predefined/patternMatch/placeholderCantBeInsideArray.json");
        mongoDBTester.setDBState(fileStream);
        mongoDBTester.assertDBStateEquals("patternMatch/placeholderCantBeInsideArray.json");
    }

    @Test(expected = AssertionError.class)
    public void simplestTestShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("patternMatch/simplestTestShouldFail$anyObjectDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTestShouldFail$anyObjectDataSet.json");
    }

    @Test
    public void advancedPatternMatchShouldPass() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyStringDataSet.json");
        mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldPass$anyStringDataSet.json");
    }

    @Test
    public void testShouldPass$anyObjectDataSet() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
        mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
    }

    @Test(expected = AssertionError.class)
    public void testShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
        mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldFail$anyObjectDataSet.json");
    }
}