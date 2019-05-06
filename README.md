# jdbc-stream
[![Build Status](https://travis-ci.org/juliomarcopineda/jdbc-stream.svg?branch=master)](https://travis-ci.org/juliomarcopineda/jdbc-stream)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.juliomarcopineda/jdbc-stream/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.juliomarcopineda/jdbc-stream)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Light-weight library to wrap JDBC ResultSet to Java 8 Stream

# Overview
jdbc-stream is a light-weight convenience library that wraps any JDBC ResultSet into a Java 8 Stream to take advantage of the Stream API and functional programming in Java.

## Add jdbc-stream to your build
jdbc-stream can be found at MavenCentral with group ID `com.github.juliomarcopineda` and artifact ID `jdbc-stream`.

#### Maven

```xml
<dependency>
  <groupId>com.github.juliomarcopineda</groupId>
  <artifactId>jdbc-stream</artifactId>
  <version>0.1.0</version>
</dependency>
```

#### Gradle (Groovy DSL)

```groovy
implementation 'com.github.juliomarcopineda:jdbc-stream:0.1.0'
```

#### Gradle (Kotlin DSL)

```kotlin
compile("com.github.juliomarcopineda:jdbc-stream:0.1.0")
```

## Example Usage
For this example, we will assume that we have a SQLite instance with the [Iris Data Set](https://archive.ics.uci.edu/ml/datasets/iris) in the table `iris` with the following schema:

```sql
CREATE TABLE iris (SepalLength real, 
                   SepalWidth real, 
                   PetalLength real, 
                   PetalWidth real, 
                   IrisClass varchar(255))
```

#### 1) Create a connection to our SQL database. 
For this example we will use an in-memory SQLite server and the `java.sql` library

```java
Connection conn = DriverManager.getConnection("jdbc:sqlite::memory")
```

#### 2) Execute SQL query to get ResultSet

```java
String sql = "SELECT * FROM iris"
PreparedStatement ps = conn.prepareStatement(sql)
ResultSet resultSet = ps.executeQuery()
```

#### 3) Use jdbc-stream library to utilize Java 8 Stream API
The following are limited examples of what you can do with jdbc-stream and Java 8 Stream API

##### a) Get number of records from SQL query

```java
long count = JdbcStream.stream(resultSet).count()
```

##### b) Get average Sepal Length of all Iris records

```java
double averageSepalLength = JdbcStream.stream(resultSet)
  .mapToDouble(rs -> {
    double sepalLength = 0;
    try {
      sepalLength = rs.getDouble("SepalLength");
    }
    catch (SQLException e) {
      // Handle exception
    }
    return sepalLength;
  })
  .average()
  .getAsDouble();
```

##### c) Get Iris species with the widest Petal Width

- Get `Map<String, List<Double>>` where each `key` is the Iris species and each `value` is the `List<Double>` of all their possible petal widths

```java
Map<String, List<Double>> petalWidths = JdbcStream.stream(resultSet)
  .map(rs -> {
    String irisClass = "";
    double petalWidth = 0;
    try {
      irisClass = rs.getString("IrisClass");
      petalWidth = rs.getDouble("PetalWidth");
    }
    catch (SQLException e) {
    	// Handle exception
    }
    return new AbstractMap.SimpleEntry<>(irisClass, petalWidth);
    })
  .collect(Collectors.toMap(Map.Entry::getKey,
                            e -> {
                              List<Double> list = new ArrayList<>();
                              list.add(e.getValue());
                              return list;
                              },
                            (l1, l2) -> Stream.of(l1, l2)
                                          .flatMap(Collection::stream)
                                          .collect(Collectors.toList())))
```
- Iterate over entries of the Map to get the Iris species with the widest petal

```java
String irisWidestPetal = petalWidths.entrySet()
  .stream()
  .max((e1, e2) -> Collections.max(e1.getValue()) > Collections.max(e2.getValue()) ? 1 : -1)
  .get()
  .getKey()
```

##### d) Use a custom Mapper from ResultSet to client-defined Java class
jdbc-stream can handle the mapping between ResultSet rows to a Java class as long as the client provides a Mapper. This Mapper must extend the Mapper interface that is included in the library.

Example usage of this Mapper as follows:

- First let us define a Sepal class.

```java
public class Sepal {
  private int width;
  private int length;
  
  public double getArea() {
    return this.width * this.length;
  }
  
  // getters and setters
}
```

- Then, let's define a Mapper from ResultSet row to a Sepal.

```java
public class SepalMapper<Sepal> implements Mapper<Sepal> {
  
  @Override
  public Sepal map(ResultSet resultSet) {
    Sepal sepal = new Sepal();
    
    try {
      sepal.setWidth(resultSet.get(SepalWidth));
      sepal.setLength(resultSet.get(SepalLength));
    }
    catch (SQLException e) {
      // Handle exception
    }
  }
}
```

- Now, we can use both the ResultSet and the custom Mapper to take advantage of Java Stream API. Suppose we want to get the average sepal area:

```java
SepalMapper mapper = new SepalMapper();

double averageSepalArea = JdbcStream(resultSet, mapper)
  .map(Sepal::getArea)
  .mapToDouble(i -> i)
  .average()
  .getAsDouble();
```

- Note: we could have used an anonymous class to define the custom mapper as follows:

```java
Mapper<Sepal> mapper = new Mapper<Sepal>() {
  @Override
  public Sepal map(ResultSet resultSet) {
    Sepal sepal = new Sepal();
    
    try {
      sepal.setWidth(resultSet.get(SepalWidth));
      sepal.setLength(resultSet.get(SepalLength));
    }
    catch (SQLException e) {
      // Handle exception
    }
  }
}
```
