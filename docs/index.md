---
layout: default
---

## About

Mongomery - is a simple library that will help to insert data into MongoDB and assert MongoDB state after test

## Install

Mongomery is deployed to Maven Central os setup is pretty simple.

For Maven projects add

```xml
<dependency>
    <groupId>com.github.borsch</groupId>
    <artifactId>mongomery</artifactId>
    <version>X.X.X</version>
</dependency>

```

For Gradle projects add
```groovy
compile group: 'com.github.borsch', name: 'mongomery', version: 'X.X.X'
```

Latest version can be found [here](https://mvnrepository.com/artifact/com.github.borsch/mongomery)

### Usage

To get the most from Mongomery please use MongoDB driver 3.7 because since this version support for Java Time API was introduced(move information can be found [here](http://mongodb.github.io/mongo-java-driver/3.7/whats-new/#jsr-310-instant-localdate-localdatetime-support))


```java
// instantiate MongoClient
com.mongodb.MongoClient mongoClient = ..;
// get database that will be used for testing
com.mongodb.client.MongoDatabase database = mongoClient.getDatabase("database_name_for_test");

// instantiate MongoDBTester
MongoDBTester mongoDBTester = new MongoDBTester(database, MatchingStrategyType.UNORDERED);


@Test
public void myTest() {
    // presetup database state
    mongoDBTested.setDBState("/path/to/json/file.json");
    // or insert manually create and isert insert object
    net.minidev.json.JSONObject jsonObject = new net.minidev.json.JSONObject();
    jsonObject.put("fieldName", 1L);
    mongoDBTester.setDBState("collection_name", jsonObject);

    // .. execute code that shoul be tested ..

    // assert MongoDB state
    mongoDBTester.assertDBStateEquals("/path/to/assert/json/file.json");
}
```