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

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.util.Require;
import io.datarouter.util.cached.Cached;
import io.datarouter.util.string.StringTool;

@Singleton
/**
 * This is the recommended interface for all read-only {@link Secret} retrieval. It allows reading values with automatic
 * namespacing provided by {@link SecretService}.
 */
public class CachedSecretFactory{

	private static final long CACHE_LENGTH = 15;
	private static final TimeUnit CACHE_LENGTH_UNIT = TimeUnit.SECONDS;

	@Inject
	private SecretService secretService;
	@Inject
	private SecretNamespacer secretNamespacer;

	//not shared

	public CachedSecret<String> cacheSecretString(Supplier<String> nameSupplier){
		return cacheSecret(nameSupplier, String.class);
	}

	public CachedSecret<String> cacheSecretString(Supplier<String> nameSupplier, String defaultValue){
		return cacheSecret(nameSupplier, String.class, defaultValue);
	}

	public <T> CachedSecret<T> cacheSecret(Supplier<String> nameSupplier, Class<T> secretClass){
		return cacheSecretInternal(nameSupplier, secretClass, Optional.empty(), false);
	}

	public <T> CachedSecret<T> cacheSecret(Supplier<String> nameSupplier, Class<T> secretClass, T defaultValue){
		return cacheSecretInternal(nameSupplier, secretClass, Optional.of(defaultValue), false);
	}

	//shared

	public CachedSecret<String> cacheSharedSecretString(Supplier<String> nameSupplier){
		return cacheSharedSecret(nameSupplier, String.class);
	}

	public CachedSecret<String> cacheSharedSecretString(Supplier<String> nameSupplier, String defaultValue){
		return cacheSharedSecret(nameSupplier, String.class, defaultValue);
	}

	public <T> CachedSecret<T> cacheSharedSecret(Supplier<String> nameSupplier, Class<T> secretClass){
		return cacheSecretInternal(nameSupplier, secretClass, Optional.empty(), true);
	}

	public <T> CachedSecret<T> cacheSharedSecret(Supplier<String> nameSupplier, Class<T> secretClass, T defaultValue){
		return cacheSecretInternal(nameSupplier, secretClass, Optional.of(defaultValue), true);
	}

	private <T> CachedSecret<T> cacheSecretInternal(Supplier<String> nameSupplier, Class<T> secretClass,
			Optional<T> defaultValue, boolean isShared){
		Require.noNulls(nameSupplier, secretClass);
		Require.isFalse(StringTool.isNullOrEmptyOrWhitespace(nameSupplier.get()));
		return new CachedSecret<>(nameSupplier, secretClass, isShared, defaultValue);
	}

	public final class CachedSecret<T> extends Cached<T>{

		private final Supplier<String> nameSupplier;
		private final Class<T> secretClass;
		private final boolean isShared;
		private final Optional<T> defaultValue;

		private CachedSecret(Supplier<String> nameSupplier, Class<T> secretClass, boolean isShared,
				Optional<T> defaultValue){
			super(CACHE_LENGTH, CACHE_LENGTH_UNIT);
			this.nameSupplier = nameSupplier;
			this.secretClass = secretClass;
			this.isShared = isShared;
			this.defaultValue = defaultValue;
		}

		@Override
		protected T reload(){
			boolean hasDefaultFallback = secretNamespacer.isDevelopment() && defaultValue.isPresent();
			try{
				return isShared ? secretService.readSharedCachedSecret(nameSupplier, secretClass, SecretOpReason
						.automatedOp("CachedSecret"), hasDefaultFallback) : secretService.readCachedSecret(nameSupplier,
						secretClass, SecretOpReason.automatedOp("CachedSecret"), hasDefaultFallback);
			}catch(RuntimeException e){
				//TODO consider making environment-based behavior possible (unless removing cached defaults)
				if(hasDefaultFallback){
					return defaultValue.get();
				}
				throw e;
			}
		}

		@Override
		public String toString(){
			return "CachedSecret=" + nameSupplier.get();
		}

	}

}
