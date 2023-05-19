# datarouter-gcp-pubsub

## About

datarouter-gcp-pubsub provides a user-friendly client for sending databeans through GCP's Pub/Sub API. Google Cloud 
Pub/Sub is a fully managed, real-time messaging service that allows sending and receiving messages between applications.
It provides reliable, many-to-many, asynchronous messaging between applications.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-gcp-pubsub</artifactId>
	<version>0.0.120</version>
</dependency>
```
## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addPlugin(new DatarouterGcpPubsubPluginBuilder())
```

## Queue Creation and Namespacing

When an application starts and initializes the datarouter-gcp-pubsub client, it will compare the registered nodes (in 
Java) with existing queues (in Google Cloud Pub/Sub) and create any queues that are missing.  Because multiple 
environments (production, staging, development, etc) may be sharing a GCP account, the queue names are 
prefixed with a namespace.  The default queue naming is `[environment]-[serviceName]-[databeanName]`, where environment 
is provided by the `DatarouterProperties` class.

A development queue could have a name like `matt-laptop-petstore-Pet`, while the production queue's name would be
`production-petstore-Pet`.

## Queue Types

Datarouter provides two queue abstractions:

### QueueStorage

When issuing a putMulti, QueueStorage will create one Google Cloud Pub/Sub message per databean.  This is appropriate 
when databeans are independent of each other and you want to isolate processing errors for each databean.  While it is
simple and reliable, it is the more expensive option.

### GroupQueueStorage

When issuing a putMulti, GroupQueueStorage will bundle multiple databeans into a single message, up to Pub/Sub's limit 
of 10MB per message.  This is a more economical format, and will usually provide higher throughput.  It lets you pass 
many small databeans through Google Cloud Pub/Sub without worrying about custom serialization formats.  The downside 
is that it's less clear how to handle a single invalid databean in a message, but many use cases are ok with this.


## Local Testing
To build this module locally, add `gcp-pubsub.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
