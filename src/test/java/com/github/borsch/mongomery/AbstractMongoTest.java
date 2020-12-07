package com.github.borsch.mongomery;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class AbstractMongoTest {

    static MongoDatabase database = null;
    static MongodForTestsFactory factory = null;

    @BeforeAll
    static void initBase() throws IOException {
        factory = MongodForTestsFactory.with(Version.Main.V3_3);
        database = factory.newMongo().getDatabase("test");
    }

    @AfterAll
    static void shutdown() {
        if (factory != null) {
            factory.shutdown();
        }
    }

}
