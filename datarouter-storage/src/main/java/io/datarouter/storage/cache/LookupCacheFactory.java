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
package io.datarouter.storage.cache;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.UncheckedExecutionException;

import io.datarouter.storage.config.guice.DatarouterExecutorGuiceModule;
import io.datarouter.storage.test.DatarouterStorageTestModuleFactory;
import io.datarouter.util.lang.ObjectTool;

/**
 * This builds {@link LookupCache}s configured by {@link LookupCacheFactoryConfig}
 */
@Singleton
public class LookupCacheFactory{

	private final ListeningExecutorService executorService;

	@Inject
	public LookupCacheFactory(@Named(DatarouterExecutorGuiceModule.POOL_lookupCache)ExecutorService executorService){
		this.executorService = MoreExecutors.listeningDecorator(executorService);
	}

	private <K,V> LookupCache<K,V> build(LookupCacheFactoryConfig<K,V> config){
		return doBuild(config, Optional.empty());
	}

	private <K,V> LookupCache<K,V> buildForTest(LookupCacheFactoryConfig<K,V> config, Ticker ticker){
		return doBuild(config, Optional.of(ticker));
	}

	private <K,V> LookupCache<K,V> doBuild(LookupCacheFactoryConfig<K,V> config, Optional<Ticker> ticker){
		ObjectTool.requireNonNulls(config.lookup, config.exceptionFunction);
		if(config.refreshAfterWriteDurationMs > config.expireAfterWriteDurationMs){
			throw new IllegalArgumentException("refreshAfterWriteDurationMs > expireAfterWriteDurationMs not allowed.");
		}

		CacheBuilder<Object,Object> builder = CacheBuilder.newBuilder()
				.maximumSize(config.maximumSize)
				.expireAfterWrite(config.expireAfterWriteDurationMs, TimeUnit.MILLISECONDS)
				.refreshAfterWrite(config.refreshAfterWriteDurationMs, TimeUnit.MILLISECONDS)
				.ticker(ticker.orElse(Ticker.systemTicker()));
		LoadingCache<K,Optional<V>> cache = builder.build(new CacheLoader<K,Optional<V>>(){
					@Override
					public Optional<V> load(K key){
						LookupCache.logger.debug("Loading {}", key);
						return Optional.ofNullable(config.lookup.apply(key));
					}

					@Override
					//this just enables asynchronous reloads when using refreshAfterWrite
					public ListenableFuture<Optional<V>> reload(K key, Optional<V> oldValue){
						LookupCache.logger.debug("Reloading {}", key);
						return executorService.submit(() -> load(key));
					}
				});

		return new LookupCache<>(cache, config.exceptionFunction);
	}

	/**
	 * Configuration for {@link LookupCacheFactory}. The only required configuration method to call is
	 * {@link LookupCacheFactoryConfig#withLookup(Function)}
	 */
	public static class LookupCacheFactoryConfig<K, V>{

		public final Function<K,RuntimeException> defaultExceptionFunction = K -> new RuntimeException(
				"Failed to lookup " + K);
		public static final Long DEFAULT_MAXIMUM_SIZE = 256L;
		public static final Long DEFAULT_REFRESH_AFTER_WRITE_MS = 10000L;
		public static final Long DEFAULT_EXPIRE_AFTER_WRITE_MS = 30000L;

		private Function<K,V> lookup;
		private Function<K,RuntimeException> exceptionFunction = defaultExceptionFunction;
		private long maximumSize = DEFAULT_MAXIMUM_SIZE;
		private long refreshAfterWriteDurationMs = DEFAULT_REFRESH_AFTER_WRITE_MS;
		private long expireAfterWriteDurationMs = DEFAULT_EXPIRE_AFTER_WRITE_MS;

		/**
		 * @param lookup function that looks up V (nullable) using K
		 */
		public LookupCacheFactoryConfig<K,V> withLookup(Function<K,V> lookup){
			Objects.requireNonNull(lookup);
			this.lookup = lookup;
			return this;
		}

		/**
		 * @param exceptionFunction function that returns a {@link RuntimeException} to throw when lookup fails
		 */
		public LookupCacheFactoryConfig<K,V> withExceptionFunction(Function<K,RuntimeException> exceptionFunction){
			Objects.requireNonNull(exceptionFunction);
			this.exceptionFunction = exceptionFunction;
			return this;
		}

		/**
		 * @param maximumSize maximum number of entries to store
		 */
		public LookupCacheFactoryConfig<K,V> withMaximumSize(long maximumSize){
			this.maximumSize = maximumSize;
			return this;
		}

