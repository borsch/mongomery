package com.github.borsch.mongomery;

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

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:3.6.23"));

    protected static MongoDatabase getDatabase() {
        String databaseName = RandomStringUtils.random(10);
        String databaseConnection = MONGO_DB_CONTAINER.getReplicaSetUrl(databaseName);
        MongoClient mongoClient = MongoClients.create(databaseConnection);
        return mongoClient.getDatabase(databaseName);
    }

}
