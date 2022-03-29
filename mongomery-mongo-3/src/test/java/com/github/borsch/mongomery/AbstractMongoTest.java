package com.github.borsch.mongomery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import org.testcontainers.utility.DockerImageName;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Testcontainers
public class AbstractMongoTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractMongoTest.class);
    private static final String MONGO_DB_VERSION = System.getProperty("mongodb.container.version", "3.6.23");

    static {
        log.info("Start MongoDB container with DB version - {}", MONGO_DB_VERSION);
    }

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:" + MONGO_DB_VERSION));

    protected static MongoDatabase getDatabase() {
        String databaseName = RandomStringUtils.random(10);
        String databaseConnection = MONGO_DB_CONTAINER.getReplicaSetUrl(databaseName);
        MongoClient mongoClient = MongoClients.create(databaseConnection);
        return mongoClient.getDatabase(databaseName);
    }

}
