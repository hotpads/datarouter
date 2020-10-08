/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.secret.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.client.local.LocalStorageSecretClient.LocalStorageDefaultSecretValues;

/**
 * This is the recommended interface for accessing {@link Secret}s that need to be written to, and namespacing is
 * automatically applied before and after all interactions with secret names. Except for when a caller switches from one
 * environmentType or service name to another, there is no need to update or migrate secret names.
 */
@Singleton
public class SecretService{

	@Inject
	private LocalStorageDefaultSecretValues localStorageDefaultSecretValues;
	@Inject
	private SecretClientSupplier secretClientSupplier;
	@Inject
	private SecretJsonSerializer jsonSerializer;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private SecretOpRecorderSupplier secretOpRecorderSupplier;

	public List<String> listSecretNames(){
		return listSecretNames(Optional.empty());
	}

	public List<String> listSecretNames(Optional<String> exclusivePrefix){
		return listSecretNamesInternal(exclusivePrefix, secretNamespacer.getAppNamespace());
	}

	public List<String> listSecretNameSuffixes(String exclusivePrefix){
		return removePrefixes(listSecretNames(Optional.of(exclusivePrefix)), exclusivePrefix);
	}

	public List<String> listSecretNamesShared(){
		return listSecretNamesInternal(Optional.empty(), secretNamespacer.getSharedNamespace());
	}

	private List<String> listSecretNamesInternal(Optional<String> exclusivePrefix, String namespace){
		Optional<String> newPrefix = Optional.of(namespace + exclusivePrefix.orElse(""));
		return removePrefixes(secretClientSupplier.get().listNames(newPrefix), namespace);
	}

	public <T> T read(Supplier<String> secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRaw(secretName.get(), reason), secretClass);
	}

	public <T> T read(String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRaw(secretName, reason), secretClass);
	}

	public <T> T readShared(Supplier<String> secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRawShared(secretName.get(), reason), secretClass);
	}

	public <T> T readShared(String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRawShared(secretName, reason), secretClass);
	}

	//skips the recording step. this is necessary if the recorder uses the same DB that the secret is needed to init.
	public <T> T readSharedWithoutRecord(String secretName, Class<T> secretClass){
		String namespace = secretNamespacer.getSharedNamespace();
		String result = secretClientSupplier.get().read(namespace + secretName).getValue();
		return deserialize(result, secretClass);
	}

	public String readRaw(String secretName, SecretOpReason reason){
		return readRawInternal(secretNamespacer.getAppNamespace(), secretName, reason);
	}

	public String readRawShared(String secretName, SecretOpReason reason){
		return readRawInternal(secretNamespacer.getSharedNamespace(), secretName, reason);
	}

	private String readRawInternal(String namespace, String secretName, SecretOpReason reason){
		record(SecretOp.READ, namespace, secretName, reason);
		return secretClientSupplier.get().read(namespace + secretName).getValue();
	}

	public <T> void create(String secretName, T secretValue, SecretOpReason reason){
		createRaw(secretName, serialize(secretValue), reason);
	}

	public void create(String secretName, String value, Class<?> secretClass, SecretOpReason reason){
		deserialize(value, secretClass);//generate an exception if this doesn't work
		createRaw(secretName, value, reason);
	}

	public void createRaw(String secretName, String serializedValue, SecretOpReason reason){
		record(SecretOp.CREATE, secretNamespacer.getAppNamespace(), secretName, reason);
		secretClientSupplier.get().create(secretNamespacer.appNamespaced(secretName), serializedValue);
	}

	public <T> void update(String secretName, T secretValue, SecretOpReason reason){
		updateRaw(secretName, serialize(secretValue), reason);
	}

	public void updateRaw(String secretName, String serializedValue, SecretOpReason reason){
		record(SecretOp.UPDATE, secretNamespacer.getAppNamespace(), secretName, reason);
		secretClientSupplier.get().update(secretNamespacer.appNamespaced(secretName), serializedValue);
	}

	public <T> void put(Supplier<String> secretName, T secretValue, SecretOpReason reason){
		put(secretName.get(), secretValue, reason);
	}

	public <T> void put(String secretName, T secretValue, SecretOpReason reason){
		record(SecretOp.PUT, secretNamespacer.getAppNamespace(), secretName, reason);
		SecretClient secretClient = secretClientSupplier.get();
		secretName = secretNamespacer.appNamespaced(secretName);
		try{
			secretClient.createQuiet(secretName, serialize(secretValue));
		}catch(RuntimeException e){
			secretClient.update(secretName, serialize(secretValue));
		}
	}

	public void delete(String secretName, SecretOpReason reason){
		record(SecretOp.DELETE, secretNamespacer.getAppNamespace(), secretName, reason);
		secretClientSupplier.get().delete(secretNamespacer.appNamespaced(secretName));
	}

	public final <T> void registerDevelopmentDefaultValue(Supplier<String> secretName, T defaultValue,
			boolean isShared){
		String namespaced = isShared ? secretNamespacer.sharedNamespaced(secretName.get()) : secretNamespacer
				.appNamespaced(secretName.get());
		if(secretNamespacer.isDevelopment()){
			localStorageDefaultSecretValues.registerDefaultValue(namespaced, serialize(defaultValue));
		}
	}

	private void record(SecretOp op, String namespace, String secretName, SecretOpReason reason){
		secretOpRecorderSupplier.get().recordOp(namespace, secretName, op, reason);
	}

	private <T> String serialize(T value){
		return jsonSerializer.serialize(value);
	}

	private <T> T deserialize(String value, Class<T> targetClass){
		return jsonSerializer.deserialize(value, targetClass);
	}

	private static List<String> removePrefixes(List<String> strings, String prefix){
		return Scanner.of(strings).map(string -> string.substring(prefix.length())).list();
	}

}
