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
package io.datarouter.autoconfig.web;

import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.autoconfig.config.DatarouterAutoConfigExecutors.AutoConfigExecutor;
import io.datarouter.autoconfig.service.AutoConfigService;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.RawStringEncoder;
import io.datarouter.web.handler.types.Param;

public class DatarouterAutoConfigHandler extends BaseHandler{

	public static final String P_name = "name";

	@Inject
	private AutoConfigService autoConfigService;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private AutoConfigExecutor executor;

	@Handler(defaultHandler = true, encoder = RawStringEncoder.class)
	private String home(){
		serverTypeDetector.assertNotProductionServer();
		return Scanner.of(autoConfigService.getAutoConfigByName().entrySet())
				.map(Entry::getValue)
				.parallel(new ParallelScannerContext(executor, 8, true))
				.map(callable -> {
					try{
						return callable.call();
					}catch(Exception e){
						// not possible
						return null;
					}
				})
				.collect(Collectors.joining("\n"));
	}

	@Handler(encoder = RawStringEncoder.class)
	public String runForName(@Param(P_name) String name) throws Exception{
		serverTypeDetector.assertNotProductionServer();
		Callable<String> callable = autoConfigService.getAutoConfigByName().get(name);
		return callable.call();
	}

}
