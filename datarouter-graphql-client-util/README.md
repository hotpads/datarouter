# datarouter-graphql-client-util

datarouter-graphql-client-util is a set of utility classes for building graphql queries in client modules. 

# Example

```java
	public static class ExampleQueryBuilder extends GraphQlClientQueryBuilder {
	    public ExampleQueryBuilder(GraphQlRootType root) {
	        super(root, ExampleGraphQlRootType.class);
	    }
	    public ExampleQueryBuilder office(ExampleOfficeGraphQlTypeBuilder builder) {
	        this.fieldWithSubQuery(builder);
	        return this;
	    }
	}

	public static class ExampleOfficeGraphQlTypeBuilder extends GraphQlClientQueryBuilder {
	    public ExampleOfficeGraphQlTypeBuilder(ExampleOfficeGraphQlArgumentType arg) {
	        super("office", arg, ExampleOfficeGraphQlType.class);
	    }
	    public ExampleOfficeGraphQlTypeBuilder location() {
	        this.field("location");
	        return this;
	    }
	    public ExampleOfficeGraphQlTypeBuilder floor(ExampleFloorGraphQlTypeBuilder builder) {
	        this.fieldWithSubQuery(builder);
	        return this;
	    }
	}
	public static class ExampleFloorGraphQlTypeBuilder extends GraphQlClientQueryBuilder {
	    public ExampleFloorGraphQlTypeBuilder() {
	        super("floor", ExampleFloorGraphQlType.class);
	    }
	    public ExampleFloorGraphQlTypeBuilder floorNum() {
	        this.field("floorNum");
	        return this;
	    }
	}
```

You can now use these builders to build a query:

```java
	private static final ExampleFloorGraphQlTypeBuilder FLOOR_QUERY = new ExampleFloorGraphQlTypeBuilder()
		.floorNum();
		
	private static final ExampleOfficeGraphQlTypeBuilder OFFICE_QUERY = new ExampleOfficeGraphQlTypeBuilder(
			new ExampleOfficeGraphQlArgumentType("san francisco"))
			.location()
			.floor(FLOOR_QUERY);
	public static final ExampleQueryBuilder EXAMPLE_OFFICE_QUERY = new ExampleQueryBuilder(GraphQlRootType.QUERY)
			.office(OFFICE_QUERY);
```

If you were to run the **build()** method on **EXAMPLE_OFFICE_QUERY** the output would mirror:

```
query {
	office(location: "san francisco"){
		location
		floor {
			floorNum
		}
	}
}
```

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-graphql</artifactId>
	<version>0.0.125</version>
</dependency>
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
