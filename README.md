# Tangram: Json-SQL Api For Datastores

Tangram is a data access framework, providing a SQL-like json interface for operation of different types of datastores.

## Getting started

Add the library to your project.

### Maven

```
<dependency>
  <groupId>com.github.mengxianun</groupId>
  <artifactId>tangram-jdbc</artifactId>
  <version>1.6.0</version>
</dependency>
```

### Gradle

```
dependencies {
  implementation 'com.github.mengxianun:tangram-jdbc:1.6.0'
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

See the detail [documentation](https://github.com/aigodata/tangram/blob/master/doc/README.md) or [Wiki](https://github.com/aigodata/tangram/wiki).

## License

This project is licensed under the MIT License.