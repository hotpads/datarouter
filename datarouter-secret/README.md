# datarouter-secret

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-secret</artifactId>
	<version>0.0.42</version>
</dependency>
```

## Installation with Datarouter

You can build this module as below. In the future a simple plugin configurer will be able to accept the module.

```java
new DatarouterSecretPluginBuilder()
		//...
		.build()
```

## About

This module provides a CRUD interface and various convenient wrappers for interacting with `Secret`s, represented by a
name and serialized `String` value.

For production use, you must implement a `SecretClient` and corresponding `SecretClientSupplier`. These low-level
clients allow all CRUD operations with no extra features (namespacing, recording ops, etc.).

`BaseSecretClient` is a recommended abstract implementation that adds counting and automated exception logging, so that
exceptions will be recorded, even if they are later caught elsewhere without incident. The only concrete implementation
provided is for convenience purposes in local development: `LocalStorageSecretClient`. This stores secrets in the
file system in a plaintext properties file.

`SecretService` and especially `CachedSecretFactory` are the recommended ways to programmatically access `Secret`s,
because they provide extra layers of convenience and are more audit-friendly. `SecretService` automatically namespaces
and records every operation based on `SecretNamespacer` and `SecretOpRecorder` implementations. `CachedSecretFactory`
builds `CachedSecret`s for improved performance of frequently read `Secret`s, like API keys.

## Usage

```java
package io.datarouter.secret.readme;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.secret.service.SecretOpReason;
import io.datarouter.secret.service.SecretService;

@Singleton
public class DatarouterSecretExample{

	//formatting doesn't really matter as long as the underlying implementation allows it (SecretClient#validateName)
	private static final String SECRET_NAME = "examples/example-secret";

	@Inject
	private SecretClientSupplier secretClientSupplier;
	@Inject
	private SecretService secretService;
	@Inject
	private CachedSecretFactory cachedSecretFactory;

	//SecretOpReason will be recorded using the configured SecretOpRecorder implementation
	public void copyAppSecretToSharedSecret(){
		String appSpecificSecret = secretService.read(SECRET_NAME, String.class, SecretOpReason.automatedOp(
				"a job might do this"));
		secretService.put(SECRET_NAME, appSpecificSecret, SecretOpReason.automatedOp("a job might do this"));
	}

	//NOTE: typically, CachedSecrets end up as private final fields that are built in a constructor.
	//There is no reason to use a CachedSecret in a local variable, since it will probably only be read once.
	public void readCachedSecrets(){
		//the provided default value will only appear in development
		CachedSecret<String> appSpecificSecret = cachedSecretFactory.cacheSecret(() -> SECRET_NAME, String.class,
				"development");
		CachedSecret<String> sharedSecret = cachedSecretFactory.cacheSharedSecretString(() -> SECRET_NAME);

		appSpecificSecret.get();
		sharedSecret.get();
	}

	public void beDangerousWithSecretClient(){
		SecretClient secretClient = secretClientSupplier.get();
		//namespacing and auditing not provided
		secretClient.delete("development/example-app/" + SECRET_NAME);
	}

}
```

## License

This library is licensed under the Apache License, Version 2.0 - see [LICENSE](../LICENSE) for details.
