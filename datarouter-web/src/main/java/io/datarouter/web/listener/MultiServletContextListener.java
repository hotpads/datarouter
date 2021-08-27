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
package io.datarouter.web.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.util.timer.PhaseTimer;

/**
 * Allow control of the Listeners order when using {@link javax.servlet.annotation.WebListener}
 */
public abstract class MultiServletContextListener implements ServletContextListener{
	private static final Logger logger = LoggerFactory.getLogger(MultiServletContextListener.class);

	private final List<ServletContextListener> listeners;

	public MultiServletContextListener(List<ServletContextListener> listeners){
		this.listeners = new ArrayList<>(listeners); // needs to be mutable
	}

	@Override
	public void contextInitialized(ServletContextEvent sce){
		Counters.inc("bootstrap");
		Counters.inc("bootstrap start");
		PhaseTimer timer = new PhaseTimer();
		listeners.forEach(listener -> {
			listener.contextInitialized(sce);
			timer.add(listener.getClass().getSimpleName());
		});
		logger.warn("startUp {}", timer);
		Counters.inc("bootstrap end");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce){
		Counters.inc("shutdown");
		Counters.inc("shutdown start");
		Collections.reverse(listeners);
		PhaseTimer timer = new PhaseTimer();
		listeners.forEach(listener -> {
			listener.contextDestroyed(sce);
			timer.add(listener.getClass().getSimpleName());
		});
		// logging framework will probably be off at this point, so using the container logging
		sce.getServletContext().log("shutDown " + timer);
		Counters.inc("shutdown end");
	}

}
