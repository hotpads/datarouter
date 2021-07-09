# datarouter-exception

## About
datarouter-exception is a tool to monitor and view exceptions

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-exception</artifactId>
	<version>0.0.81</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterExceptionPluginBuilder(...)
		.build()
```

## Features

### Record Exceptions

Exceptions are recorded with the `ExceptionRecorder`, which is heavily used throughout datarouter. `ExceptionRecorder`
 can be used anywhere, and is automatically added for all jobs and handlers. Each time an exception occurs it's logged
 and saved in the `ExceptionRecord` table with a TTL of 2 weeks. 

The `ExceptionHandlingFilter` catches exceptions for http requests and saves them in the `HttpRequestRecord` table with
 the associated exception id.


### Browse Exceptions

All the exceptions that are caught are aggregated by exception type and location over the last hour and saved in the
 `ExceptionRecordSummary` table. The summaries are displayed in the "Browse Exceptions" page on the datatrouter UI.

Each summary shows the following, sorted by count.
* `Type` - The type of exception 
* `Location` - Where the exception occurred
* `Issue` (editable) - A tracking number for third party services like JIRA
* `Count` - The number of summaries exceptions with the same type and location
* `Mute` (editable) - If the summary has been muted or not.
* `Details` - View the exception details. This could be any exception record with the same type and location.


### Exception Details

You can view the details of any exception by searching for the exceptionRecordId or clicking on the details link from
 the BrowseExceptions page.

The details page shows all the data collected for the specified exception:

 * Server Name
 * Code Version
 * Stack Trace
 * Location (class canonical name)
 * Call origin

If there is an associated http request with the exception, than a record is saved in the HttpRequestRecord table and
 the following is also displayed on the details page.
 * Request url
 * Parameters
 * Body
 
 Client options
 * Client Ip address
 * Cookies
 * Header


### Custom Handling

Datarouter provides an option to configure the handling and recording of exceptions. The default implementations are
 `DefaultExceptionHandlingConfig` and `DefaultExceptionRecorder` but can be overridden in the PluginBuilder.
Exceptions can also be posted to third party services with an implementation of the `ExceptionRecordPublisher` and
 adding `enablePublishing(...)` to the PluginBuilder.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
