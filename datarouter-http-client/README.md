# datarouter-http-client

datarouter-http-client wraps Apache HTTP Client and adds a JSON serialization layer.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-http-client</artifactId>
	<version>0.0.126</version>
</dependency>
```

## Usage

### Client configuration

First, you need to create a `DatarouterHttpClient` using the builder. There are multiple options available, here is a sample:

```java
DatarouterHttpClient httpClient = new DatarouterHttpClientBuilder(new GsonJsonSerializer(new Gson()))
		// Retry the requests twice
		.setRetryCount(() -> 2)
		// Add apiKey=SECRET to each request
		.setApiKeySupplier(() -> "SECRET")
		// Change the maximum number of connections in the pool	
		.setMaxConnectionsPerRoute(100)
		.setMaxTotalConnections(100)
		// Ignore invalid SSL certificates
		.setIgnoreSsl(true)
		// Use a custom JSON serializer
		.setJsonSerializer(jsonSerializer)
		.build();
```

### Making a request

To make a request, you need to create a `DatarouterHttpRequest` and submit it to your `DatarouterHttpClient`.

#### Example with a GET param
```java
// reuse this client
DatarouterHttpClient client = new DatarouterHttpClientBuilder().build();

DatarouterHttpRequest request = new DatarouterHttpRequest(
		HttpRequestMethod.GET, 
		"https://example.com/api")
		.addGetParam("id", "1"); // Passing a GET parameter
DatarouterHttpResponse response = client.execute(request);
String stringResult = response.getEntity();
```

#### Example with a POST param
```java
// reuse this client
DatarouterHttpClient client = new DatarouterHttpClientBuilder().build();

DatarouterHttpRequest request = new DatarouterHttpRequest(
		HttpRequestMethod.POST, 
		"https://example.com/api")
		.addPostParam("id", "1"); // Passing a POST parameter
DatarouterHttpResponse response = client.execute(request);
String stringResult = response.getEntity();
```

#### Example with a JSON body
```java
public class BodyExample{
	public final int id;

	public BodyExample(int id){
		this.id = id;
	}
}
```
```java
// reuse this client
DatarouterHttpClient client = new DatarouterHttpClientBuilder().build();

DatarouterHttpRequest request = new DatarouterHttpRequest(
		HttpRequestMethod.POST, 
		"https://example.com/api");
client.setEntityDto(request, new BodyExample(1)); // Sets the body or entity for the request. Can be any object serializable in JSON.
DatarouterHttpResponse response = client.execute(request);
String stringResult = response.getEntity();
```

### JSON deserialization

If your target API returns JSON, you can write Java beans and the HTTP client will deserialize it for you.

```java
public static class ExampleDataTransferObject{
	public Long id;
	public String title;
}

public static void main(String[] args){
	DatarouterHttpRequest request = new DatarouterHttpRequest(
			HttpRequestMethod.GET, 
			"https://example.com/api")
			.addGetParam("id", "1");
	DatarouterHttpClient client = new DatarouterHttpClientBuilder().build();
	ExampleDataTransferObject dto = client.execute(request, ExampleDataTransferObject.class);
}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
