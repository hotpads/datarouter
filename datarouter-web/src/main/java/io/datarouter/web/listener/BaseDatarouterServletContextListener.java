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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.inject.InjectorRetriever;

/**
 * Use this to configure ServletContextListeners in Java rather than web.xml.  Listeners should implement the
 * DatarouterWebAppListener interface and be added to the getWebAppListeners() method
 */
public abstract class BaseDatarouterServletContextListener implements ServletContextListener, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterServletContextListener.class);

	private record ExecutionModeAndListeners(
			ExecutionMode executionMode,
			List<DatarouterAppListener> listeners){
	}

	private static final boolean STARTUP_ALL_LISTENERS_SYNCHRONOUSLY = true;
	private static final boolean SHUTDOWN_ALL_LISTENERS_SYNCHRONOUSLY = false;

	private final List<Class<? extends DatarouterAppListener>> listenerClasses;
	private final List<Class<? extends DatarouterWebAppListener>> webListenerClasses;
	private final List<DatarouterAppListener> allListeners;
	private final List<ExecutionModeAndListeners> listenersByExecutionMode;

	public BaseDatarouterServletContextListener(
			List<Class<? extends DatarouterAppListener>> listenerClasses,
			List<Class<? extends DatarouterWebAppListener>> webListenerClasses){
		this.listenerClasses = listenerClasses;
		this.webListenerClasses = webListenerClasses;
		this.allListeners = new ArrayList<>();
		this.listenersByExecutionMode = new ArrayList<>();
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		buildExecuteOnActionsLists(event);
		processListeners(OnAction.STARTUP, STARTUP_ALL_LISTENERS_SYNCHRONOUSLY);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		Collections.reverse(listenersByExecutionMode);
		listenersByExecutionMode.forEach(pair -> Collections.reverse(pair.listeners()));
		processListeners(OnAction.SHUTDOWN, SHUTDOWN_ALL_LISTENERS_SYNCHRONOUSLY);
		listenersByExecutionMode.clear();
		allListeners.clear();
	}

	private void buildExecuteOnActionsLists(ServletContextEvent event){
		DatarouterInjector injector = getInjector(event.getServletContext());
		Scanner.of(listenerClasses)
				.map(injector::getInstance)
				.forEach(allListeners::add);
		Scanner.of(webListenerClasses)
				.map(injector::getInstance)
				.each(listener -> listener.setServletContext(event.getServletContext()))
				.forEach(allListeners::add);

		Scanner.of(allListeners)
				.splitBy(DatarouterAppListener::safeToExecuteInParallel)
				.map(scanner -> scanner.collect(ArrayList::new))
				.map(listeners -> new ExecutionModeAndListeners(
						listeners.getFirst().safeToExecuteInParallel()
								? ExecutionMode.PARALLEL : ExecutionMode.SYNCHRONOUS,
						listeners))
				.forEach(listenersByExecutionMode::add);
	}

	private void processListeners(OnAction onAction, boolean executeAllListenersSynchronously){
		ThreadFactory factory = new NamedThreadFactory("datarouterListenerExecutor", false);
		ExecutorService executor = Executors.newFixedThreadPool(allListeners.size(), factory);
		for(ExecutionModeAndListeners listenersByShutdownMode : listenersByExecutionMode){
			List<DatarouterAppListener> listeners = listenersByShutdownMode.listeners();
			ExecutionMode executionMode = executeAllListenersSynchronously
					? ExecutionMode.SYNCHRONOUS
					: listenersByShutdownMode.executionMode();
			logger.info("{} {}: [{}]",
					onAction.display,
					executionMode.display,
					listeners.stream()
							.map(DatarouterAppListener::getClass)
							.map(Class::getSimpleName)
							.collect(Collectors.joining(", ")));
			var timer = new PhaseTimer();
			long startMs = System.currentTimeMillis();
			if(executionMode == ExecutionMode.SYNCHRONOUS){
				Scanner.of(listeners)
						.map(listener -> executeOnAction(onAction, listener))
						.forEach(timer::add);
			}else if(executionMode == ExecutionMode.PARALLEL){
				Scanner.of(listeners)
						.parallelUnordered(new Threads(executor, listeners.size()))
						.map(listener -> executeOnAction(onAction, listener))
						.forEach(timer::add);
			}
			long durationMs = System.currentTimeMillis() - startMs;
			logger.warn("finished {} {}, wallClockDurationMs={} {}",
					onAction.display,
					executionMode.display,
					durationMs,
					timer);
		}
		ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(2));
	}

	private PhaseTimer executeOnAction(OnAction onAction, DatarouterAppListener listener){
		String className = listener.getClass().getSimpleName();
		logger.info("starting {} listener={}", onAction.display, className);
		var phaseTimer = new PhaseTimer();
		var startTimeMs = System.currentTimeMillis();
		if(onAction == OnAction.STARTUP){
			listener.onStartUp();
		}else if(onAction == OnAction.SHUTDOWN){
			listener.onShutDown();
		}
		phaseTimer.add(className);
		long durationMs = System.currentTimeMillis() - startTimeMs;
		Log log = logger::info;
		if(durationMs > 1_000){
			log = logger::warn;
		}
		log.accept("finished {} listener={} durationMs={}", onAction.display, className, durationMs);
		Metrics.measure("AppListener %s %s durationMs".formatted(onAction.metricName, className), durationMs);
		return phaseTimer;
	}

	interface Log{
		void accept(String string, Object... objects);
	}

	private enum OnAction{
		STARTUP("starting", "startup"),
		SHUTDOWN("shutting down", "shutdown");

		private final String display;
		private final String metricName;

		OnAction(String display, String metricName){
			this.display = display;
			this.metricName = metricName;
		}

	}

	private enum ExecutionMode{
		SYNCHRONOUS("synchronously"),
		PARALLEL("in parallel");

		private final String display;

		ExecutionMode(String display){
			this.display = display;
		}

	}

}
