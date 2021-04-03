# datarouter-path-node

datarouter-path-node is a simple object that can be used for building tree-like structures.
 A `PathNode` can be either a `leaf` or a `branch`. Leaves just hold a string value. Branches
  have a string value and class of child PathNodes.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-path-node</artifactId>
	<version>0.0.67</version>
</dependency>
```

## Example

PathNodes can be used to define any type of tree structure. Two useful examples are:
* Define api endpoints. These classes can use the `PathsRoot` marker interface.
* Define file system directories. These classes can use the `FilesRoot` marker interface.

```java
@Singleton
public static class TestPaths extends PathNode{

	public final ApiPaths api = branch(ApiPaths::new, "api");

	public static class ApiPaths extends PathNode{
		public final V1Paths v1 = branch(V1Paths::new, "v1");
		public final V2Paths v2 = branch(V2Paths::new, "v2");
	}

	public static class V1Paths extends PathNode{
		public final PathNode example = leaf("example");
	}

	public static class V2Paths extends PathNode{
		public final PathNode example = leaf("example");
		public final PathNode newExample = leaf("newExample");
	}

}
```

```java
// Call statically
public class Example{

	private static final TestPaths PATHS = new TestPaths();

	public void example(){
		String output = PATHS.api.v1.exampleEndpoint.toSlashedString();
		System.out.println(output); // prints (without quotations) "/api/v1/exampleEndpoint"
	}

}

// Injectable with framework injectors like Guice
public class Example{

	@Inject
	private TestPaths paths;

	public void example(){
		String output = paths.api.v1.exampleEndpoint.toSlashedString();
		System.out.println(output); // prints (without quotations) "/api/v1/exampleEndpoint"
	}

}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
