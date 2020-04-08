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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.secret.client.LocalStorageSecretClient.LocalStorageDefaultSecretValues;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.storage.oprecord.DatarouterSecretOpRecordDao;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.handler.encoder.HandlerEncoder;

/**
 * This is the recommended interface for accessing {@link Secret}s that need to be written to, and namespacing is
 * automatically applied before and after all interactions with secret names. Except for when a caller switches from one
 * environmentType or service name to another, there is no need to update or migrate secret names.
 */
@Singleton
public class SecretService{

	private static final String SHARED_NAMESPACE = "shared";

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterSecretOpRecordDao datarouterSecretOpRecordDao;
	@Inject
	@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)
	private JsonSerializer jsonSerializer;
	@Inject
	private LocalStorageDefaultSecretValues localStorageDefaultSecretValues;
	@Inject
	private SecretClientSupplier secretClientSupplier;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	public List<String> listSecretNames(){
		return listSecretNames(Optional.empty());
	}

	public List<String> listSecretNames(Optional<String> exclusivePrefix){
		return listSecretNamesInternal(exclusivePrefix, getAppNamespace());
	}

	public List<String> listSecretNameSuffixes(String exclusivePrefix){
		return removePrefixes(listSecretNames(Optional.of(exclusivePrefix)), exclusivePrefix);
	}

	public List<String> listSecretNamesShared(){
		return listSecretNamesInternal(Optional.empty(), getSharedNamespace());
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

	public String readRaw(String secretName, SecretOpReason reason){
		return readRawInternal(getAppNamespace(), secretName, reason);
	}

	public String readRawShared(String secretName, SecretOpReason reason){
		return readRawInternal(getSharedNamespace(), secretName, reason);
	}

	private String readRawInternal(String namespace, String secretName, SecretOpReason reason){
		recordOp(SecretOp.READ, namespace, secretName, reason);
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
		recordOp(SecretOp.CREATE, getAppNamespace(), secretName, reason);
		secretClientSupplier.get().create(namespaced(secretName), serializedValue);
	}

	public <T> void update(String secretName, T secretValue, SecretOpReason reason){
		updateRaw(secretName, serialize(secretValue), reason);
	}

	public void updateRaw(String secretName, String serializedValue, SecretOpReason reason){
		recordOp(SecretOp.UPDATE, getAppNamespace(), secretName, reason);
		secretClientSupplier.get().update(namespaced(secretName), serializedValue);
	}

	public <T> void put(Supplier<String> secretName, T secretValue, SecretOpReason reason){
		put(secretName.get(), secretValue, reason);
	}

	public <T> void put(String secretName, T secretValue, SecretOpReason reason){
		recordOp(SecretOp.PUT, getAppNamespace(), secretName, reason);
		SecretClient secretClient = secretClientSupplier.get();
		secretName = namespaced(secretName);
		try{
			secretClient.createQuiet(secretName, serialize(secretValue));
		}catch(RuntimeException e){
			secretClient.update(secretName, serialize(secretValue));
		}
	}

	public void delete(String secretName, SecretOpReason reason){
		recordOp(SecretOp.DELETE, getAppNamespace(), secretName, reason);
		secretClientSupplier.get().delete(namespaced(secretName));
	}

	public final <T> void registerDevelopmentDefaultValue(Supplier<String> secretName, T defaultValue){
		String namespaced = namespaced(secretName.get());
		if(serverTypeDetector.mightBeDevelopment()){
			localStorageDefaultSecretValues.registerDefaultValue(namespaced, serialize(defaultValue));
		}
	}

	public final <T> void registerDevelopmentDefaultValueShared(Supplier<String> secretName, T defaultValue){
		String namespaced = sharedNamespaced(secretName.get());
		if(serverTypeDetector.mightBeDevelopment()){
			localStorageDefaultSecretValues.registerDefaultValue(namespaced, serialize(defaultValue));
		}
	}

	private void recordOp(SecretOp op, String namespace, String secretName, SecretOpReason reason){
		datarouterSecretOpRecordDao.recordOp(namespace, secretName, op, reason);
	}

	private <T> String serialize(T value){
		return jsonSerializer.serialize(value);
	}

	private <T> T deserialize(String value, Class<T> targetClass){
		return jsonSerializer.deserialize(value, targetClass);
	}

	private String namespaced(String secretName){
		return getAppNamespace() + secretName;
	}

	private String getAppNamespace(){
		return getEnvironment() + '/' + Objects.requireNonNull(datarouterService.getName()) + '/';
	}

	private String sharedNamespaced(String secretName){
		return getSharedNamespace() + secretName;
	}

	private String getSharedNamespace(){
		return getEnvironment() + '/' + SHARED_NAMESPACE + '/';
	}

	private String getEnvironment(){
		return Objects.requireNonNull(datarouterProperties.getEnvironmentType());
	}

	private static List<String> removePrefixes(List<String> strings, String prefix){
		return IterableTool.nullSafeMap(strings, string -> string.substring(prefix.length()));
	}

}
