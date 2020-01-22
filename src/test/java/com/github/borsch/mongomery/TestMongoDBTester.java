package com.github.borsch.mongomery;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import net.minidev.json.JSONObject;

class TestMongoDBTester {

    private static MongoDBTester mongoDBTester;
    private static MongodForTestsFactory factory = null;

    @BeforeAll
    static void init() throws IOException {
        factory = MongodForTestsFactory.with(Version.Main.V3_3);

        final MongoClient mongo = factory.newMongo();
        final MongoDatabase db = mongo.getDatabase("test");
        mongoDBTester = new MongoDBTester(db, "/expected/", "/predefined/");
    }

    @BeforeEach
    void beforeMethod() {
        mongoDBTester.cleanIgnorePath();
        mongoDBTester.dropDataBase();
    }

    @AfterAll
    static void shutdown() {
        if (factory != null)
            factory.shutdown();
    }

    @Test
    void strictMatchSimplestTestShouldPass() {
        mongoDBTester.setDBState("strictMatch/simplestTestDataSet.json");
        mongoDBTester.assertDBStateEquals("strictMatch/simplestTestShouldPassExpectedDataSet.json");
    }

    @Test
    void strictMatchSimplestTestShouldFail() {
        mongoDBTester.setDBState("strictMatch/simplestTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("strictMatch/simplestTestShouldFailExpectedDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Books doesn't match with expected.");
    }

    @Test
    void patternMatchOnlySimplestTestFor$AnyObjectShouldPass() {
        mongoDBTester.setDBState("patternMatch/simplestTest$anyObjectDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectDataSet.json");
    }

    @Test
    void simplestTest$anyObjectMixedWith$anyString() {
        mongoDBTester.setDBState("patternMatch/simplestTest$anyObjectMixedWith$anyStringDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectMixedWith$anyStringDataSet.json");
    }

    @Test
    void patternMatch$anyObjectShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectCantBeNullDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void patternMatch$anyStringShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyStringCantBeNullDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void complexTestShouldPass() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        mongoDBTester.assertDBStateEquals("patternMatch/complexTestDataSet.json");
    }

    @Test
    void complexTestShouldFailIfCantFindAllStrictMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllStrictMatches.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void complexTestShouldFailIfCantFindAllPatternMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllPatternMatches.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void shouldFailIfUserUsesPlaceholderMixedWithCharInString() {
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
    void shouldCompareLongViaEqualPatternMatch() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 12345623534L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch$eqPattern.json");
    }

    @Test
    void shouldFailToCompareLongViaEqualPatternMatchWhenValuesAreDifferent() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 1L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch$eqPattern.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection TestCollection doesn't match with expected.");
    }

    @Test
    void shouldCompareLongViaAnyPatternMatch() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("fieldLongValue", 12345623534L);
        mongoDBTester.setDBState("TestCollection", jsonObject);
        mongoDBTester.assertDBStateEquals("patternMatch/longValueMatch$anyPattern.json");
    }

    @Test
    void shouldFailIfPlaceholderIsInsideArray() {
        final InputStream fileStream = this.getClass()
                .getResourceAsStream("/predefined/patternMatch/placeholderCantBeInsideArray.json");
        mongoDBTester.setDBState(fileStream);
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/placeholderCantBeInsideArray.json"))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("You can't use match patterns in arrays for now!");
    }

    @Test
    void simplestTestShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("patternMatch/simplestTestShouldFail$anyObjectDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTestShouldFail$anyObjectDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void advancedPatternMatchShouldPass() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyStringDataSet.json");
        mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldPass$anyStringDataSet.json");
    }

    @Test
    void testShouldPass$anyObjectDataSet() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
        mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
    }

    @Test
    void testShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldFail$anyObjectDataSet.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Vocabulary doesn't match with expected.");
    }

    @Test
    void shouldFailWhenCollectionNamesAreDifferent() {
        mongoDBTester.setDBState("TestCollection", new JSONObject());
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("differentCollectionNames.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Names of collections in db is different from described in json file!");
    }

    @Test
    void shouldThrowException_whenCannotMatchStrictly() {
        mongoDBTester.setDBState("patternMatch/patternMatch_withStrictMatch.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/patternMatch_withStrictMatch.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Can't find pattern match for 1 element(s) in collection TestCollection.");
    }

    @Test
    void shouldMathAnyDate() {
        final JSONObject dbState = new JSONObject();
        dbState.put("date", new Date());
        dbState.put("localDate", LocalDate.now());
        dbState.put("time", LocalTime.now());
        dbState.put("localDateTime", LocalDateTime.now());
        dbState.put("stringField", "some-string");

        mongoDBTester.setDBState("TestCollection", dbState);

        mongoDBTester.assertDBStateEquals("patternMatch/anyDateMatch.json");
    }

    @Test
    void shouldPassStrictMatchWithIgnoreFields() {
        mongoDBTester.addIgnorePaths(
            "_id",
            "intField",
            "subObjectField.field1",
            "subObjectField2.subObjectField2.field1",
            "subObjectField2.field1"
        );

        mongoDBTester.setDBState("strictMatch/strictMatchIgnoreFields.json");
        mongoDBTester.assertDBStateEquals("strictMatch/strictMatchIgnoreFields_ok.json");
    }

    @Test
    void shouldFailStrictMatchWithIgnoreFields() {
        mongoDBTester.addIgnorePaths(
            "_id",
            "intField",
            "subObjectField.field1",
            "subObjectField2.subObjectField2.field1",
            "subObjectField2.field1"
        );

        mongoDBTester.setDBState("strictMatch/strictMatchIgnoreFields.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("strictMatch/strictMatchIgnoreFields_fail.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Collection doesn't match with expected.");
    }

    @Test
    public void shouldMatch_withIgnoreFields_PatternMatchStrategy_success() {
        mongoDBTester.addIgnorePaths(
            "_id",
            "intField",
            "subObjectField2.unexpectedField"
        );

        mongoDBTester.setDBState("patternMatch/patternMatchIgnoreFields.json");
        mongoDBTester.assertDBStateEquals("patternMatch/patternMatchIgnoreFields_ok.json");
    }

    @Test
    public void shouldMatch_withIgnoreFields_PatternMatchStrategy_fail() {
        mongoDBTester.addIgnorePaths(
            "_id",
            "intField",
            "subObjectField2.field1"
        );

        mongoDBTester.setDBState("patternMatch/patternMatchIgnoreFields.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/patternMatchIgnoreFields_fail.json"))
            .isInstanceOf(AssertionError.class)
            .hasMessageStartingWith("Collection Collection doesn't match with expected.");
    }
}