# Tangram: Json-SQL Api For Datastores

Tangram is a data access framework, providing a SQL-like json interface for operation of different types of datastores.

#### select

```
{
  "select": "table"
  "fields": ["column1", "column2", ...]
  "join": ["join_table"]
  "where": ["column1=value1", "column2=value2", ...]
  "group": ["column1", "column2", ...]
  "order": ["column1", "column2", ...]
}
```

#### insert

```
{
  "insert": "table",
  "values": {
    "column1": value1,
    "column2": value2,
    ...
  }
}
```

#### update

```
{
  "update": "table",
  "values": {
    "column1": value1,
    "column2": value2,
    ...
  }
}
```

#### delete

```
{
  "delete": "table",
  "where": ["column1=value1", "column2=value2", ...]
}
```

## Getting started

Add the library to your project.

### Maven

```
<dependency>
  <groupId>com.github.mengxianun</groupId>
  <artifactId>tangram-jdbc</artifactId>
  <version>x.y.z</version>
</dependency>
```

### Gradle

```
dependencies {
  implementation 'com.github.mengxianun:tangram-jdbc:x.y.z'
}
```

### Config File

Create a configuration file in the classpath directory and configure the data source. The default configuration file name is **air.json**

```
{
    "datasources": {
        "myds": {
            "url": "jdbc:mysql://localhost:3306/tangram",
            "username": "tangram",
            "password": "123456",
            ...
        }
    }
}

```

### Usage

```
Translator translator = new DefaultTranslator();
String json = "{\"select\":\"sys_user\",\"fields\":\"*\",\"where\":\"id=1\"}";
DataResultSet dataResultSet = translator.translate(json);
Object data = dataResultSet.getData();
```

## Documentation

See the detail [~~documentation~~](https://github.com/aigodata/tangram/blob/master/doc/README.md) or [Wiki](https://github.com/aigodata/tangram/wiki).

## License

This project is licensed under the MIT License.