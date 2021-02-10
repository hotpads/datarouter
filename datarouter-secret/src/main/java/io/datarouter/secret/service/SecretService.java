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
import io.datarouter.secret.client.SecretClient.SecretClientOpResult;
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

	private List<String> listSecretNamesInternal(Optional<String> exclusivePrefix, String namespace,
			SecretOpReason reason){
		Optional<String> namespacedPrefix = Optional.of(namespace + exclusivePrefix.orElse(""));
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.LIST, namespace, "", reason);
		var results = helper.apply(opInfo, client -> client.listNames(namespacedPrefix), true);
		return Scanner.of(results.getAllSupplierResults())
				.collate(Scanner::of)
				.listTo(names -> removePrefixes(names, namespace));
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
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.READ, namespace, secretName, reason, false);
		return helper.apply(opInfo, client -> client.read(opInfo.getNamespaced())).getValue();
	}

	public String readRaw(String secretName, SecretOpReason reason){
		return readRawInternal(secretNamespacer.getAppNamespace(), secretName, reason);
	}

	public String readRawShared(String secretName, SecretOpReason reason){
		return readRawInternal(secretNamespacer.getSharedNamespace(), secretName, reason);
	}

	private String readRawInternal(String namespace, String secretName, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.READ, namespace, secretName, reason);
		return helper.apply(opInfo, client -> client.read(opInfo.getNamespaced())).getValue();
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
		helper.apply(opInfo, client -> client.create(opInfo.getNamespaced(), serializedValue));
	}

	public <T> void update(String secretName, T secretValue, SecretOpReason reason){
		updateRaw(secretName, serialize(secretValue), reason);
	}

	public void updateRaw(String secretName, String serializedValue, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.UPDATE, secretNamespacer.getAppNamespace(), secretName, reason);
		helper.apply(opInfo, client -> client.update(opInfo.getNamespaced(), serializedValue));
	}

	public <T> void put(Supplier<String> secretName, T secretValue, SecretOpReason reason){
		put(secretName.get(), secretValue, reason);
	}

	public <T> void put(String secretName, T secretValue, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.PUT, secretNamespacer.getAppNamespace(), secretName, reason);
		String namespacedName = opInfo.getNamespaced();
		String serializedValue = serialize(secretValue);
		helper.apply(opInfo, client -> {
			try{
				return client.create(namespacedName, serializedValue);
			}catch(RuntimeException e){
				return client.update(namespacedName, serializedValue);
			}
		});
	}

	public void delete(String secretName, SecretOpReason reason){
		SecretOpInfo opInfo = new SecretOpInfo(SecretOp.DELETE, secretNamespacer.getAppNamespace(), secretName, reason);
		helper.apply(opInfo, client -> client.delete(opInfo.getNamespaced()));
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
		private SecretClientConfigHolder config;
		@Inject
		private SecretNamespacer secretNamespacer;
		@Inject
		private SecretOpRecorderSupplier secretOpRecorderSupplier;

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
		 * convenience method to call {@link SecretClientHelper#apply(SecretOpInfo, Function, boolean)} and return the
		 * first success
		 *
		 * @param <T> type of the {@link SecretClientOpResult} returned by func
		 * @param opInfo passed to getSuppliers
		 * @param func parameterized function to apply
		 * @return result of the successful {@link SecretClientOpResult} (null for Void)
		 * @throws exception thrown by func on last failure or {@link NoConfiguredSecretClientSupplierException} if none
		 * are appropriately configured
		 */
		private <T> T apply(SecretOpInfo opInfo, Function<SecretClient,SecretClientOpResult<T>> func){
			return apply(opInfo, func, false).getSingleSupplierResult();
		}

		/**
		 * attempts to apply func to appropriately configured {@link SecretClient}s, until one or all succeed
		 *
		 * @param <T> type of the {@link SecretClientOpResult} returned by func
		 * @param opInfo passed to getSuppliers
		 * @param func parameterized function to apply
		 * @param shouldApplyToAllSuppliers if true, then func is applied to each client, instead of stopping after the
		 * first success
		 * @return if shouldApplyToAllSuppliers, then the result will contain all successful op attempts, otherwise
		 * every failed op attempt followed by the one successful op attempt
		 * @throws if shouldApplyToAllSuppliers, exception thrown by func on first failure, otherwise exception thrown
		 * by func on last failure or {@link NoConfiguredSecretClientSupplierException} if no suppliers are
		 * appropriately configured
		 */
		private <T> SecretClientHelperApplyResult<T> apply(SecretOpInfo opInfo, Function<SecretClient,
				SecretClientOpResult<T>> func, boolean shouldApplyToAllSuppliers){
			if(opInfo.shouldRecord){
				secretOpRecorderSupplier.get().recordOp(opInfo);
			}
			List<SecretClientOpAttempt<T>> attemptedOps = new ArrayList<>();
			for(Iterator<? extends SecretClientSupplier> iter = getSuppliers(opInfo); iter.hasNext();){
				SecretClientSupplier supplier = iter.next();
				SecretClientOpResult<T> result = func.apply(supplier.get());
				//TODO add supplier counters?
				attemptedOps.add(new SecretClientOpAttempt<>(supplier.getClass().getSimpleName(), result));

				//give up on applying to all suppliers if one fails
				if(shouldApplyToAllSuppliers && !result.isSuccess()){
					break;
				}
				//continue to next supplier if this one failed or if applying to all supplier should continue
				if(!result.isSuccess() || shouldApplyToAllSuppliers){
					continue;
				}

				//success (short circuit the rest of the suppliers, if any)
				if(attemptedOps.size() > 1){
					String attemptedSecretClientSuppliers = Scanner.of(attemptedOps)
							.map(opAttempt -> opAttempt.secretClientSupplierName)
							.collect(Collectors.joining(","));
					logger.warn("Secret op succeeded after multiple attempts. attemptedSecretClientSuppliers="
							+ attemptedSecretClientSuppliers);
				}
				return new SecretClientHelperApplyResult<>(attemptedOps);
			}
			//all suppliers successfully applied
			if(attemptedOps.size() > 0 && shouldApplyToAllSuppliers && Scanner.of(attemptedOps)
					.allMatch(SecretClientOpAttempt::isSuccess)){
				return new SecretClientHelperApplyResult<>(attemptedOps);
			}
			//overall failure
			if(attemptedOps.size() > 0){
				attemptedOps.forEach(SecretClientHelper::logIfError);
				//throw the most recent failed op exception
				throw attemptedOps.get(attemptedOps.size() - 1).getException();
			}
			//nothing attempted because no suppliers are configured to handle this op
			throw new NoConfiguredSecretClientSupplierException(opInfo);
		}

		private static <T> void logIfError(SecretClientOpAttempt<T> opAttempt){
			if(opAttempt.isSuccess()){
				return;
			}
			logger.error(opAttempt.secretClientSupplierName + " failed op", opAttempt.getException());
		}

		private static class SecretClientOpAttempt<T>{

			public final String secretClientSupplierName;
			public final SecretClientOpResult<T> opResult;

			public SecretClientOpAttempt(String secretClientSupplierName, SecretClientOpResult<T> opResult){
				this.secretClientSupplierName = secretClientSupplierName;
				this.opResult = opResult;
			}

			public boolean isSuccess(){
				return opResult.isSuccess();
			}

			public T getResult(){
				return opResult.result.orElse(null);//allows Void ops
			}

			public SecretClientException getException(){
				return opResult.exception.get();
			}

		}

		private static class SecretClientHelperApplyResult<T>{

			public final List<SecretClientOpAttempt<T>> opAttempts;

			public SecretClientHelperApplyResult(List<SecretClientOpAttempt<T>> opAttempts){
				this.opAttempts = opAttempts;
			}

			public List<T> getAllSupplierResults(){
				return Scanner.of(opAttempts)
						.map(SecretClientOpAttempt::getResult)
						.list();
			}

			public T getSingleSupplierResult(){
				return opAttempts.get(opAttempts.size() - 1).getResult();
			}

		}

	}

}
