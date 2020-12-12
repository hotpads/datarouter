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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.config.SecretClientConfig;
import io.datarouter.secret.config.SecretClientConfigHolder;
import io.datarouter.secret.exception.NoConfiguredSecretClientSupplierException;
import io.datarouter.secret.exception.SecretClientException;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.SecretOpInfo;
import io.datarouter.secret.op.SecretOpReason;

/**
 * This is the recommended interface for accessing {@link Secret}s that need to be written to, and namespacing is
 * automatically applied before and after all interactions with secret names. Except for when a caller switches from one
 * environmentType or service name to another, there is no need to update or migrate secret names.
 */
@Singleton
public class SecretService{
	private static final Logger logger = LoggerFactory.getLogger(SecretService.class);

	@Inject
	private SecretClientHelper helper;
	@Inject
	private SecretJsonSerializer jsonSerializer;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private SecretOpRecorderSupplier secretOpRecorderSupplier;

	public List<String> listSecretNames(SecretOpReason reason){
		return listSecretNames(Optional.empty(), reason);
	}

	public List<String> listSecretNames(Optional<String> exclusivePrefix, SecretOpReason reason){
		return listSecretNamesInternal(exclusivePrefix, secretNamespacer.getAppNamespace(), reason);
	}

	public List<String> listSecretNameSuffixes(String exclusivePrefix, SecretOpReason reason){
		return removePrefixes(listSecretNames(Optional.of(exclusivePrefix), reason), exclusivePrefix);
	}

	public List<String> listSecretNamesShared(SecretOpReason reason){
		return listSecretNamesInternal(Optional.empty(), secretNamespacer.getSharedNamespace(), reason);
	}

