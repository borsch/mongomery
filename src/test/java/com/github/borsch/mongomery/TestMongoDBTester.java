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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.borsch.mongomery.exceptions.ComparisonException;
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

    @ParameterizedTest
    @CsvSource({
        "simple strict match test, strictMatch/simplestTestDataSet.json, strictMatch/simplestTestShouldPassExpectedDataSet.json",
        "pattern match with simple $anyObject() , patternMatch/simplestTest$anyObjectDataSet.json, patternMatch/simplestTest$anyObjectDataSet.json",
        "pattern match: $anyObject() and $anyString(), patternMatch/simplestTest$anyObjectMixedWith$anyStringDataSet.json, patternMatch/simplestTest$anyObjectMixedWith$anyStringDataSet.json",
        "pattern match: complex test, patternMatch/complexTestDataSet.json, patternMatch/complexTestDataSet.json",
        "pattern match: $anyObject() with number of fields, advancedPatternMatch/testShouldPass$anyObjectDataSet.json, advancedPatternMatch/testShouldPass$anyObjectDataSet.json",
        "pattern match: $anyString() with pattern, advancedPatternMatch/testShouldPass$anyStringDataSet.json, advancedPatternMatch/testShouldPass$anyStringDataSet.json"
    })
    void shouldPass(final String testName, final String setup, final String expected) {
        mongoDBTester.setDBState(setup);
        mongoDBTester.assertDBStateEquals(expected);
    }

    @Test
    void strictMatchSimplestTestShouldFail() {
        mongoDBTester.setDBState("strictMatch/simplestTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("strictMatch/simplestTestShouldFailExpectedDataSet.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessage(Utils.readFile("/error/strictMatch/simplestTestShouldFailExpectedDataSet.txt"));
    }

    @Test
    void patternMatch$anyObjectShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyObjectCantBeNullDataSet.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void shouldThrowException_whenDifferentCollectionSize() {
        mongoDBTester.setDBState("differentCollectionSize.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("differentCollectionSize.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessage("Collection TestCollection has different elements size. Expected size: 2, actual size: 1");
    }

    @Test
    void patternMatch$anyStringShouldFailOnNull() {
        mongoDBTester.setDBState("patternMatch/simplestTest$PlaceholderCantBeNullDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/simplestTest$anyStringCantBeNullDataSet.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void complexTestShouldFailIfCantFindAllStrictMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllStrictMatches.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void complexTestShouldFailIfCantFindAllPatternMatches() {
        mongoDBTester.setDBState("patternMatch/complexTestDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/complexTestShouldFailIfCantFindAllPatternMatches.json"))
            .isInstanceOf(ComparisonException.class)
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
            .isInstanceOf(ComparisonException.class)
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
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection Account doesn't match with expected.");
    }

    @Test
    void testShouldFail$anyObjectDataSet() {
        mongoDBTester.setDBState("advancedPatternMatch/testShouldPass$anyObjectDataSet.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("advancedPatternMatch/testShouldFail$anyObjectDataSet.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection Vocabulary doesn't match with expected.");
    }

    @Test
    void shouldFailWhenCollectionNamesAreDifferent() {
        mongoDBTester.setDBState("TestCollection", new JSONObject());
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("differentCollectionNames.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Names of collections in db is different from described in json file!");
    }

    @Test
    void shouldThrowException_whenCannotMatchStrictly() {
        mongoDBTester.setDBState("patternMatch/patternMatch_withStrictMatch.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/patternMatch_withStrictMatch.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessage(Utils.readFile("/error/patternMatch/patternMatch_withStrictMatch.txt"));
    }

    @Test
    void shouldMatchAnyDate() {
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
    void shouldMatchStrictLocalDateTime() {
        final JSONObject dbState = new JSONObject();
        dbState.put("localDateTime", LocalDateTime.of(2222, 2, 2, 1, 3, 4));

        mongoDBTester.setDBState("TestCollection", dbState);
        mongoDBTester.assertDBStateEquals("localDateTime/expectedValidMatch.json");
    }

    @Test
    void shouldThrowException_whenCannotParseLocalDateTime() {
        final JSONObject dbState = new JSONObject();
        dbState.put("localDateTime", LocalDateTime.of(2222, 2, 2, 1, 3, 4));
        mongoDBTester.setDBState("TestCollection", dbState);

        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("localDateTime/unparsableLocalDateTime.json"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't parse value of placeholder unparsable_local_date_time to LocalDateTime. Field localDateTime");
    }

    @Test
    void shouldMatchStrictLocalDate() {
        final JSONObject dbState = new JSONObject();
        dbState.put("localDate", LocalDate.of(2222, 2, 2));
        mongoDBTester.setDBState("TestCollection", dbState);

        mongoDBTester.assertDBStateEquals("localDate/expectedValidMatch.json");
    }

    @Test
    void shouldMatchStrictLocalDate_insertUtilForLocalDate() {
        mongoDBTester.setDBState("localDate/expectedValidMatch.json");
        mongoDBTester.assertDBStateEquals("localDate/expectedValidMatch.json");
    }

    @Test
    void shouldThrowException_whenCannotParseLocalDate() {
        final JSONObject dbState = new JSONObject();
        dbState.put("localDate", LocalDateTime.of(2222, 2, 2, 1, 3, 4));
        mongoDBTester.setDBState("TestCollection", dbState);

        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("localDate/unparsableLocalDate.json"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't parse value of placeholder unparsable_local_date to LocalDate. Field localDate");
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
            .isInstanceOf(ComparisonException.class)
            .hasMessage(Utils.readFile("/error/strictMatch/strictMatchIgnoreFields.txt"));
    }

    @Test
    void shouldMatch_withIgnoreFields_PatternMatchStrategy_success() {
        mongoDBTester.addIgnorePaths(
            "_id",
            "intField",
            "subObjectField2.unexpectedField"
        );

        mongoDBTester.setDBState("patternMatch/patternMatchIgnoreFields.json");
        mongoDBTester.assertDBStateEquals("patternMatch/patternMatchIgnoreFields_ok.json");
    }

    @Test
    void shouldMatch_withIgnoreFields_PatternMatchStrategy_fail() {
        mongoDBTester.addIgnorePaths(
            "_id",
            "intField",
            "subObjectField2.field1"
        );

        mongoDBTester.setDBState("patternMatch/patternMatchIgnoreFields.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("patternMatch/patternMatchIgnoreFields_fail.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection Collection doesn't match with expected.");
    }

    @Test
    void shouldInsertLocalDateTime() {
        mongoDBTester.setDBState("insertLocalDateTime.json");
        mongoDBTester.assertDBStateEquals("insertLocalDateTime.json");
    }

    @Test
    void shouldInsertLong() {
        mongoDBTester.setDBState("insertLong.json");
        mongoDBTester.assertDBStateEquals("insertLong.json");
    }

}