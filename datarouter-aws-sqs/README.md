# datarouter-aws-sqs

## About

datarouter-aws-sqs provides a user-friendly client for sending databeans through Amazon's SQS (Simple Queue Service). 
SQS is a hosted messaging service with high availability and elasticity and predictable pricing.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-aws-sqs</artifactId>
	<version>0.0.92</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterSqsPlugin())
```

## Queue Creation and Namespacing

When an application starts and initializes the datarotuer-aws-sqs client, it will compare the registered nodes (in java)
with existing queues (in SQS) and create any queues that are missing.  Because multiple environments, 
(production, staging, development, etc) may be sharing an AWS account or region, the queue names are prefixed with a
namespace.  The default queue naming is `[environment]-[serviceName]-[databeanName]`, where environment is provided
by the `DatarouterProperties` class.

A development queue could have a name like `matt-laptop-petstore-Pet`, while the production queue's name would be
`production-petstore-Pet`.

## Queue Types

Datarouter provides two queue abstractions:

### QueueStorage

When issuing a putMulti, QueueStorage will create one SQS message per databean.  This is appropriate when databeans are
independent of each other and you want to isolate processing errors for each databean.  While it is simple and reliable, 
it is the more expensive option.

### GroupQueueStorage

When issuing a putMulti, GroupQueueStorage will bundle multiple databeans into a single message, up to SQS's limit of 
256KB per message.  This is a more economical format, and will usually provide higher throughput.  It lets you pass many
small databeans through SQS without worrying about custom serialization formats.  The downside is that it's less clear 
how to handle a single invalid databean in a message, but many use cases are ok with this.


## Local Testing
To build this module locally, add `aws-sqs.properties` to `/etc/datarouter/test`.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
