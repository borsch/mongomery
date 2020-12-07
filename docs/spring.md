---
layout: default
title: Spring Wrapper
---

## Table of Contents

- [About](#about)
- [Install](#install)
- [Usage](#usage)
    - [Basic example](#basic-example)
    - [Embedded MongoDB server](#embedded-mongodb-server)
    - [Embedded MongoDB server timezone](#timezone)

## About

[Mongomery Spring](https://github.com/borsch/mongomery-spring) - wrapper for Spring and Spring Boot tests that gives and annotation based configuration. 
There is also an example of [Spring tests](https://github.com/borsch/mongomery-spring/tree/master/mongomery-spring-example) and [Spring Boot tests](https://github.com/borsch/mongomery-spring/tree/master/mongomery-spring-boot-example)

## Install

Mongomery Spring is deployed to Maven Central so setup is pretty simple.

For Maven projects add

```xml
<dependency>
    <groupId>com.github.borsch</groupId>
    <artifactId>mongomery-spring</artifactId>
    <version>X.X.X</version>
</dependency>

```

For Gradle projects add
```groovy
compile group: 'com.github.borsch', name: 'mongomery-spring', version: 'X.X.X'
```

Latest version can be found [here](https://mvnrepository.com/artifact/com.github.borsch/mongomery-spring)

### Usage

#### Basic example

```java
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ 
    // .. other listeners ..
    MongomeryExecutionListener.class 
})
class MyTestClass {

    @DatabaseMongoSetup("databaseSetup.json")           // pre-setup MongoDB state
    @ExpectedMongoDatabase("expectedAfterSave.json")    // asserts MongoDB state after test
    public void test() {
        // .. test code ..
    }

}
```

#### Embedded MongoDB server

You can also setup tests to run against embedded database

```xml
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo</artifactId>
    <version>version</version>
    <scope>test</scope>
</dependency>
```

```java
@Bean
MongoClient mongoClient() {
    try {
        MongodForTestsFactory factory = MongodForTestsFactory.with(Version.Main.V3_3);
        return factory.newMongo();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

#### TimeZone
For Embedded database work of `LocalDateTime` & `LocalDate` highly depends on TimeZone. To set `UTC` timezone do the following for tests
```java
TimeZone.setDefault(TimeZone.getTimeZone("UTC")); 
```




