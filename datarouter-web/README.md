# datarouter-web

datarouter-web is a basic framework to handle HTTP requests.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-web</artifactId>
	<version>0.0.27</version>
</dependency>
```

## Installation with Datarouter

The `DatarouterWebWebappBuilder` provides a simple way to bootstrap your application. It also provides an easy way 
to add and seamlessly integrate other datarouter modules into your application. Most datarouter modules will have a 
`Plugin` configuration which you can add to the WebappBuilder. All plugins come bundled with what's needed for that 
plugin to run, like Daos, AppListeners, nav items, FilterParams, Settings, etc. Other datarouter modules may have 
different types of plugins that bring in more functionality and will have an extension of `DatarouterWebWebappBuilder`, 
but datarouter-web has `BaseWebPlugin`. 

## Usage

### Handler

In datarouter-web, controllers are called handlers. To create a handler, create a class that extends `BaseHandler`. 
In this class, you can have multiple handler methods, each annotated with the `Handler` annotation.
By default, the object returned by the handler method will be serialized to JSON, unless it is a
`io.datarouter.web.handler.mav.Mav`, then it will use a JSP.

```java
import io.datarouter.web.handler.BaseHandler;

public class HelloWorldHandler extends BaseHandler{

	@Handler(defaultHandler = true)
	public String index(){
		return "Hello World";
	}

}
```

### Route set

Handlers are grouped into route sets. Each route set maps to a path prefix that comes after the context path.
Inside the constructor of the route set, you can define multiple routes with the `handle` method. It takes a regex, 
so you can match any subpath. There are helper methods to match a subdirectory, or to match all paths that have 
not been matched yet.

```java
import javax.inject.Singleton;

import io.datarouter.web.dispatcher.BaseRouteSet;

@Singleton
public class ExampleRouteSet extends BaseRouteSet{

	public ExampleRouteSet(){
		super(""); // this route set will match /context-path

		handleOthers(HelloWorldHandler.class); // all requests will go to this handler
	}

}
```

### Dispatcher servlet

Route sets are registered in a dispatcher servlet. The dispatcher servlet is an http servlet that you can add 
to your web.xml file, or to your `WebappConfigBuilder` with `addRouteSet`


### Dependency injection

datarouter-web relies internally on dependency injection. We recommend using Guice, but it is also compatible 
with Spring. With Guice, you can build off the provided `DatarouterWebWebappBuilder` which creates all of the 
necessary bindings for the framework and provides the ability to override any of the defaults. 

Implementations of `DispatcherServlet` and `RouteSet` can be injected and bound as singletons. However, 
`BaseHandler` holds state about the request and should therefore not be a singleton.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
