---
layout: default
title: Mongomery
---

## Table of Contents

- [About](#about)
- [Install](#install)
- [Example of JSON](#example-of-json-file-that-can-be-used-to-insertassert-collections)
- [Placeholders](#placeholders)
  - [Insert placeholders](#insert-placeholders)
    - [`$insertLocalDateTime()`](#insertlocaldatetimeyyyy-mm-ddthhmmss-to-insert-javatimelocaldatetime)
    - [`$insertLocalDate()`](#insertlocaldateyyyy-mm-dd-to-insert-javatimelocaldate)
    - [`$insertLong()`](#insertlong1242-to-insert-long)
  - [Assert placeholders](#assert-placeholders)
    - [`$anyObject()`](#anyobject---asserts-that-field-is-of-type-object-with-any-number-of-fields)
    - [`$anyObject() with number of fields`](#anyobject23---assert-that-field-is-of-object-type-and-has-specific-amount-of-direct-inner-fields)
    - [`$anyString()`](#anystring-assert-that-field-is-of-string-type-with-any-content)
    - [`$anyString(/regex/)` assert string with regex](#anystringregex-assert-that-field-is-of-string-type-and-its-content-match-specific-pattern)
    - [`$anyLongValue()`](#anylongvalue-assert-that-field-if-of-long-type-with-any-value)
    - [`$anyDate()`](#anydate-assert-that-field-if-of-date-time-type-with-any-value)
    - [`$eqLongValue()`](#eqlongvalue123-assert-that-field-of-long-type-and-it-has-specific-value)
    - [`$eqLocalDateTimeValue()`](#eqlocaldatetimevalueyyyy-mm-ddthhmmss-assert-that-date-time-field-has-specific-value)
    - [`$eqLocalDateValue()`](#eqlocaldatevalueyyyy-mm-dd-assert-that-date-field-has-specific-value)

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

There is two types of Placeholders: insert & assert placeholders.

#### Insert placeholders

##### `$insertLocalDateTime(yyyy-MM-ddTHH:mm:ss)` to insert `java.time.LocalDateTime`
```json
{
  "TestCollection": [
    {
      "stringField": "some value",
      "localDateTimeField": "$insertLocalDateTime(2020-02-02T11:11:11)"
    }
  ]
}
```


##### `$insertLocalDate(yyyy-MM-dd)` to insert `java.time.LocalDate`
```json
{
  "TestCollection": [
    {
      "localDate": "$insertLocalDate(2222-02-02)"
    }
  ]
}
```

##### `$insertLong(1242)` to insert `long`
```json
{
  "TestCollection": [
    {
      "stringField": "some value",
      "longField": "$insertLong(215345325523512)"
    }
  ]
}
```

#### Assert placeholders

##### `$anyObject()` - asserts that field is of type `object` with any number of fields

assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "objectField": "$anyObject()"
    }
  ]
}
```

then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "objectField": { }
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "objectField": {
        "field": 1
      }
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "objectField": {
        "field": 1,
        "field2": 2
      }
    }
  ]
}
```

test will fail on any of below condition:
- field `objectField` doesn't exists
- field `objectField` is any non-object field(**NOTE:** MongoDB persists long & Date-Time objects as and objects which means that will pass check against `$nayObject()`)

##### `$anyObject(23)` - assert that field is of `object` type and has specific amount of direct inner fields
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "objectField": "$anyObject(2)"
    }
  ]
}
```

then bellow object can pass this assert. **note:** there is no check of fields' names for object
```json
{
  "TestCollection": [
    {
      "objectField": {
        "field": 1,
        "field2": 2
      }
    }
  ]
}
```

##### `$anyString()` assert that field is of `string` type with any content
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "stringField": "$anyString()"
    }
  ]
}
```
then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "stringField": ""
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "stringField": "some long string"
    }
  ]
}
```

##### `$anyString(/regex/)` assert that field is of `string` type and it's content match specific pattern
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "stringField": "$anyString(/.*_endWith/)"
    }
  ]
}
```
then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "stringField": "some long content that _endWith"
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "stringField": "_endWith"
    }
  ]
}
```

##### `$anyLongValue()` assert that field if of `long` type with any value
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "longField": "$anyLongValue()"
    }
  ]
}
```
then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "longField": "$insertLong(123)"
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "longField": {
        "$numberLong": 53242342   // this is how MongoDB stores long 
      }
    }
  ]
}
```

##### `$anyDate()` assert that field if of `Date-Time` type with any value
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "dateField": "$anyDate()"
    }
  ]
}
```
then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "dateField": "$insertLocalDateTime(2222-03-04T05:06:07)"
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "dateField": "$insertLocalDate(2222-03-04)"
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "dateField": {
        "$date": 12512312531231   // this is how MongoDB stores Date-Time objects 
      }
    }
  ]
}
```

##### `$eqLongValue(123)` assert that field of `long` type and it has specific value
 ```json
 {
   "TestCollection": [
     {
       "longField": "$eqLongValue(123)"
     }
   ]
 }
 ```
 then any of the bellow objects will pass assert:
 ```json
 {
   "TestCollection": [
     {
       "longField": "$insertLong(123)"
     }
   ]
 }
 ```
 ```json
 {
   "TestCollection": [
     {
       "longField": {
          "$numberLong": 123 
       }
     }
   ]
 }
 ```

##### `$eqLocalDateTimeValue(yyyy-MM-ddTHH:mm:ss)` assert that `Date-Time` field has specific value
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "dateField": "$eqLocalDateTimeValue(2020-03-04T05:06:07)"
    }
  ]
}
```
then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "dateField": "$insertLocalDateTime(2222-03-04T05:06:07)"
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "dateField": {
        "$date": 12512312531231   // 2020-03-04T05:06:07 in millis. value depends on timezone you use in connection string
      }
    }
  ]
}
```

##### `$eqLocalDateValue(yyyy-MM-dd)` assert that `Date` field has specific value
assert JSON file has following content:
```json
{
  "TestCollection": [
    {
      "dateField": "$eqLocalDateValue(2020-03-04)"
    }
  ]
}
```
then any of the bellow objects will pass assert:
```json
{
  "TestCollection": [
    {
      "dateField": "$insertLocalDate(2222-03-04)"
    }
  ]
}
```
```json
{
  "TestCollection": [
    {
      "dateField": {
        "$date": 12512312531231   // 2020-03-04 in millis. value depends on timezone you use in connection string
      }
    }
  ]
}
```