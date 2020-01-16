package com.github.borsch.mongomery;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

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

    @Test
    public void strictMatchSimplestTestShouldFail() {
        mongoDBTester.setDBState("strictMatch/simplestTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("strictMatch/simplestTestShouldFailExpectedDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Books doesn't match with expected.");
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

    @Test
    public void patternMatch$anyObjectShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectCantBeNullDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void patternMatch$anyStringShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyStringCantBeNullDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void complexTestShouldPass() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/complexTestDataSet.json");
    }

    @Test
    public void complexTestShouldFailIfCantFindAllStrictMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllStrictMatches.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void complexTestShouldFailIfCantFindAllPatternMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllPatternMatches.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void shouldFailIfUserUsesPlaceholderMixedWithCharInString() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("_id", "$anyObject()");
        jsonObject.put("email", " $anyString()");
        jsonObject.put("firstName", "firstName)");
        mongoDBTester.setDBState("TestCollection", jsonObject);
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/placeholderCantBeMixedWithCharacter.json"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("String with placeholder shouldn't contain any other characters (even whitespaces)! Your string was: \" $anyString()\"");
    }

    @Test
    public void shouldCompareLongViaEqualPatternMatch() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 12345623534L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch$eqPattern.json");
    }

    @Test
    public void shouldFailToCompareLongViaEqualPatternMatchWhenValuesAreDifferent() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 1L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch$eqPattern.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void shouldCompareLongViaAnyPatternMatch() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 12345623534L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch$anyPattern.json");
    }

    @Test
    public void shouldFailIfPlaceholderIsInsideArray() {
        final InputStream fileStream = this.getClass()
                .getResourceAsStream("/predefined/patternMatch/placeholderCantBeInsideArray.json");
        mongoDBTester.setDBState(fileStream);
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/placeholderCantBeInsideArray.json"))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("You can't use match patterns in arrays for now!");
    }

    @Test
    public void simplestTestShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("patternMatch/simplestTestShouldFail$anyObjectDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTestShouldFail$anyObjectDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
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

    @Test
    public void testShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldFail$anyObjectDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void shouldFailWhenCollectionNamesAreDifferent() {
        mongoDBTester.setDBState("TestCollection", new JSONObject());
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("differentCollectionNames.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Names of collections in db is different from described in json file!");
    }

    @Test
    public void shouldThrowException_whenCannotMatchStrictly() {
        mongoDBTester.setDBState("patternMatch/patternMatch_withStrictMatch.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/patternMatch_withStrictMatch.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s).");
    }

    @Test
    public void shouldMathAnyDate() {
        final JSONObject dbState = new JSONObject();
        dbState.put("date", new Date());
        dbState.put("localDate", LocalDate.now());
        dbState.put("time", LocalTime.now());
        dbState.put("localDateTime", LocalDateTime.now());
        dbState.put("stringField", "some-string");

        mongoDBTester.setDBState("TestCollection", dbState);

        mongoDBTester.assertDBStateEquals("patternMatch/anyDateMatch.json");
    }
}