# Tangram

JSON操作数据库

## 快速开始

config (air.json)

```
{
    // 数据源
    "datasources": {
        "mysql": {
            "url": "jdbc:mysql://localhost:3306/tangram",
            "username": "tangram",
            "password": "123456",
            "initialSize": 1,
            "minIdle": 1,
            "maxActive": 20,
            "maxWait": 60000,
            "timeBetweenEvictionRunsMillis": 60000,
            "minEvictableIdleTimeMillis": 300000,
            "validationQuery": "SELECT 'x'",
            "testWhileIdle": true,
            "testOnBorrow": false,
            "testOnReturn": false,
            "poolPreparedStatements": false,
            "filters": "stat"
        }
    }
}

```

java

```
Translator translator = new DefaultTranslator(configJsonPath);
DataResultSet dataResultSet = translator.translate(requestJson);
if (dataResultSet.succeed()) {
	Object data = dataResultSet.getData();
	// 操作...
}
```

request

```
{
	"detail":"sys_user", // key:操作类型, value:数据库表
	"fields":["*"], // 操作字段
	"where":"id=1"  // 操作条件
}
```

response

```
[
	{
		"id": 1,
		"name": "小明",
		"username": "xiaoming",
		"weight": 120.9,
		"create_time": "Jan 1, 2019 10:43:45 PM",
		"birth": "三月 1, 2019",
		"sex": 2,
		"detail": [
			"red",
			"blue"
		],
		"dead": false,
		"hobby_id": 1
	}
]
```



## JSON文档

### 使用

Translator.translate方法为tangram引擎的唯一入口，接收json格式的请求字符串，返回json格式的结果。

### 配置

#### air.json

引擎主配置文件，结构

```
{
	// 数据源
	"datasource":{
		"url":"jdbc:mysql://localhost:3306/tangram", // url
		"username":"root", // 用户名
		"password":"123456", // 密码
		// ... other properties
	},
	// 原生SQL支持
	"native_enabled":false, // 默认false
	// 数据库表配置文件路径
	"tables_config_path":"tables" // 默认classpath下的tables文件夹
}
```

暂时只有datasource属性

## 请求JSON结构文档

### Type&Table

#### **说明**

​	操作类型：数据库表名 | 数据库表名 + 空格 + 数据库表别名

#### 类型格式

​	detail：详细，单个结果

​	query：查询，多个结果

​	select：查询，同query

​	insert：插入

​	update：更新

​	delete：删除

##### 示例

```
{
    "query":"user" // 或者"user u"
}	
```

### Fields

#### **说明**

​	操作字段，支持函数。该属性可以省略，如果不指定，则默认查询所有字段

#### 类型格式

​	数组：

```
{
    "field":["field1", "field2",...]
}
```

​	字符串：

```
{
    "field":"field_name"
}
```

#### 示例

```
{
  "fields":["username", "age"]
}
```

#### 示例2

```
{
  "fields":["count(*)"]
}
```



### Values

#### **说明**

​	插入或更新的数据，对应insert，update

#### 类型格式

```
{
    "values":{
        "field1":value1,
        "field2":value2,
        ...
    }
}
```

#### 示例

```
{
  "values":{
  	"id":1,
    "username":"li",
    "age":20
  }
}
```

### Join

#### 说明

​	多表关联Join操作，需要在配置文件中预先配置表的关联关系。

​	数据库表支持别名，数据库表名 + 空格 + 数据库表别名

#### 类型格式

​	数组

```
{
    "join":[
        {
            连接类型:连接表名
        },
        {
            连接类型:连接表名
        }
        ...
    ]
}
```

​	如果只有一个关联表的情况下，可以使用对象形式

```
{
    "join":{
    	连接类型:连接表名
    }
}
```

​	如果关联关系是left，则left可以省略，简写为字符串形式

```
{
    "join":"连接表名"
}
```

#### 示例

```
{
    "join":[
        {
            "left":"user_info"
        },
        {
            "right":"student_info"
        },
        "code_sex"
    ]
}
```

### Where

#### **说明**

​	sql中的where查询条件

#### 类型格式

​	[condition1, condition2...]

​	condition格式为{连接符：条件}，连接符包括and和or

​	条件可以有多种格式

​		字符串：单个条件，字段+运算符+值。如 "a=1"

​		数组：多个条件，需要括号包裹

