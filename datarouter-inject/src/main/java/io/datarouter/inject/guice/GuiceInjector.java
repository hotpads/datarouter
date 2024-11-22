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
package io.datarouter.inject.guice;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import io.datarouter.inject.DatarouterInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GuiceInjector implements DatarouterInjector{
	private static final Logger logger = LoggerFactory.getLogger(GuiceInjector.class);

	private final Injector injector;

	@Inject
	public GuiceInjector(Injector injector){
		this.injector = injector;
	}

	@Override
	public <T>T getInstance(Class<T> clazz){
		long startMs = System.currentTimeMillis();
		T instance = injector.getInstance(clazz);
		long durationMs = System.currentTimeMillis() - startMs;
		if(durationMs > 10_000){
			logger.warn("Long injection class={} ms={}", clazz.getSimpleName(), durationMs);
		}
		return instance;
	}

	@Override
	public <T> Map<String,T> getInstancesOfType(Class<T> type){
		return GuiceTool.getInstancesOfType(injector, type);
	}

	@Override
	public void injectMembers(Object instance){
		injector.injectMembers(instance);
	}

}