	//TODO list names for EVERY supplier that allows LIST, instead of just the first that allows it
	private List<String> listSecretNamesInternal(Optional<String> exclusivePrefix, String namespace,
			SecretOpReason reason){
		Optional<String> namespacedPrefix = Optional.of(namespace + exclusivePrefix.orElse(""));
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.LIST, namespace, "", reason);
		return removePrefixes(helper.apply(opInfo, client -> client.listNames(namespacedPrefix)), namespace);
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
	public <T> T readSharedWithoutRecord(String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRawSharedWithoutRecord(secretName, reason), secretClass);
	}

	//skips the recording step. this is necessary if the recorder uses the same DB that the secret is needed to init.
	public String readRawSharedWithoutRecord(String secretName, SecretOpReason reason){
		String namespace = secretNamespacer.getSharedNamespace();
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.READ, namespace, secretName, reason);
		return helper.apply(opInfo, client -> client.read(namespace + secretName).getValue());
	}

	public String readRaw(String secretName, SecretOpReason reason){
		return readRawInternal(secretNamespacer.getAppNamespace(), secretName, reason);
	}

	public String readRawShared(String secretName, SecretOpReason reason){
		return readRawInternal(secretNamespacer.getSharedNamespace(), secretName, reason);
	}

	private String readRawInternal(String namespace, String secretName, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.READ, namespace, secretName, reason);
		record(opInfo);
		return helper.apply(opInfo, client -> client.read(namespace + secretName).getValue());
	}

	public <T> void create(String secretName, T secretValue, SecretOpReason reason){
		createRaw(secretName, serialize(secretValue), reason);
	}

	public void create(String secretName, String value, Class<?> secretClass, SecretOpReason reason){
		deserialize(value, secretClass);//generate an exception if this doesn't work
		createRaw(secretName, value, reason);
	}

	public void createRaw(String secretName, String serializedValue, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.CREATE, secretNamespacer.getAppNamespace(), secretName, reason);
		record(opInfo);
		helper.accept(opInfo, client -> client.create(secretNamespacer.appNamespaced(secretName), serializedValue));
	}

	public <T> void update(String secretName, T secretValue, SecretOpReason reason){
		updateRaw(secretName, serialize(secretValue), reason);
	}

	public void updateRaw(String secretName, String serializedValue, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.UPDATE, secretNamespacer.getAppNamespace(), secretName, reason);
		record(opInfo);
		helper.accept(opInfo, client -> client.update(secretNamespacer.appNamespaced(secretName), serializedValue));
	}

	public <T> void put(Supplier<String> secretName, T secretValue, SecretOpReason reason){
		put(secretName.get(), secretValue, reason);
	}

	public <T> void put(String secretName, T secretValue, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.PUT, secretNamespacer.getAppNamespace(), secretName, reason);
		record(opInfo);
		String namespacedName = secretNamespacer.appNamespaced(secretName);
		String serializedValue = serialize(secretValue);
		helper.accept(opInfo, client -> {
			try{
				client.createQuiet(namespacedName, serializedValue);
			}catch(RuntimeException e){
				client.update(namespacedName, serializedValue);
			}
		});
	}

	public void delete(String secretName, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.DELETE, secretNamespacer.getAppNamespace(), secretName, reason);
		record(opInfo);
		helper.accept(opInfo, client -> client.delete(secretNamespacer.appNamespaced(secretName)));
	}

	private void record(SecretOpInfo opInfo){
		secretOpRecorderSupplier.get().recordOp(opInfo);
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

	@Singleton
	private static class SecretClientHelper{

		@Inject
		private DatarouterInjector injector;
		@Inject
		private SecretNamespacer secretNamespacer;
		@Inject
		private SecretClientConfigHolder config;

		/**
		 * injects and returns appropriate {@link SecretClientSupplier}s based on opInfo and {@link SecretClientConfig}s
		 * from config
		 *
		 * @param opInfo passed to {@link SecretClientConfig#allowed(SecretOpInfo)}
		 */
		private Iterator<? extends SecretClientSupplier> getSuppliers(SecretOpInfo opInfo){
			return config.getAllowedSecretClientSupplierClasses(secretNamespacer.isDevelopment(), opInfo)
					.map(injector::getInstance)
					.iterator();
		}

		/**
		 * attempts to apply func with appropriately configured {@link SecretClientSupplier}s until one succeeds
		 *
		 * @param <T> return type of func
		 * @param opInfo passed to getSuppliers
		 * @param func parameterized function to apply with the supplied {@link SecretClient}s
		 * @return result of func
		 * @throws exception thrown by func when applied with last configured {@link SecretClientSupplier} or
		 *         {@link NoConfiguredSecretClientSupplierException} if none are appropriately configured
		 */
		private <T> T apply(SecretOpInfo opInfo, Function<SecretClient,T> func){
			List<String> attemptedSecretClientSuppliers = new ArrayList<>();
			for(Iterator<? extends SecretClientSupplier> iter = getSuppliers(opInfo); iter.hasNext();){
				try{
					SecretClientSupplier supplier = iter.next();
					attemptedSecretClientSuppliers.add(supplier.getClass().getSimpleName());
					T result = func.apply(supplier.get());
					if(attemptedSecretClientSuppliers.size() > 1){
						logger.warn("Secret op succeeded after multiple attempts. attemptedSecretClientSuppliers="
								+ Scanner.of(attemptedSecretClientSuppliers).collect(Collectors.joining(",")));
					}
					return result;
				}catch(SecretClientException e){//these are already logged in BaseSecretClient
					if(!iter.hasNext()){
						throw e;
					}
				}catch(RuntimeException e){//these are not logged in BaseSecretClient, so they need to be logged here
					logger.warn("Unexpected error while attempting secret op.", e);
					if(!iter.hasNext()){
						throw e;
					}
				}
			}
			throw new NoConfiguredSecretClientSupplierException(opInfo);
		}

		/**
		 * same as apply but for functions with void return types
		 * @param opInfo same as {@link apply}
		 * @param func parameterized function to accept with the supplied {@link SecretClient}s
		 */
		private void accept(SecretOpInfo opInfo, Consumer<SecretClient> func){
			apply(opInfo, secretClient -> {
				func.accept(secretClient);
				return null;
			});
		}

	}

}
