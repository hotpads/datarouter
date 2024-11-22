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
package io.datarouter.autoconfig.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.autoconfig.config.DatarouterAutoConfigExecutors.AutoConfigExecutor;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.servertype.ServerTypeDetector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AutoConfigService{
	private static final Logger logger = LoggerFactory.getLogger(AutoConfigService.class);

	private final ServerTypeDetector serverTypeDetector;
	private final AutoConfigExecutor executor;
	private final ChangelogRecorder changelogRecorder;

	private final Map<String,Callable<String>> autoConfigByName;

	@Inject
	public AutoConfigService(
			PluginInjector pluginInjector,
			ServerTypeDetector serverTypeDetector,
			AutoConfigExecutor executor,
			ChangelogRecorder changelogRecorder){
		this.serverTypeDetector = serverTypeDetector;
		this.executor = executor;
		this.changelogRecorder = changelogRecorder;

		autoConfigByName = new HashMap<>();

		pluginInjector.getInstances(AutoConfig.KEY)
				.forEach(autoConfig -> autoConfigByName.put(autoConfig.getName(), autoConfig));
		pluginInjector.getInstances(AutoConfigGroup.KEY)
				.forEach(autoConfigGroup -> autoConfigByName.put(autoConfigGroup.getName(), autoConfigGroup));
	}

	public Map<String,Callable<String>> getAutoConfigByName(){
		return autoConfigByName;
	}

	public String runAutoConfigAll(String triggerer){
		serverTypeDetector.assertNotProductionServer();
		return Scanner.of(getAutoConfigByName().entrySet())
				.parallelUnordered(new Threads(executor, 8))
				.map(entry -> {
					try{
						return runInternal(entry.getKey(), entry.getValue(), triggerer, "all");
					}catch(Exception e){
						// not possible
						return null;
					}
				})
				.collect(Collectors.joining("\n"));
	}

	public String runAutoConfigForName(String name, String triggerer) throws Exception{
		serverTypeDetector.assertNotProductionServer();
		Callable<String> callable = autoConfigByName.get(name);
		return runInternal(name, callable, triggerer, "single");
	}

	private String runInternal(
			String name,
			Callable<String> callable,
			String triggerer,
			String mode)
	throws Exception{
		Metrics.count("AutoConfig start " + name);
		logger.warn("running autoconfig name={} triggerer={} mode={}", name, triggerer, mode);
		String autoConfig = callable.call();
		var changelogDto = new DatarouterChangelogDtoBuilder(
				"AutoConfig",
				name,
				"triggered",
				triggerer)
				.build();
		changelogRecorder.record(changelogDto);
		return autoConfig;
	}

}