​	当连接符为and时，condition的连接符可以省略。即{连接符：条件} -> 条件

​	如果想指定值为字符类型，需要用单引号包裹，如"a='1'"

#### 规则

##### 运算符

​	等于：=

​	不等于：!=

​	大于：>

​	大于等于：>=

​	小于：<

​	小于等于：<=

​	in：, 逗号拼接多个值

​	between：~ 两边都包含边界，如果想要更精确的范围，请使用大于小于运算符

​	like：%=

#### 示例

```
"where":[
  "age=30", // age = 30
  "age!=30", // age <> 30
  "age>30", // age > 30
  "age<30", // age < 30
  "age>=30", // >= 30
  "age<=30", // age <= 30
  "age=30~40", // age between 30 and 40
  "username%=%ao%", // like '%ao%'
  "username=zhangsan,zhaosi", // username in ('zhangsan','zhaosi')
  "username!=zhangsan,zhaosi" // username not in ('zhangsan','zhaosi')
]
```
##### 连接符

​	and：{"and":"a=1"}  可简写成 ”a=1“

​	or：{"or":"a=1"}

​	{"and":"a=1"}或{"or":"a=1"}是条件的最小单位。对象中的value不可以再是多个条件。如果需要多个条件，可以写多个对象

​	sql中表达多个条件组合成的条件时通常用（）包裹，这里用[]表示sql中的（），将多个条件放在同一数组中

#### 示例

**sql**:

```
where sex = 'male'
	and character = 'optimism'
	or height = 1.8
	and (job != 'student' or hobby = 'football' or hobby = 'pingpang')
	or (age >= 25 and age <= 40)
	or age between 25 40
```

**json**:

```
"where":[
  "sex=male",
  "character=optimism",
  {"or":"height=1.8"},
  ["job!=student", {"or":"hobby=football"}, {"or":"hobby=pingpang"}],
  {"or":"age=25~40"}
]
```

### Group

#### **说明**

​	sql中的group分组查询

#### 类型格式

​	[field1, field2...]

​	注：只有一个字段的时候可以直接写成字符串

#### 示例

```
"group":["username", "age"] // group by username, age
```

### Order

#### **说明**

​	sql中的order排序

#### 类型格式

​	数组类型，每个数组元素是一个字段，空格，升序（asc）/降序（desc）连接的字符串。

​	注：只有一个字段的时候可以直接写成字符串

#### 示例

```
"order":["age desc", "username asc"] // order by age desc, username asc
```

**简写形式**：字段前面加上+，-符号。+代表升序，-代表降序

```
"order":["-age", "+username"] // order by age desc, username asc
```

### Limit

#### **说明**

​	sql中的分页查询

#### 类型格式

​	[开始行数, 结束行数]

#### 示例

**json**：

```
"limit":[6, 10]
```

### Transaction

#### 说明

​	事务操作，由 transaction 节点包裹多个JSON操作

#### 类型格式

```
{
	"transaction":[
		request_json1,
		request_json2
	]
}
```

#### 示例

```
{
	"transaction":[
		{
			"insert":"person",
			"values":{
				"name":"li",
				"age":22
			}
		},
		{
			"insert":"person",
			"values":{
				"name":"lei",
				"age":25,
				"phone":"19986523332"
			}
		}
	]
}
```

### Native

#### **说明**

​	自定义sql语句查询

#### 类型格式

​	字符串

#### 示例

```
{"native":"select * from user"}
```



## 数据库表配置

​	tables_config_path路径下的json文件，文件名任意，默认为表名。

### 属性

#### table (未实现)

​	实际数据库表名称，字符串类型。不配置时则以文件名为表名

#### columns

​	数据库表的列，JSON对象，内部元素为 {列：列配置} 的键值对

##### 	列配置

###### 		association：关联表信息，JSON对象

```
"user_id": {
	"association": {
    	"target_table": "user",
        "target_column": "id",
        "type": "1_n"
   }
}
```

###### ignore：列忽略，Boolean类型。返回数据不包含该列

```
"password": {
	"ignore": true
}
```



### 示例

sys_user.json

```
{
	"columns": {
		"password": {
			"ignore": true
		},
		"user_id": {
			"association": {
                "target_table": "user",
                "target_column": "id",
                "type": "1_n"
            }
		}
	}
}
```

