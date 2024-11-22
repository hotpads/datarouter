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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.datarouter.httpclient.endpoint.DatarouterSharedSecretRetriever;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.util.Require;
import io.datarouter.util.cached.Cached;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
/**
 * This is the recommended interface for all read-only {@link Secret} retrieval. It allows reading values with automatic
 * namespacing provided by {@link SecretService}.
 */
public class CachedSecretFactory implements DatarouterSharedSecretRetriever{

	private static final long CACHE_LENGTH = 15;
	private static final TimeUnit CACHE_LENGTH_UNIT = TimeUnit.SECONDS;

	@Inject
	private SecretService secretService;

	/*--------------------------- Not shared ----------------------------*/

	public CachedSecret<String> cacheSecretString(Supplier<String> nameSupplier){
		return cacheSecret(nameSupplier, String.class);
	}

	public <T> CachedSecret<T> cacheSecret(Supplier<String> nameSupplier, Class<T> secretClass){
		return cacheSecretInternal(nameSupplier, secretClass, false);
	}

	/*--------------------------- Shared ----------------------------*/

	public CachedSecret<String> cacheSharedSecretString(Supplier<String> nameSupplier){
		return cacheSharedSecret(nameSupplier, String.class);
	}

	public <T> CachedSecret<T> cacheSharedSecret(Supplier<String> nameSupplier, Class<T> secretClass){
		return cacheSecretInternal(nameSupplier, secretClass, true);
	}

	private <T> CachedSecret<T> cacheSecretInternal(Supplier<String> nameSupplier, Class<T> secretClass,
			boolean isShared){
		Require.noNulls(nameSupplier, secretClass);
		Require.isFalse(StringTool.isNullOrEmptyOrWhitespace(nameSupplier.get()));
		return new CachedSecret<>(nameSupplier, secretClass, isShared);
	}

	public final class CachedSecret<T> extends Cached<T>{

		private final Supplier<String> nameSupplier;
		private final Class<T> secretClass;
		private final boolean isShared;

		private CachedSecret(Supplier<String> nameSupplier, Class<T> secretClass, boolean isShared){
			super(CACHE_LENGTH, CACHE_LENGTH_UNIT);
			this.nameSupplier = nameSupplier;
			this.secretClass = secretClass;
			this.isShared = isShared;
		}

		public CachedSecret<T> withTtl(Duration ttl){
			super.setTtl(ttl);
			return this;
		}

		@Override
		protected T reload(){
			SecretOpConfig.Builder builder = SecretOpConfig.builder(SecretOpReason.automatedOp("CachedSecret"));
			if(isShared){
				builder.useSharedNamespace();
			}
			return secretService.read(nameSupplier.get(), secretClass, builder.build());
		}

		@Override
		public String toString(){
			return "CachedSecret=" + nameSupplier.get();
		}

	}

}
