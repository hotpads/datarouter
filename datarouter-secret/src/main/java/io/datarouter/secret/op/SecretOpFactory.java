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
package io.datarouter.secret.op;

import java.util.List;
import java.util.Optional;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.config.SecretClientSupplierConfigHolder;
import io.datarouter.secret.op.adapter.DeserializingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.ListNamespacingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.NamespacingMode;
import io.datarouter.secret.op.adapter.NamespacingAdapter.OptionalStringNamespacingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.SecretNamespacingAdapter;
import io.datarouter.secret.op.adapter.NamespacingAdapter.StringNamespacingAdapter;
import io.datarouter.secret.op.adapter.NoOpAdapter;
import io.datarouter.secret.op.adapter.SecretOpAdapter;
import io.datarouter.secret.op.adapter.SecretOpAdapterChain;
import io.datarouter.secret.op.adapter.SecretValueExtractingAdapter;
import io.datarouter.secret.op.adapter.SerializingAdapter;
import io.datarouter.secret.op.adapter.TypedSecret;
import io.datarouter.secret.op.client.SecretClientOp;
import io.datarouter.secret.op.client.SecretClientOps;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretOpRecorderSupplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Contains convenience methods and injections to make creating standard {@link SecretOp}s easier.
 */
@Singleton
public class SecretOpFactory{

	@Inject
	private DatarouterInjector injector;
	@Inject
	private SecretClientSupplierConfigHolder secretClientSupplierConfigHolder;
	@Inject
	private SecretJsonSerializer jsonSerializer;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private SecretOpRecorderSupplier secretOpRecorderSupplier;

	public <T> SecretOp<TypedSecret<T>,Secret,Void,Void> buildCreateOp(String name, T value,
			SecretOpConfig config){
		return buildWriteOp(name, value, config, SecretClientOps.CREATE);
	}

	public <T> SecretOp<String,String,Secret,T> buildReadOp(String name, Class<T> classOfT,
			SecretOpConfig config){
		return buildOp(
				config,
				name,
				name,
				SecretClientOps.READ,
				new StringNamespacingAdapter(secretNamespacer, config, NamespacingMode.ADDING),
				new SecretOpAdapterChain<>(new SecretNamespacingAdapter(
						secretNamespacer,
						config,
						NamespacingMode.REMOVING))
						.chain(new DeserializingAdapter<>(jsonSerializer, classOfT, config))
						.chain(new SecretValueExtractingAdapter<>()));
	}

	public <T> SecretOp<TypedSecret<T>,Secret,Void,Void> buildUpdateOp(String name, T value,
			SecretOpConfig config){
		return buildWriteOp(name, value, config, SecretClientOps.UPDATE);
	}

	public SecretOp<String,String,Void,Void> buildDeleteOp(String name, SecretOpConfig config){
		return buildOp(
				config,
				name,
				name,
				SecretClientOps.DELETE,
				new StringNamespacingAdapter(secretNamespacer, config, NamespacingMode.ADDING),
				new NoOpAdapter<>());
	}

	public <T> SecretOp<TypedSecret<T>,Secret,Void,Void> buildPutOp(String name, T value,
			SecretOpConfig config){
		return buildWriteOp(name, value, config, SecretClientOps.PUT);
	}

	public SecretOp<Optional<String>,Optional<String>,List<String>,List<String>> buildListOp(
			Optional<String> prefix, SecretOpConfig config){
		return buildOp(
				config,
				prefix.orElse(""),
				prefix,
				SecretClientOps.LIST,
				new OptionalStringNamespacingAdapter(secretNamespacer, config, NamespacingMode.ADDING),
				new SecretOpAdapterChain<>(new ListNamespacingAdapter(
						secretNamespacer,
						config,
						NamespacingMode.REMOVING)));
	}

	private <T> SecretOp<TypedSecret<T>,Secret,Void,Void> buildWriteOp(String name, T value,
			SecretOpConfig config, SecretClientOp<Secret,Void> secretClientOp){
		TypedSecret<T> wrappedInput = new TypedSecret<>(name, value);
		SerializingAdapter<T> serializingAdapter = new SerializingAdapter<>(jsonSerializer, config);
		return buildOp(
				config,
				name,
				wrappedInput,
				secretClientOp,
				new SecretOpAdapterChain<>(serializingAdapter)
						.chain(new SecretNamespacingAdapter(secretNamespacer, config, NamespacingMode.ADDING)),
				new NoOpAdapter<>());
	}

	private <I,CI,CO,O> SecretOp<I,CI,CO,O> buildOp(SecretOpConfig secretOpConfig, String name, I input,
			SecretClientOp<CI,CO> secretClientOp, SecretOpAdapter<I,CI> inputAdapter,
			SecretOpAdapter<CO,O> outputAdapter){
		return new SecretOp<>(
				name,
				secretNamespacer,
				input,
				secretOpConfig,
				injector,
				secretClientSupplierConfigHolder,
				secretClientOp,
				inputAdapter,
				outputAdapter,
				secretOpRecorderSupplier);
	}

}