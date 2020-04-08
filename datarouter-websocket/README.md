# datarouter-websocket

## About
Datarouter-websocket helps setting up websocket in a tomcat environment, managing open websocket connections and
 dispatching messages across a cluster of servers.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-websocket</artifactId>
	<version>0.0.26</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuidlder`.

```java
.addJobPlugin(new DatarouterWebSocketPluginBuilder(...)
		.build())
```

Add a class that extends the GuiceWebSocketConfig for the injector to find.
```java
public static class ExampleWebSocketConfig extends GuiceWebSocketConfig{
}
```

## Usage

An implementation of `WebSocketService` is responsible for handling the incoming websocket message. It can have
 injected objects. A `WebSocketService` is mapped to a name, and all the messages starting with `name|` will get
 dispatched to that `WebSocketService`. A new instance of `WebSocketService` is provisioned for each connection.
 A `WebSocketService` will also receive a callback when the websocket connection is closed.

The user owning the websocket connection is identified through the `UserTokenRetriever`. Messages can be sent to that
 user with `PushService` forward methods.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