		//TODO formatter complains about space before '(' in javadoc
		/**
		 * @param refreshAfterWriteDuration maximum duration that a V is returned for after executing lookup
		 */
		public LookupCacheFactoryConfig<K,V> withRefreshAfterWriteDuration(Duration refreshAfterWriteDuration){
			Objects.requireNonNull(refreshAfterWriteDurationMs);
			this.refreshAfterWriteDurationMs = refreshAfterWriteDuration.toMillis();
			return this;
		}

		/**
		 * @param expireAfterWriteDuration time until cache will attempt to asynchronously reload lookup during reads
		 */
		public LookupCacheFactoryConfig<K,V> withExpireAfterWriteDuration(Duration expireAfterWriteDuration){
			Objects.requireNonNull(expireAfterWriteDurationMs);
			this.expireAfterWriteDurationMs = expireAfterWriteDuration.toMillis();
			return this;
		}

		public LookupCache<K,V> buildWithFactory(LookupCacheFactory factory){
			return factory.build(this);
		}

	}

	/**
	 * A cache that asynchronously loads V based on K. See {@link LookupCacheFactoryConfig} for configuration details.
	 */
	public static class LookupCache<K,V> implements LookupCacheGetters<K,V>{

		private static final Logger logger = LoggerFactory.getLogger(LookupCache.class);

		private final LoadingCache<K,Optional<V>> cache;
		private final Function<K,RuntimeException> exceptionFunction;

		private LookupCache(LoadingCache<K,Optional<V>> cache, Function<K,RuntimeException> exceptionFunction){
			this.cache = cache;
			this.exceptionFunction = exceptionFunction;
		}

		/**
		 * returns V associated with K if present or throws configured exception (see
		 * {@link LookupCacheFactoryConfig#withExceptionFunction(Function)}) if not present
		 */
		@Override
		public V getOrThrow(K key){
			return getInternal(key).orElseThrow(() -> {
				return exceptionFunction.apply(key);
			});
		}

		/**
		 * @return {@link Optional} containing V associated with K if present or empty {@link Optional} if not present
		 */
		@Override
		public Optional<V> get(K key){
			return getInternal(key);
		}

		private Optional<V> getInternal(K key){
			Objects.requireNonNull(key);
			Optional<V> value = cache.getUnchecked(key);
			//force subsequent calls to (synchronously) load the value, instead of caching the absent value
			if(!value.isPresent()){
				cache.invalidate(key);
			}
			return value;
		}

		@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
		public static class LookupCacheIntegrationTests{

			private static final RuntimeException customException = new RuntimeException("custom");
			private static final RuntimeException unexpectedException = new RuntimeException("unexpected");

			@Inject
			LookupCacheFactory factory;

			@Test
			private void testFailedLookups(){
				LookupCache<String,String> cache = buildCache(newTestTicker(), key -> null);
				Assert.assertEquals(cache.get(""), Optional.empty());
				try{
					cache.getOrThrow("");
					Assert.fail();
				}catch(RuntimeException e){
					Assert.assertEquals(e, customException);
				}
			}

			@Test
			private void testRuntimeExceptionBehavior(){
				LookupCache<String,String> cache = buildCache(newTestTicker(), key -> {
					throw unexpectedException;});
				try{
					cache.get("");
					Assert.fail();
				}catch(RuntimeException e){
					Assert.assertEquals(e.getClass(), UncheckedExecutionException.class);
					Assert.assertEquals(e.getCause(), unexpectedException);
				}
				try{
					cache.getOrThrow("");
					Assert.fail();
				}catch(RuntimeException e){
					Assert.assertEquals(e.getClass(), UncheckedExecutionException.class);
					Assert.assertEquals(e.getCause(), unexpectedException);
				}
			}

			@Test
			private void testSuccessfulLookups(){
				LookupCache<String,String> cache = buildCache(newTestTicker(), key -> "");
				Assert.assertEquals(cache.get(""), Optional.of(""));
				Assert.assertEquals(cache.getOrThrow(""), "");
			}

			private LookupCache<String,String> buildCache(Ticker ticker, Function<String,String> lookupFunction){
				LookupCacheFactoryConfig<String,String> config = getSharedConfig().withLookup(lookupFunction);
				return factory.buildForTest(config, ticker);
			}

			private static LookupCacheFactoryConfig<String,String> getSharedConfig(){
				LookupCacheFactoryConfig<String,String> config = new LookupCacheFactoryConfig<>();
				config.withExpireAfterWriteDuration(Duration.ofMillis(4))
						.withRefreshAfterWriteDuration(Duration.ofMillis(2))
						.withExceptionFunction(key -> customException);
				return config;
			}

			//TODO not used yet (DATAROUTER-769)
			private static Ticker newTestTicker(){
				return new Ticker(){

					int time = 0;

					@Override
					public long read(){
						return time++;
					}

				};
			}

		}

	}
}
