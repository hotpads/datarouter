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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.inject.InjectorRetriever;

/**
 * Use this to configure ServletContextListeners in Java rather than web.xml.  Listeners should implement the
 * DatarouterWebAppListener interface and be added to the getWebAppListeners() method
 */
public abstract class BaseDatarouterServletContextListener implements ServletContextListener, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterServletContextListener.class);

	private static final boolean STARTUP_ALL_LISTENERS_SYNCHRONOUSLY = false;
	private static final boolean SHUTDOWN_ALL_LISTENERS_SYNCHRONOUSLY = false;

	private final List<Class<? extends DatarouterAppListener>> listenerClasses;
	private final List<Class<? extends DatarouterWebAppListener>> webListenerClasses;
	private final List<DatarouterAppListener> allListeners;
	private final List<Pair<ExecutionMode,List<DatarouterAppListener>>> listenersByExecutionMods;

	public BaseDatarouterServletContextListener(
			List<Class<? extends DatarouterAppListener>> listenerClasses,
			List<Class<? extends DatarouterWebAppListener>> webListenerClasses){
		this.listenerClasses = listenerClasses;
		this.webListenerClasses = webListenerClasses;
		this.allListeners = new ArrayList<>();
		this.listenersByExecutionMods = new ArrayList<>();
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		buildExecuteOnActionsLists(event);
		processListeners(OnAction.STARTUP, STARTUP_ALL_LISTENERS_SYNCHRONOUSLY);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		Collections.reverse(listenersByExecutionMods);
		listenersByExecutionMods.forEach(pair -> Collections.reverse(pair.getRight()));
		processListeners(OnAction.SHUTDOWN, SHUTDOWN_ALL_LISTENERS_SYNCHRONOUSLY);
		listenersByExecutionMods.clear();
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
				.map(Scanner::list)
				.map(listeners -> new Pair<>(
						listeners.get(0).safeToExecuteInParallel() ? ExecutionMode.PARALLEL : ExecutionMode.SYNCHRONOUS,
						listeners))
				.forEach(listenersByExecutionMods::add);
	}

	private void processListeners(OnAction onAction, boolean executeAllListenersSynchronously){
		ThreadFactory factory = new NamedThreadFactory("datarouterListenerExecutor", false);
		ExecutorService executor = Executors.newFixedThreadPool(allListeners.size(), factory);
		var timer = new PhaseTimer();
		long shutdownStartMillis = System.currentTimeMillis();
		for(Pair<ExecutionMode,List<DatarouterAppListener>> listenersByShutdownMode : listenersByExecutionMods){
			List<DatarouterAppListener> listeners = listenersByShutdownMode.getRight();
			ExecutionMode executionMode = executeAllListenersSynchronously
					? ExecutionMode.SYNCHRONOUS
					: listenersByShutdownMode.getLeft();
			logger.warn("{} {}: [{}", onAction.display, executionMode.display, listeners.stream()
					.map(listener -> listener.getClass().getSimpleName())
					.collect(Collectors.joining(", ")) + "]");
			if(executionMode == ExecutionMode.SYNCHRONOUS){
				Scanner.of(listeners)
						.map(executeOnAction(onAction))
						.forEach(timer::add);
			}else if(executionMode == ExecutionMode.PARALLEL){
				long shutdownParallelStartMillis = System.currentTimeMillis();
				Scanner.of(listeners)
						.parallel(new ParallelScannerContext(executor, listeners.size(), true))
						.map(executeOnAction(onAction))
						.forEach(timer::add);
				logger.info("Parallel {} total={}", onAction.display,
						System.currentTimeMillis() - shutdownParallelStartMillis);
			}
		}
		logger.warn(String.format("%s [total=%d][%s]", onAction, System.currentTimeMillis() - shutdownStartMillis,
				timer.getPhaseNamesAndTimes().stream()
						.map(pair -> pair.getLeft() + "=" + pair.getRight())
						.collect(Collectors.joining("]["))));
		ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(2));
	}

	private Function<DatarouterAppListener,PhaseTimer> executeOnAction(OnAction onAction){
		return listener -> {
			String className = listener.getClass().getSimpleName();
			logger.info("{} listener={}", onAction.display, className);
			var phaseTimer = new PhaseTimer();
			if(onAction == OnAction.STARTUP){
				listener.onStartUp();
			}else if(onAction == OnAction.SHUTDOWN){
				listener.onShutDown();
			}
			return phaseTimer.add(className);
		};
	}

	private enum OnAction{
		STARTUP("starting"),
		SHUTDOWN("shutting down");

		private final String display;

		OnAction(String display){
			this.display = display;
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
