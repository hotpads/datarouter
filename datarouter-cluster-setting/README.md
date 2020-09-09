# datarouter-cluster-setting

## About

The Cluster Setting module allows you to configure settings with default values in the code and override those values
through the web UI at runtime.  Overrides are stored in the `ClusterSetting` table.  The "cluster" naming refers to the 
fact that a group of webapp instances will be controlled by a single, shared `ClusterSetting` table.

With a service-oriented architecture, the cluster generally refers to a group of instances of the
same webapp, where the fleet of services will have a ClusterSetting table for each service.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-cluster-setting</artifactId>
	<version>0.0.48</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addJobPlugin(new DatarouterClusterSettingPluginBuilder(...)
      ...
      .build()
```

## Concepts

#### Scopes

Several scopes are defined in the `ClusterSettingScope` enum.  From most specific to least specific:

- `serverName`
  - a specific server like `prod-hello-world-3`
- `serverType`
  - a datarouter `ServerType` like `exampleWeb`, `exampleJob`, or `exampleJoblet`
- `environmentName`
  - for example, a specific staging environment like `stg-team3`, `stg-candidate` or `stg-hotfix`, or a production
  environment like `prod-east-1`, or `prod-west-1`
- `environmentType`
  - usually `production`, `staging`, or `development`, where there may be multiple staging or even production 
  environments
- `default`
  - the global default - the final fallback if nothing more specific is found

#### Read-oriented

Each running instance may find a different value for each setting.  It's important to understand that each instance
will go searching for the most specific scope that applies to it.  When resolving a setting, it will use a getMulti
to select the 4 potential database overrides (application, serverName, serverType, and default), and combine them with
the 6 potential code overrides

It then works backwards from `serverName` scope to `default`, prioritizing database over code:

- `serverName/database`
- `serverName/code`
- `serverType/database`
- `serverType/code`
- `environmentName/code`
- `environmentType/code`
- `default/database`
- `default/code`

The first value present will be selected.

The flexibility means it can take some thinking to choose the right scope, for
example if there is a `serverType` setting in the code then overriding it in the database will require specifying
at least a `serverType` scope.  The `default` scope will not replace it.

There's no option to override the `environmentName` nor `environmentType` defaults in the 
database because all instances of the webapp in a running cluster should share the same scopes.

## Features

#### Caching

The `CachedSetting` class transparently wraps the result of a call to the database, avoiding subsequent database calls 
for 15 seconds. It's therefore OK to reference setting values in most loops, however consider capturing the setting 
value in a local variable before entering a very fast, short-lived loop to avoid thread synchronization overhead.

#### Emails

Each time a database setting override is edited, an email will be sent to the application administrators so they are
aware of what is changing.  While not as proactive as a code review, it may help catch configuration errors in
production.

#### Logs

Each time a database setting override is edited, a `ClusterSettingLog` entry is saved for future reference.  You can 
view the setting logs in the UI if it becomes helpful for future debugging.

#### Configuration Alerting

In some scenarios, an application can accumulate many settings over time, due to things like feature flags, if they're
not pruned.  A company with many services and many developers may enable the developers to run any of the services in 
their development environment and expect them to work similarly between developers.  And a company may choose to run any
number of staging environments that may need custom configuration for some subset of the settings.

In those scenarios, it becomes untenable to keep database setting overrides up to date manually via the UI.  The
`ConfigurationScanReportEmailJob` can be run in production to email the application administrators alerting them of
default values that can be moved from the database into the code.  Maintaining default values in the code centralizes
them, can automatically apply them to all developer machines and staging environments, and allows them to be passed
through more formal code reviews.

The job sends an email identifying database overrides with any of these problems:

- `redundant`
  - values can be deleted from the database because the in-code fallback value is the same
- `old`
  - values should be added as in-code defaults and become redundant after the next deploy
- `expired`
  - settings exist in the database but don't have corresponding code that will read them, so can be deleted from the
  database
- `unknown`
  - even the `SettingRoot` isn't recognized, potentially indicating the setting belongs to a different webapp
- `invalidServerName`
  - the datarouter-webapp-instance system indicates this server is no longer providing a heartbeat, so the setting can
  likely be deleted
- `invalidServerType`
  - the db setting has an unknown server type value, likely left after a server type decommission. Server
  types are stored in one of the `ServerTypes` implementations.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
