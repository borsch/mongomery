package com.github.borsch.mongomery;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.borsch.mongomery.exceptions.ComparisonException;
import com.github.borsch.mongomery.type.MatchingStrategyType;

class OrderedMatchingStrategyTest extends AbstractMongoTest {

    private static MongoDBTester mongoDBTester;

    @BeforeAll
    static void init() {
        mongoDBTester = new MongoDBTester(getDatabase(), MatchingStrategyType.ORDERED);
    }

    @BeforeEach
    void beforeMethod() {
        mongoDBTester.cleanIgnorePath();
        mongoDBTester.dropDataBase();
        mongoDBTester.addIgnorePaths("_id");
    }

    @ParameterizedTest
    @CsvSource({
        "/OrderedMatchingStrategyTest/predefined/initDb.json, /OrderedMatchingStrategyTest/expected/successMatch.json",
        "/OrderedMatchingStrategyTest/predefined/initDb.json, /OrderedMatchingStrategyTest/expected/successMatch_anySring.json",
    })
    void shouldMatchWhenSameOrder(final String initialDb, final String expectedDb) {
        mongoDBTester.setDBState(initialDb);
        mongoDBTester.assertDBStateEquals(expectedDb);
    }

    @Test
    void shouldThrowException_whenSameObjectsButDifferentOrder() {
        mongoDBTester.setDBState("/OrderedMatchingStrategyTest/predefined/initDb.json");
        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("/OrderedMatchingStrategyTest/expected/error_differentOrder.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection [test] has different objects at index [0].")
            .hasMessageContaining("Expected object: {\"field1\":\"gaer64\",\"name\":\"34hserh\"}");
    }

    @Test
    void shouldAssertArraySize_fine() {
        mongoDBTester.addIgnorePaths("name", "field1");
        mongoDBTester.setDBState("/OrderedMatchingStrategyTest/predefined/initDbWithArray.json");
        mongoDBTester.assertDBStateEquals("/OrderedMatchingStrategyTest/expected/arrayLengthAssert.json");
    }

    @Test
    void shouldAssertArraySize_fail() {
        mongoDBTester.addIgnorePaths("name", "field1");
        mongoDBTester.setDBState("/OrderedMatchingStrategyTest/predefined/initDbWithArray.json");

        assertThatThrownBy(() -> mongoDBTester.assertDBStateEquals("/OrderedMatchingStrategyTest/expected/differentArrayLength.json"))
            .isInstanceOf(ComparisonException.class)
            .hasMessageStartingWith("Collection [test] has different objects at index [0].")
            .hasMessageContaining("Expected object: {\"array\":\"$arrayWithSize(5)\"}");
    }

}