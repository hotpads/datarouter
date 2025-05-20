# datarouter-changelog

Datarouter-changelog is a tool that records changes or user actions, which can be viewed on a simple UI.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-changelog</artifactId>
	<version>0.0.127</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addPlugin(new DatarouterChangelogPluginBuilder(...)
		...
		.build()
```

## Usage

Datarouter already records many changes or actions by default, but additional ones can be recorded by using the `ChangelogRecorder`.

```java
@Inject
private ChangelogRecorder changelogRecorder;

public void someMethod(){
	// ...
	changelogRecorder.record("changelogType", "name", "action", "username", "userToken", "comment");
}
```
Changelogs can also be published to third party services.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
