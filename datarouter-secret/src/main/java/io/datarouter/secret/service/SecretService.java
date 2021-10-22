/*
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
import java.util.Map;
import java.util.Optional;
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
import io.datarouter.secret.client.SecretClientOp;
import io.datarouter.secret.client.SecretClientOp.SecretClientOpResult;
import io.datarouter.secret.client.SecretClientOps;
import io.datarouter.secret.config.SecretClientConfig;
import io.datarouter.secret.config.SecretClientConfigHolder;
import io.datarouter.secret.exception.NoConfiguredSecretClientSupplierException;
import io.datarouter.secret.exception.SecretClientException;
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
	private SecretClientConfigHolder config;

	public List<SecretClientConfig> getSecretClientSupplierConfigs(){
		return config.getConfigs(secretNamespacer.isDevelopment());
	}

	public List<String> listSecretNames(Optional<String> targetSecretClientSupplierConfigName,
			SecretOpReason reason){
		return listSecretNames(targetSecretClientSupplierConfigName, Optional.empty(), reason);
	}

	public List<String> listSecretNames(Optional<String> targetSecretClientSupplierConfigName,
			Optional<String> exclusivePrefix, SecretOpReason reason){
		return listSecretNamesInternal(targetSecretClientSupplierConfigName, exclusivePrefix, secretNamespacer
				.getAppNamespace(), reason);
	}

	public List<String> listSecretNamesShared(Optional<String> targetSecretClientSupplierConfigName,
			SecretOpReason reason){
		return listSecretNamesInternal(targetSecretClientSupplierConfigName, Optional.empty(), secretNamespacer
				.getSharedNamespace(), reason);
	}

	private List<String> listSecretNamesInternal(Optional<String> targetSecretClientSupplierConfigName,
			Optional<String> exclusivePrefix, String namespace, SecretOpReason reason){
		Optional<String> namespacedPrefix = Optional.of(namespace + exclusivePrefix.orElse(""));
		var opInfo = new SecretOpInfo<>(SecretClientOps.LIST, namespace, "", reason,
				targetSecretClientSupplierConfigName);
		return removePrefixes(helper.apply(opInfo, namespacedPrefix), namespace);
	}

	public List<String> listSecretNameSuffixes(String exclusivePrefix, SecretOpReason reason){
		var secretNames = listAllSecretNamesInternal(Optional.of(exclusivePrefix), secretNamespacer.getAppNamespace(),
				reason);
		return removePrefixes(secretNames, exclusivePrefix);
	}

	private List<String> listAllSecretNamesInternal(Optional<String> exclusivePrefix, String namespace,
			SecretOpReason reason){
		var results = doListSecretNamesInternal(exclusivePrefix, namespace, reason);
		return Scanner.of(results.getAllSupplierResults().values())
				.collate(Scanner::of)
				.listTo(names -> removePrefixes(names, namespace));
	}

	//not sure why this requires package
	private io.datarouter.secret.service.SecretService.SecretClientHelper.SecretClientHelperApplyResult<List<String>>
	doListSecretNamesInternal(Optional<String> exclusivePrefix, String namespace, SecretOpReason reason){
		Optional<String> namespacedPrefix = Optional.of(namespace + exclusivePrefix.orElse(""));
		var opInfo = new SecretOpInfo<>(SecretClientOps.LIST, namespace, "", reason);
		return helper.apply(opInfo, namespacedPrefix, true);
	}

	public <T> T read(String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRaw(Optional.empty(), secretName, reason), secretClass);
	}

	public <T> T readNamespaced(String secretNamespace, String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRawInternal(Optional.empty(), secretNamespace, secretName, reason), secretClass);
	}

	public <T> T readShared(String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRawShared(Optional.empty(), secretName, reason), secretClass);
	}

	//skips the recording step. this is necessary if the recorder uses the same DB that the secret is needed to init.
	public <T> T readSharedWithoutRecord(String secretName, Class<T> secretClass, SecretOpReason reason){
		return deserialize(readRawSharedWithoutRecord(secretName, reason), secretClass);
	}

	//skips the recording step. this is necessary if the recorder uses the same DB that the secret is needed to init.
	public String readRawSharedWithoutRecord(String secretName, SecretOpReason reason){
		String namespace = secretNamespacer.getSharedNamespace();
		var opInfo = new SecretOpInfo<>(SecretClientOps.READ, namespace, secretName, reason, false, true);
		return helper.apply(opInfo, opInfo.getNamespaced()).getValue();
	}

	public String readRaw(Optional<String> targetSecretClientSupplierConfigName, String secretName,
			SecretOpReason reason){
		return readRawInternal(targetSecretClientSupplierConfigName, secretNamespacer.getAppNamespace(), secretName,
				reason);
	}

	public String readRawShared(Optional<String> targetSecretClientSupplierConfigName, String secretName,
			SecretOpReason reason){
		return readRawInternal(targetSecretClientSupplierConfigName, secretNamespacer.getSharedNamespace(), secretName,
				reason);
	}

	private String readRawInternal(Optional<String> targetSecretClientSupplierConfigName, String namespace,
			String secretName, SecretOpReason reason){
		var opInfo = new SecretOpInfo<>(SecretClientOps.READ, namespace, secretName, reason,
				targetSecretClientSupplierConfigName);
		return helper.apply(opInfo, opInfo.getNamespaced()).getValue();
	}

	public <T> T readCachedSecret(Supplier<String> secretName, Class<T> secretClass, SecretOpReason reason,
			boolean hasDefaultFallback){
		var opInfo = new SecretOpInfo<>(SecretClientOps.READ, secretNamespacer.getAppNamespace(), secretName.get(),
				reason, false, !hasDefaultFallback);
		return deserialize(helper.apply(opInfo, opInfo.getNamespaced()).getValue(), secretClass);
	}

	public <T> T readSharedCachedSecret(Supplier<String> secretName, Class<T> secretClass, SecretOpReason reason,
			boolean hasDefaultFallback){
		var opInfo = new SecretOpInfo<>(SecretClientOps.READ, secretNamespacer.getSharedNamespace(), secretName.get(),
				reason, false, !hasDefaultFallback);
		return deserialize(helper.apply(opInfo, opInfo.getNamespaced()).getValue(), secretClass);
	}

	public <T> void create(String secretName, T secretValue, SecretOpReason reason){
		createRaw(Optional.empty(), secretName, serialize(secretValue), reason);
	}

	public void create(Optional<String> targetSecretClientSupplierConfigName, String secretName, String value,
			Class<?> secretClass, SecretOpReason reason){
		deserialize(value, secretClass);//generate an exception if this doesn't work
		createRaw(targetSecretClientSupplierConfigName, secretName, value, reason);
	}

	public void createRaw(Optional<String> targetSecretClientSupplierConfigName, String secretName,
			String serializedValue, SecretOpReason reason){
		createRawNamespaced(targetSecretClientSupplierConfigName, secretNamespacer.getAppNamespace(), secretName,
				serializedValue, reason);
	}

	public <T> void createNamespaced(String secretNamespace, String secretName, T secretValue, SecretOpReason reason){
		createRawNamespaced(Optional.empty(), secretNamespace, secretName, serialize(secretValue), reason);
	}

	private void createRawNamespaced(Optional<String> targetSecretClientSupplierConfigName, String secretNamespace,
			String secretName, String serializedValue, SecretOpReason reason){
		var opInfo = new SecretOpInfo<>(SecretClientOps.CREATE, secretNamespace, secretName, reason,
				targetSecretClientSupplierConfigName);
		helper.apply(opInfo, new Secret(opInfo.getNamespaced(), serializedValue));
	}

	public <T> void update(String secretName, T secretValue, SecretOpReason reason){
		updateRaw(Optional.empty(), secretName, serialize(secretValue), reason);
	}

	public void updateRaw(Optional<String> targetSecretClientSupplierConfigName, String secretName,
			String serializedValue, SecretOpReason reason){
		var opInfo = new SecretOpInfo<>(SecretClientOps.UPDATE, secretNamespacer.getAppNamespace(), secretName, reason,
				targetSecretClientSupplierConfigName);
		helper.apply(opInfo, new Secret(opInfo.getNamespaced(), serializedValue));
	}

	public <T> void put(Supplier<String> secretName, T secretValue, SecretOpReason reason){
		put(secretName.get(), secretValue, reason);
	}

	public <T> void put(String secretName, T secretValue, SecretOpReason reason){
		var opInfo = new SecretOpInfo<>(SecretClientOps.PUT, secretNamespacer.getAppNamespace(), secretName, reason);
		String namespacedName = opInfo.getNamespaced();
		String serializedValue = serialize(secretValue);
		helper.apply(opInfo, new Secret(namespacedName, serializedValue));
	}

	public void delete(Optional<String> targetSecretClientSupplierConfigName, String secretName, SecretOpReason reason){
		deleteNamespaced(targetSecretClientSupplierConfigName, secretNamespacer.getAppNamespace(), secretName, reason);
	}

	public void deleteNamespaced(Optional<String> targetSecretClientSupplierConfigName, String secretNamespace,
			String secretName, SecretOpReason reason){
		var opInfo = new SecretOpInfo<>(SecretClientOps.DELETE, secretNamespace, secretName, reason,
				targetSecretClientSupplierConfigName);
		helper.apply(opInfo, opInfo.getNamespaced());
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
		 * returns usable {@link SecretClientConfig}s based on opInfo
		 * @param opInfo passed to {@link SecretClientConfig#allowed(SecretOpInfo)}
		 */
		private Iterator<SecretClientConfig> getSecretClientConfigs(SecretOpInfo<?,?> opInfo){
			return config.getAllowedSecretClientConfigs(secretNamespacer.isDevelopment(), opInfo).iterator();
		}

		private <I,O> O apply(SecretOpInfo<I,O> opInfo, I input){
			return apply(opInfo, input, false).getSingleSupplierResult();
		}

		/**
		 * attempts to execute a {@link SecretClientOp} using configured {@link SecretClient}s, until one or all succeed
		 * See {@link SecretClientOps} for available ops.
		 * @param <I> {@link SecretClientOp} input type
		 * @param <O> {@link SecretClientOp} output type
		 * @param opInfo determines whether to record the op and which configured suppliers may be used
		 * @param input input to op
		 * @param shouldApplyToAllSuppliers if true, then the op is executed with each client, instead of stopping after
		 * the first success
		 * @return if shouldApplyToAllSuppliers, then the result will contain all successful op attempts, otherwise
		 * every failed op attempt followed by the one successful op attempt
		 * @throws if shouldApplyToAllSuppliers, exception thrown by func on first failure, otherwise exception thrown
		 * by func on last failure or {@link NoConfiguredSecretClientSupplierException} if no suppliers are
		 * appropriately configured
		 */
		private <I,O> SecretClientHelperApplyResult<O> apply(SecretOpInfo<I,O> opInfo, I input,
				boolean shouldApplyToAllSuppliers){
			if(opInfo.shouldRecord){
				secretOpRecorderSupplier.get().recordOp(opInfo);
			}
			SecretClientOp<I,O> op = opInfo.clientOp;
			List<SecretClientOpAttempt<O>> attemptedOps = new ArrayList<>();
			for(Iterator<SecretClientConfig> iter = getSecretClientConfigs(opInfo); iter.hasNext();){
				SecretClientConfig supplierConfig = iter.next();
				var clientSupplier = injector.getInstance(supplierConfig.getSecretClientSupplierClass());
				SecretClientOpResult<O> result = op.validateAndExecute(clientSupplier.get(), input);
				//TODO add supplier counters?
				attemptedOps.add(new SecretClientOpAttempt<>(supplierConfig, result));

				//give up on applying to all suppliers if one fails
				if(shouldApplyToAllSuppliers && !result.isSuccess()){
					break;
				}
				//continue to next supplier if this one failed or if applying to all supplier should continue
				if(!result.isSuccess() || shouldApplyToAllSuppliers){
					continue;
				}

				//success (short circuit the rest of the suppliers, if any)
				if(attemptedOps.size() > 1 && !secretNamespacer.isDevelopment()){
					String attemptedSecretClientConfigs = Scanner.of(attemptedOps)
							.map(opAttempt -> opAttempt.secretClientConfig)
							.map(SecretClientConfig::getConfigName)
							.collect(Collectors.joining(","));
					logger.warn("Secret op succeeded after multiple attempts. attemptedSecretClientConfigs="
							+ attemptedSecretClientConfigs);
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
				attemptedOps.forEach(attemptedOp -> SecretClientHelper.logIfError(attemptedOp, opInfo));
				//throw the most recent failed op exception
				throw attemptedOps.get(attemptedOps.size() - 1).getException();
			}
			//nothing attempted because no suppliers are configured to handle this op
			throw new NoConfiguredSecretClientSupplierException(opInfo);
		}

		private static <T> void logIfError(SecretClientOpAttempt<T> opAttempt, SecretOpInfo<?,?> secretOpInfo){
			if(opAttempt.isSuccess() || !secretOpInfo.shouldLog){
				return;
			}
			logger.error(opAttempt.secretClientConfig.getConfigName() + " failed op", opAttempt.getException());
		}

		private static class SecretClientOpAttempt<T>{

			public final SecretClientConfig secretClientConfig;
			private final SecretClientOpResult<T> opResult;

			public SecretClientOpAttempt(SecretClientConfig secretClientConfig, SecretClientOpResult<T> opResult){
				this.secretClientConfig = secretClientConfig;
				this.opResult = opResult;
			}

			public boolean isSuccess(){
				return opResult.isSuccess();
			}

			public T getResult(){
				if(!opResult.isSuccess()){
					throw getException();
				}
				return opResult.result.orElse(null);//allows Void ops
			}

			public SecretClientException getException(){
				return opResult.exception.orElseGet(() -> new SecretClientException("unknown error"));
			}

		}

		private static class SecretClientHelperApplyResult<T>{

			public final List<SecretClientOpAttempt<T>> opAttempts;

			public SecretClientHelperApplyResult(List<SecretClientOpAttempt<T>> opAttempts){
				this.opAttempts = opAttempts;
			}

			public Map<String,T> getAllSupplierResults(){
				return Scanner.of(opAttempts)
						.toMap(
								opAttempt -> opAttempt.secretClientConfig.getConfigName(),
								opAttempt -> opAttempt.getResult());
			}

			public T getSingleSupplierResult(){
				return opAttempts.get(opAttempts.size() - 1).getResult();
			}

		}

	}

}
