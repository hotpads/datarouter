# datarouter-webapp-instance

## About

Each webapp contains a WebappInstance table which allows each running webapp to publish its latest state to a central
place.  The publishing job is effectively a heartbeat, scheduled every 20 seconds.  Viewing the web UI lets you see
all instances in the cluster with information like IP address or build time, and identify instances that haven't been
deployed in a while.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-webapp-instance</artifactId>
	<version>0.0.46</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterWebappInstancePluginBuilder(...)
		...
		.build()
```

## Features

#### WebappInstanceLog

A WebappInstanceLog table has a more specific PrimaryKey resulting in a new record for each time the instance is started.
This can help track when each version of the code was deployed.

#### Stale Webapp Alerts

For teams that manually deploy their code and want to ensure it doesn't fall too far behind their main branch, a
WebappInstanceAlertJob will email the administrators when it finds any instances older than some configured time.  The
default alert threshold is set to 7 days.

#### OneTimeLoginToken

Some webapps have an authentication system that is tied to the domain name pointed at a load balancer and prevents
logging in when visiting a particular instance hostname or IP address.  The OneTimeLoginToken functionality allows 
the authentication system to insert a secure token into the database that will authenticate the user when they visit
the instance directly.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
