# datarouter-mysql

datarouter-mysql is an implementation of [datarouter-storage](../datarouter-storage) that allows you to connect to a MySQL database.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-mysql</artifactId>
	<version>0.0.8</version>
</dependency>
```

## Usage

### Primary Key

This class represents the primary key of the MySQL table. Datarouter will define a `PRIMARY KEY` with the columns defined in this class.

```java
public class TestDatabeanKey extends BasePrimaryKey<TestDatabeanKey>{

	private String id;

	public static class FieldKeys{
		public static final StringFieldKey id = new StringFieldKey("id");
	}

	public TestDatabeanKey(){
	}

	public TestDatabeanKey(String id){
		this.id = id;
	}

	@Override
	public List<Field<?>> getFields(){ // the list of columns of the PRIMARY KEY
		return Arrays.asList(new StringField(FieldKeys.id, id));
	}

}
```

You can define an `AUTO_INCREMENT` key column by specifying a `FieldGeneratorType` with value `MANAGED` on the field key.

```java
public static final UInt63FieldKey id = new UInt63FieldKey("id")
		.withFieldGeneratorType(FieldGeneratorType.MANAGED);
```

When using composite primary keys, the ordering of the `getFields` method matters. The columns in the primary key will follow the same order.


### Databean

This class represents a MySQL table. Each instance will be a row of that table. Besides its primary key, this one defines an `INT` column called `someInt`.

```java
public class TestDatabean extends BaseDatabean<TestDatabeanKey,TestDatabean>{

	private TestDatabeanKey key;
	private Integer someInt;

	private static class FieldKeys{
		private static final IntegerFieldKey someInt = new IntegerFieldKey("someInt");
	}

	public TestDatabean(){
		this.key = new TestDatabeanKey(); // it is required to initialize the key field of a databean
	}

	public TestDatabean(String id, Integer someInt){
		this.key = new TestDatabeanKey(id);
		this.someInt = someInt;
	}

	public static class TestDatabeanFielder extends BaseDatabeanFielder<TestDatabeanKey,TestDatabean>{

		protected TestDatabeanFielder(){
			super(TestDatabeanKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TestDatabean databean){
			return Arrays.asList(
					new IntegerField(FieldKeys.someInt, databean.someInt));
		}

	}

	@Override
	public Class<TestDatabeanKey> getKeyClass(){
		return TestDatabeanKey.class;
	}

	@Override
	public TestDatabeanKey getKey(){
		return key;
	}

	public Integer getSomeInt(){
		return someInt;
	}

}
```

Datarouter will generate the following `CREATE TABLE` statement for such a databean.

```sql
create table testDatabase.TestDatabean (
 someInt int(11),
 id varchar(255) not null,
 primary key (id)
)engine INNODB, character set utf8mb4, collate utf8mb4_bin, comment 'created by test-server [2018-11-26T11:38:14.538-08:00]', row_format Dynamic;
```


### Router

Now that we have a databean class, we can create a node that will allow us to perform database queries. In the following router, we define a simple node.
The node is configured to use a database client called `mysqlClient`. We will use a configuration file to tell datarouter how to connect this client to the database.

```java
@Singleton
public class TestRouter extends BaseRouter{

	private static final ClientId MYSQL_CLIENT = new ClientId("mysqlClient", true);

	public final SortedMapStorage<TestDatabeanKey,TestDatabean> node;

	@Inject
	public TestRouter(Datarouter datarouter, DatarouterProperties datarouterProperties, NodeFactory nodeFactory,
			DatarouterSettings datarouterSettings){
		super(datarouter, datarouterProperties, "test", nodeFactory, datarouterSettings);

		node = createAndRegister(MYSQL_CLIENT, TestDatabean::new, TestDatabeanFielder::new);
	}

}
```

### Dependency injection configuration

For this example, we will be using Guice to inject dependencies. It's also possible to use other dependency injectors like Spring.

```java
public class TestGuiceModule extends BaseModule{

	@Override
	protected void configure(){
		// install the bindings of datarouter-storage
		install(new DatarouterStorageGuiceModule());
		// bind the standard codec factory - you can create your own if you want to define your own field types
		bindOptional(MysqlFieldCodecFactory.class).setDefault().to(StandardMysqlFieldCodecFactory.class);
		// datarouter will use the application's name to look for configuration files
		bind(DatarouterProperties.class).toInstance(new SimpleDatarouterProperties("testApp"));
		// we register all the routers of our application here
		bind(RouterClasses.class).toInstance(new RouterClasses(TestRouter.class));
	}

}
```

### Configuration files

#### Client configuration

The following configuration file tells datarouter that the `mysqlClient` client defined in the router is a MySQL client that needs to connect to `localhost:3306/testDatabase`.
This file needs to be located in `/etc/datarouter/config/datarouter-testApp.properties`.

```
client.mysqlClient.type=mysql
client.mysqlClient.url=localhost:3306/testDatabase
client.mysqlClient.user=user
client.mysqlClient.password=password
```

#### Schema update configuration

Datarouter can create databases, tables and keep the schema up-to-date with what is defined in the code. 
To activate it, you will have to add this file at `/etc/datarouter/config/schema-update.properties`.

```
schemaUpdate.enable=true
schemaUpdate.execute.addColumns=true
schemaUpdate.execute.deleteColumns=true
schemaUpdate.execute.modifyColumns=true
schemaUpdate.execute.addIndexes=true
schemaUpdate.execute.dropIndexes=true
schemaUpdate.execute.modifyEngine=true
schemaUpdate.execute.modifyCharacterSetOrCollation=true
schemaUpdate.execute.modifyRowFormat=true
```

On production environments, it is recommended to use `schemaUpdate.print` instead of `schemaUpdate.execute`. The ALTER TABLE statements will be logged and emailed instead of executed.

### Application code

We have everything we need to start writing application code and database queries. 
The following main method will start the framework, write a databean to the MySQL table, and then read it.

```java
public static void main(String[] args){
	// create the Injector with our test module
	Injector injector = Guice.createInjector(Arrays.asList(new TestGuiceModule()));
	// get an instance of our router with the injector
	TestRouter router = injector.getInstance(TestRouter.class);
	// instantiate a databean
	Integer someInt = ThreadLocalRandom.current().nextInt();
	TestDatabean databean = new TestDatabean("foo", someInt);
	// write the databean to the database, will issue an INSERT ... ON DUPLICATE KEY UPDATE by default
	router.node.put(databean, null);
	// other put behaviors are available with PutMethod, this one will issue an INSERT IGNORE
	router.node.put(databean, new Config().setPutMethod(PutMethod.INSERT_IGNORE));
	// read the databean using the same primary key
	TestDatabean roundTripped = router.node.get(new TestDatabeanKey("foo"), null);
	// check that we were able to read the someInt column
	Assert.assertEquals(roundTripped.getSomeInt(), someInt);
	// databeans are equal if their keys are equal, they also sort by primary key
	Assert.assertEquals(roundTripped, databean);
	// let's put another databean, with a different key
	Integer anotherInt = ThreadLocalRandom.current().nextInt();
	TestDatabean anotherDatabean = new TestDatabean("bar", anotherInt);
	router.node.put(anotherDatabean, null);
	// you can fetch the rows given a range of primary keys, here, we fetch everything
	long sum = router.node.stream(Range.everything(), null) // built-in Java 8 stream
			.mapToInt(TestDatabean::getSomeInt)
			.sum();
	Assert.assertEquals(sum, someInt + anotherInt);
}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
