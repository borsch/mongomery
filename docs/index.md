---
layout: default
title: Mongomery
---

## Table of Contents

- [About](#about)
- [Install](#install)
- [Example of JSON](#example-of-json-file-that-can-be-used-to-insertassert-collections)
- [Placeholders](#placeholders)
- [Spring](#placeholders)

## About

Mongomery - is a simple library that will help to insert data into MongoDB and assert MongoDB state after test

## Install

Mongomery is deployed to Maven Central so setup is pretty simple.

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

## Example of JSON file that can be used to insert/assert collections

```json
{
  "collection_name_1": [
    {
      "field1_object1": 1,
      "field2_object1": "string",
      "field3_object1": {
        "sub_field": false
      }
    }, {
      "field1_object2": 1,
      "field2_object2": "string",
      "field3_object2": {
        "sub_field": false
      }
    }
  ]
}
```

## Placeholders

MongoDB doen't allow you to use fields with `$` in name. Sometimes it's necessary because of nature how MongoDB stores Date-Time & Long.
That's why Mongomery add something called placeholders that can be used to insert/assert data in JSON files

There is two types of Placeholders: insert & assert placeholders. More info about placeholders you can find [here](placeholders.md)

## Mongomery Spring wrapper

Mongomery also has it's own Spring wrapper that can be used in Spring & Spring Boot tests. Documentation can be found [here](spring.md)