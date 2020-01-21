[![CircleCI](https://circleci.com/gh/circleci/circleci-docs/tree/teesloane-patch-5.svg?style=svg)](https://circleci.com/gh/borsch/mongomery/tree/master)

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
- 0.2.0 - 12/01/2020 - assert `com.mongodb.client.MongoDatabase` instead of `com.mongodb.DB` (deprecated API)
- 0.1.0 - 11/01/2020 - first release

## how to use
Assume you have "predefinedTestData.json" file in you resources folder that looks like this:

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

To load all this data in database you need write only two lines of code:

    //db here is a com.mongodb.DB instance
    MongoDBTester mongoDBTester = new MongoDBTester(db);
    mongoDBTester.setDBState("predefinedTestData.json");

To check db state:

    mongoDBTester.assertDBStateEquals("expectedTestData.json");

There is two ways to write json files with expected data:

1. <b>Strict match.</b> This is usual json file like you have seen above.
In most cases you don't need more than exact describing of db state after test.

2. <b>Pattern match.</b> If you want to use random strings in your test or for example your business
logic generates random ids for entities you may want a little more than strict match:


    {
      "Movies": [
        {
          "_id": "$anyObject()",
          "name": "Titanic",
          "year": 1997
        }
      ]
    }

json above says that test expects one document in "Movies" collection that will have name Titanic and
a year 1997. Also it must have non null field _id with any object in it.

<pre>
Currently available next special functions:<br/>
<b>$anyObject()</b> - placeholder for any non null object
<b>$anyObject(int)</b> - placeholder for any non null object which has exactly "int" number of fields. ("int" here is any positive integer) <br/>
<b>$anyString()</b> - placeholder for any non null string
<b>$anyString(/regex/)</b> - placeholder for any non null string which matches with given regex. ("regex" here is a valid regular expression)
</pre>

<br/>
TODO: <br/>
1. Write more useful tests<br/>
2. Refactor and optimize it<br/>
3. Add better error messages for pattern match.<br/> 
