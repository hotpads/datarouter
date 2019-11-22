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
package io.datarouter.web.listener;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.inject.InstanceRegistry;
import io.datarouter.util.concurrent.ExecutorServiceTool;

@Singleton
public class ExecutorsAppListener implements DatarouterAppListener{

	@Inject
	private InstanceRegistry instanceRegistry;

	@Override
	public void onShutDown(){
		for(ExecutorService executor : instanceRegistry.getAllInstancesOfType(ExecutorService.class)){
			ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(5));
		}
	}

}
