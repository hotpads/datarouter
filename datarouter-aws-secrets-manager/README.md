# datarouter-aws-secrets-manager

## About

This is a small module that provides the AWS Secrets Manager client implementation of `BaseSecretClient` and `SecretClientSupplier` from datarouter-secret.

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-aws-secrets-manager</artifactId>
	<version>0.0.56</version>
</dependency>
```

## Installation with Datarouter

You can install this module by adding its plugin to the `WebappBuilder`.

```java
.addWebPlugin(new DatarouterAwsSecretsManagerPluginBuilder(...)
	.build()
```

## Usage

See datarouter-secret's README for usage. This module only provides the AWS client implementation and helpers.

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
