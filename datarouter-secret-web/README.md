# datarouter-secret-web

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-secret-web</artifactId>
	<version>0.0.95</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterSecretWebPluginBuilderImpl(...)
		...
		.build()
```

## About

This module adds features and implementations to datarouter-secret, using datarouter-storage and datarouter-web. Many
of these are defaulted in `DatarouterSecretWebPluginBuilder`

`DatarouterSecretOpRecordDao` simply records `SecretOp`s into a storage node.

`SecretHandler` is the recommended way to manually access `Secret`s. It is a simple CRUD UI that takes advantage of all
the built-in features of `SecretService`. A `SecretHandlerPermissions` implementation is required to grant permissions
for real use, but `DefaultSecretHandlerPermissions`, which grants all permissions outside of production, is provided.

`DatarouterPropertiesAndServiceSecretNamespacer` is a useful implementation of `SecretNamespacer` with the following
behavior: for "app-specific" `Secret`s, the namespace is "\<environment type\>/\<service name\>/", and for "shared"
`Secret`s, the namespace is "\<environment type\>/shared/". Environment type is determined by
`DatarouterProperties#getEnvironmentType`, and service name is determined by `DatarouterService#getName`.

## Usage

See datarouter-secret.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
