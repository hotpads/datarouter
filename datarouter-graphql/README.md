# datarouter-graphql

GraphQL is a query language and specification for defining APIs. 
With GraphQL you use schema to describe your API, and you can query that API dynamically as opposed to statically in the REST world. 
The benefits of using GraphQL include less round trips to the server from the client, smaller response sizes, and faster responses depending on what is queried. 
Datarouter-graphql is a library that can be used to easily create GraphQL APIs in java web applications. 
It’s built on top of the open source graphql-java and integrates with datarouter-web, an RPC framework. 
Datarouter-graphql includes standards for defining API schema and logic for how to execute the queries through datafetchers.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-graphql</artifactId>
	<version>0.0.127</version>
</dependency>
```
## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addPlugin(new DatarouterGraphQlPlugin())
```

## Setting up a GraphQL API

In order to build a GraphQL api we extend the GraphQlBaseHandler class. 

```java
public class ExampleGraphQlHandler extends GraphQlBaseHandler{
	@Handler
	public ExecutionResult example(@RequestBody ExecutionInput query){
		return execute(query);
	}
	@Override
	public Class<? extends GraphQlType> getRootType(){
		return ExampleGraphQlRootType.class;
	}
	@Override
	public Class<? extends GraphQlFetcherRegistry> fetcherRegistry(){
		return ExampleGraphQlFetcherRegistry.class;
	}
}
```

