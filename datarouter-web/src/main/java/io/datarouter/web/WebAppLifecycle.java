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
package io.datarouter.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.web.DatarouterWebExecutors.WebAppLifecycleExecutor;
import io.datarouter.web.shutdown.ShutdownService;

@Singleton
public class WebAppLifecycle{
	private static final Logger logger = LoggerFactory.getLogger(WebAppLifecycle.class);

	private final WebAppLifecycleExecutor webAppLifecycleExecutor;

	private WebAppLifecycleState state = WebAppLifecycleState.BOOTING;
	private final Map<WebAppLifecycleState,List<WebAppLifecycleListener>> listeners = new HashMap<>();

	@Inject
	public WebAppLifecycle(WebAppLifecycleExecutor webAppLifecycleExecutor, ShutdownService shutdownService){
		this.webAppLifecycleExecutor = webAppLifecycleExecutor;
		addListener(WebAppLifecycleState.HTTP_READY, newState -> {
			logger.warn("making warmup http calls");
			// TODO make http calls
			set(WebAppLifecycleState.HTTP_WARMED);
		});
		addListener(WebAppLifecycleState.HTTP_WARMED, newState -> shutdownService.advance());
	}

	/**
	 * This method execute the state listeners in another thread. It will be return quickly.
	 * I use synchronization to advancing multiple time to the same state.
	 */
	public synchronized void set(WebAppLifecycleState newState){
		if(newState.ordinal() > state.ordinal()){
			state = newState;
			logger.warn("new state={}", state);
			webAppLifecycleExecutor.submit(() -> {
				listeners.getOrDefault(newState, List.of()).forEach(listener -> listener.process(newState));
			});
		}
	}

	public void addListener(WebAppLifecycleState state, WebAppLifecycleListener listener){
		listeners.computeIfAbsent(state, $ -> new ArrayList<>()).add(listener);
	}

}
