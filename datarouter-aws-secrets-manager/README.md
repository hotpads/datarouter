# datarouter-aws-secrets-manager

## About

This is a small module that provides the AWS Secrets Manager client implementation/concrete class of `SecretClient`/`BaseSecretClient` from datarouter-secret. It also provides a `SecretClientSupplier` implementation that returns the AWS client for non-development and `LocalStorageSecretClient` for development.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-aws-secrets-manager</artifactId>
	<version>0.0.38</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterAwsSecretsPluginBuilder(...)
	.build()
```

## Usage

See datarouter-secret's README for usage. This module only provides the AWS client implementation and helpers.

`DatarouterAwsCredentials` allows separate credentials for production and non-production usage of AWS Secrets Manager. Proper AWS user/policy/role configuration is necessary for these to be useful.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