We define apis in the same manner we would in regular [datarouter-web](https://github.com/hotpads/datarouter/tree/master/datarouter-web#handler) handlers by using the @Handler annotation. 
The **execute** method is inherited from the GraphQlBaseHandler class and will run the incoming query on the GraphQL engine. 
By default, fields in the query will be resolved asynchronously. 
Returning an **ExecutionResult** object from your handler will ensure that the response of your GraphQL api will be serialized in the correct format.  

Central to the idea of GraphQL is the schema file which describes the api input and output formats. 
We take a code first schema generation approach where the schema gets described through custom **GraphQlType** classes. 
When configuring a datafetcher on a field, the return type of the datafetcher will have to match the type of the field. 
The benefit of this approach is code remains in sync with the schema because any changes in schema will have to be reflected in what gets returned by datafetchers.

Extending the GraphQlBaseHandler will force you to have to override two methods getRootType and fetcherRegistry. 

We use the **Ql** annotation class on the root type to specify the type of operation. 
We can also use the fetcherId argument. 
Below is the implementation for ExampleGraphQlFetcherRegistry, ExampleGraphQlRootType and child classes.

```java
public class ExampleGraphQlFetcherRegistry extends GraphQlFetcherRegistry{
	public ExampleGraphQlFetcherRegistry(){
		bind(ExampleGraphQlFetcherIdentifiers.OFFICE, ExampleOfficeDataFetcher.class);
	}
}
```

```java
public class ExampleGraphQlRootType implements GraphQlType {
    @Ql(root = GraphQlRootType.QUERY, fetcherId = ExampleGraphQlFetcherIdentifiers.OFFICE)
    public final ExampleOfficeGraphQlType office;

    public ExampleGraphQlRootType(ExampleOfficeGraphQlType office) {
        this.office = office;
    }
}

public class ExampleOfficeGraphQlType implements GraphQlType {
    public final String location;
    public final ExampleFloorGraphQlType floor;

    public ExampleOfficeGraphQlType(String location, ExampleFloorGraphQlType floor) {
        this.location = location;
        this.floor = floor;
    }
}

public class ExampleFloorGraphQlType implements GraphQlType {
    public final Integer floorNum;
    public final List<ExampleConferenceRoomGraphQlType> conferenceRooms;

    public ExampleFloorGraphQlType(Integer floorNum, List<ExampleConferenceRoomGraphQlType> conferenceRooms) {
        this.floorNum = floorNum;
        this.conferenceRooms = conferenceRooms;
    }
}

public class ExampleConferenceRoomGraphQlType implements GraphQlType {
    @Ql(required = true, description = "Name of the meeting room")
    public final String name;
    @Ql(required = true, description = "Availability of the room")
    public Boolean availability;

    public ExampleConferenceRoomGraphQlType(String name, boolean availability) {
        this.name = name;
        this.availability = availability;
    }
}
```
The equivalent schema for this would be: 

```
type Query {
  office(location: String): ExampleOfficeGraphQlType
}

type ExampleOfficeGraphQlType {
  location: String
  floor: ExampleFloorGraphQlType
}

type ExampleFloorGraphQlType {
  floorNum: Int
  conferenceRooms: [ExampleConferenceRoomGraphQlType]
}

type ExampleConferenceRoomGraphQlType {
  # Name of the meeting room
  name: String!
  # Availability of the room
  availability: Boolean!
}
```

The ExampleOfficeDataFetcher class would look like this: 

```java
public class ExampleOfficeDataFetcher
extends BaseDataFetcher<ExampleOfficeGraphQlType,ExampleOfficeGraphQlArgumentType>{
	@Override
	public GraphQlResultDto<ExampleOfficeGraphQlType> getData(){
		String location = args.location;
		if(location == null){
			return GraphQlResultDto.withError(GraphQlErrorDto.invalidInput("location cannot be null!"));
		}
		return GraphQlResultDto.withData(ExampleQlData.placeToOffice.get(location));
	}
}
```
Note that arguments on the field are a parameterized type of the BaseDataFetcher class. 
Our library will deserialize the arguments passed in by the caller of the api to an instance of the argument type class, and you can access the argument by using the args parameter. 
If there is no argument on a field with a custom fetcher you can use the EmptyGraphQlArgumentType to designate this.

## N + 1 Problem and Dataloader

In the GraphQL world, the N + 1 problem is an issue that arises when resolving a list field which requires additional fetching for each elements child fields. 
In this example, you’d make a single DB call to fetch the data for the parent field, and then for every child field you’d have to make an additional DB call which equates to N + 1 calls, where N is the number of items in the list. 
This structure can lead to redundant database calls, database degradation for really large queries, and poor performance.
To solve this problem, the graphQl community introduced the dataloader library which acts as a batching layer between datafetchers and the data access layer. 
Datarouter-graphql has support for batching with dataloader. 
In order to utilize the capabilities, we use a custom base datafetcher class called **BaseDataLoaderFetcher**. 
An implementation of BaseDataLoaderFetcher looks like this: 


```java
public class ExampleTeamsDataFetcher
extends BaseDataLoaderFetcher<List<ExampleTeamGraphQlType>, EmptyGraphQlArgumentType, ExampleTeamsKey>{
	@Override
	public ExampleTeamsKey buildLoaderKey(){
		ExampleOrgGraphQlType org = environment.getSource();
		return new ExampleTeamsKey(org.orgName);
	}
	@Override
	public Class<? extends DatarouterBatchLoader<ExampleTeamsKey,List<ExampleTeamGraphQlType>>> getBatchLoaderClass(){
		return ExampleTeamsBatchLoader.class;
	}
}
```

Here you will have to override 2 methods:

**buildLoaderKey()**: this method returns a key object which will be used for batching. You need to describe the current field being resolved. 

**getBatchLoaderClass()**: this method returns the class which holds the logic for fetching a batch of key objects retrieved from the buildLoaderKey() method. 

An implementation of the loader would look like this: 

```java
@Singleton
public class ExampleTeamsBatchLoader
implements DatarouterBatchLoader<ExampleTeamsKey,List<ExampleTeamGraphQlType>>{
	@Override
	public Map<ExampleTeamsKey,GraphQlResultDto<List<ExampleTeamGraphQlType>>> load(Set<ExampleTeamsKey> keys){
		return Scanner.of(keys)
				.toMap(Function.identity(), right -> GraphQlResultDto.withData(
						ExampleQlData.orgsToTeams.getOrDefault(right.orgName, List.of())));
	}
}

```

Here we override the load method, which takes a set of keys crafted from the datafetcher and holds logic for fetching them. 
You can imagine a batched database call to fetch data for all the keys at a single time.  

##Testing
When using datarouter-graphql, you will also get access to the graphql playground. 
This can be accessed through the docs -> GraphQL Playground link in the toolbar on the top of the home page of your service. 

From there, you’ll have a playground you can use to query your apis. 
You can add a query parameter of trace=true if you want to get information about how your query is being resolved. 
There will also be a visualization that will show up in the tracing section of the playground. 

In addition, we have an integration test called DatarouterGraphQlSchemaIntegrationService that will attempt to catch errors in your apis during building. 
It will test if the GraphQlType classes have the correct field types, make sure datafetchers are properly registered, in addition to other housekeeping to make sure we’re catching issues early. 


## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
