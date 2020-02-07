![](https://github.com/borsch/mongomery/workflows/Java%20CI/badge.svg)

# mongomery
Simple and useful util for unit and integration testing with mongodb (java 1.8+)

This library allows you easily populate db with predefined data
from a json file and also do assertions about db state using a json file.

    <dependency>
      <groupId>com.github.borsch</groupId>
      <artifactId>mongomery</artifactId>
      <version>wanted-version</version>
    </dependency>
    
## change log
- [0.3.1](https://github.com/borsch/mongomery/releases/tag/mongomery-0.3.1) - 06/02/2020
- [0.3.0](https://github.com/borsch/mongomery/releases/tag/mongomery-0.3.0) - 26/01/2020
- [0.2.0](https://github.com/borsch/mongomery/releases/tag/mongomery-0.2.0) - 12/01/2020 - assert `com.mongodb.client.MongoDatabase` instead of `com.mongodb.DB` (deprecated API)
- [0.1.0](https://github.com/borsch/mongomery/releases/tag/mongomery-0.1.0) - 11/01/2020 - first release

## how to use
Assume you have "predefinedTestData.json" file in you resources folder that looks like this:
```json
{
   "Books": [
    {
      "_id": {
         "$oid": "55f3ed00b1375a40e61830bf"
      },
      "englishTitle": "The Little Prince",
      "originalTitle": "Le Petit Prince",
      "author": "Antoine de Saint-Exupéry"
    }
   ],
   "Movies": [
    {
      "_id": {
        "$oid": "55f3ed00b1375a48e61830bf"
      },
      "name": "Titanic",
      "year": 1997
    }
   ]
}
```

To load all this data in database you need write only two lines of code:

```java
com.mongodb.client.MongoDatabase db = getMyMongoDatabase();
MongoDBTester mongoDBTester = new MongoDBTester(db);
mongoDBTester.setDBState("predefinedTestData.json");
```

To check db state:
```java
mongoDBTester.assertDBStateEquals("expectedTestData.json");
```

## placeholders

Sometimes you don't know exact value that will bee stored in DB(random, time etc.) or you just don't care about what exactly value will be stored. In such cases you can use **placeholders**. It's a kind of pattern matchers.

Assume you have added following object into collection **MyPeople**:
```
{
  "createdAt": LocalDateTime.of(2222, 2, 3),    <- java example of how you pass datetime :)
  "firstName": "Some first name",
  "email": "email@example.com"
}
```

then you can assert this object with following json & placeholders
```
{
  "_id": "$anyObject()",               <- we use autogenerated MongoDb id
  "createdAt": "$anyDate()",           <- we don't care about date, when this person was created
  "firstName": "Some first name",      <- we want to have EXACT match
  "email": "email@example.com"         <- same as firstName
}
```

In this sample we use two placeholders `$anyObject()` & `$anyDate()`.

Available placeholders:
- `$anyObject()` - assert that field is nested object. examples
```
{
  "field1": { },   <- OK
  "field2": { "innerField": 1 },     <- OK
  "field3": { "innectField1": 1, "innerField2": 2 },     <- OK
  "field4": "some string",     <- FAIL
  "field5": 1,    <- FAIL
}
```
- `$anyObject(1)` - same as above but assert number of **dirrect** inner fields.  
example: `{ "someField": "$anyObject(4)" }` - will check that `someField` is an object and has exacty 4 dirrect fields
- `$anyString()` - will pass only when field is of string type
- `$anyString(/regex/)` - check that field is string and and content of this field match passed pattern. example
assume you have persisted following object `{ "field": "My supper long string with date inside: 2222-02-02." }`
this will pass against following patterns:
```
{
  "field": "$anyString(/^My supper long string.*/)"      <- assert start with
}
{
  "field": "$anyString(/.*2222-02-02.*/)"                <- assert contains text inside
}
```
following pattern will not pass because string doesn't match given pattern
```
{
  "field": "$anyString(/.*not exapecte content.*/)"      <- fails
}
```
- `$anyLongValue()` - assert that fields contains any long value. there problem here is that MongoDB stores long in following way
```json
{
  "longValueField": {
     "$numberLong": 125265462536734536345345
  }
}
```
- `$eqLongValue(23525235)` - assert that field has exact long value
- `$anyDate()` - assert that field has any timestamp value

## fields ignore
Sometimes you want to ignore some fields during mathing process. For example if you use autogenerate IDs then it's quite hard to predict what value will be generated. In such cases you can exclude those fields from mathing process:


```java
com.mongodb.client.MongoDatabase db = getMyMongoDatabase();
MongoDBTester mongoDBTester = new MongoDBTester(db);
mongoDBTester.setDBState("predefinedTestData.json");
mongoDBTester.addIgnorePaths("_id", "field1", "field2");    <- ignore all this fields while match objects
mongoDBTester.assertDBStateEquals("expectedTestData.json");
```
