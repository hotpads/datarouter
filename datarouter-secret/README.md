# datarouter-secret

## Installation with Maven

```xml
<dependency>
	<groupId>io.datarouter</groupId>
	<artifactId>datarouter-secret</artifactId>
	<version>0.0.103</version>
</dependency>
```

## Installation with Datarouter

You can build this module as below.

```java
new DatarouterSecretPluginBuilderImpl()
		//.setSecretClientConfigHolder() default LocalStorageSecretClientSupplier for all ops
		//.setSecretNamespacer() default EmptyNamespacer.class
		//.setSecretOpRecorderSupplier() default NoOpSecretOpRecorderSupplier.class
		//.setJsonSerializer() default GsonToolJsonSerializer.class
		//.setLocalStorageConfig() default DefaultLocalStorageConfig.class
		//.setDefaultMemorySecrets() default DefaultMemorySecrets()
		.build()
```

## About

This module provides a CRUD interface and various convenient wrappers for interacting with `Secret`s, represented by a
name and serialized `String` value.

For production use, you must implement a `SecretClient` and corresponding `SecretClientSupplier`. These low-level
clients allow all CRUD operations with no extra features (namespacing, recording ops, etc.).

`BaseSecretClient` is a recommended abstract implementation that adds counting and automated exception logging, so that
exceptions will be recorded, even if they are later caught elsewhere without incident. Provided implementations are  
`LocalStorageSecretClient`, which stores secrets in the file system in a plaintext properties file, and
`MemorySecretClient`, which stores secrets in memory and is not persistent but is useful for testing. The easiest way to
directly use these is by injecting their respective `SecretClientSupplier`s, but this is not recommended (see below.).

`SecretService` and especially `CachedSecretFactory` are the recommended ways to programmatically access `Secret`s,
because they provide extra layers of convenience and are more audit-friendly. `SecretService` automatically namespaces
and records every operation based on `SecretNamespacer` and `SecretOpRecorder` (configured with
`SecretOpRecorderSupplier`) implementations. `CachedSecretFactory` builds `CachedSecret`s for improved performance of
frequently read `Secret`s, like API keys.

Usage of these requires configuration of the `SecretClientConfigHolder`. This, combined with, `SecretClientConfig` can
be used to configure multiple implementations/instances (from various `SecretClientSupplier`s) and choose which to use
for reading, writing, etc. Another `MemorySecretClient` implementation that depends on this configuration can be
supplied with `InjectedDefaultMemorySecretClientSupplier` (This is initialized with the configured
`DefaultMemorySecrets`.).

Note on `SecretOpRecorder`: if the implementation relies on reading a secret to initialize itself, then that read
operation must either use a `SecretClient` method directly or use `SecretService#readSharedWithoutRecord`. Otherwise it
will be impossible to initialize the recorder because it will be stuck recursively trying to fetch the secret and record
fetching the secret.

## Usage

```java
package io.datarouter.secret.readme;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.secret.service.SecretService;

@Singleton
public class DatarouterSecretExample{

	//formatting doesn't really matter as long as the underlying implementation allows it (SecretClient#validateName)
	private static final String SECRET_NAME = "examples/example-secret";

	@Inject
	private MemorySecretClientSupplier secretClientSupplier;
	@Inject
	private SecretService secretService;
	@Inject
	private CachedSecretFactory cachedSecretFactory;

	//SecretOpReason will be recorded using the configured SecretOpRecorder implementation
	public void copySharedSecretToAppSecret(){
		String sharedSecret = secretService.readShared(SECRET_NAME, String.class, SecretOpReason.automatedOp(
				"a job might do this"));
		secretService.put(SECRET_NAME, sharedSecret, SecretOpReason.automatedOp("a job might do this"));
	}

	//NOTE: typically, CachedSecrets end up as private final fields that are built in a constructor.
	//There is no reason to use a CachedSecret in a local variable, since it will probably only be read once.
	public void readCachedSecrets(){
		//the provided default value will be returned in development, if no client returns a value
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
