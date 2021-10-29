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

	private final List<Class<? extends DatarouterAppListener>> listenerClasses;
	private final List<Class<? extends DatarouterWebAppListener>> webListenerClasses;
	private final List<DatarouterAppListener> listenersToShutdown;
	private final List<Pair<ShutdownMode,List<DatarouterAppListener>>> listenersByShutdownMods;

	public BaseDatarouterServletContextListener(
			List<Class<? extends DatarouterAppListener>> listenerClasses,
			List<Class<? extends DatarouterWebAppListener>> webListenerClasses){
		this.listenerClasses = listenerClasses;
		this.webListenerClasses = webListenerClasses;
		this.listenersToShutdown = new ArrayList<>();
		this.listenersByShutdownMods = new ArrayList<>();
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		DatarouterInjector injector = getInjector(event.getServletContext());
		var timer = new PhaseTimer();
		Scanner.of(listenerClasses)
				.map(injector::getInstance)
				.each(listenersToShutdown::add)
				.each(DatarouterAppListener::onStartUp)
				.map(Object::getClass)
				.map(Class::getSimpleName)
				.forEach(timer::add);
		Scanner.of(webListenerClasses)
				.map(injector::getInstance)
				.each(listenersToShutdown::add)
				.each(listener -> listener.setServletContext(event.getServletContext()))
				.each(DatarouterWebAppListener::onStartUp)
				.map(Object::getClass)
				.map(Class::getSimpleName)
				.forEach(timer::add);
		logger.warn("startUp {}", timer);

		Scanner.of(listenersToShutdown)
				.reverse()
				.splitBy(DatarouterAppListener::safeToShutdownInParallel)
				.map(Scanner::list)
				.map(listeners -> new Pair<>(
						listeners.get(0).safeToShutdownInParallel() ? ShutdownMode.PARALLEL : ShutdownMode.SYNCHRONOUS,
						listeners))
				.forEach(listenersByShutdownMods::add);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		ThreadFactory factory = new NamedThreadFactory("datarouterListenerShutdownExecutor", false);
		ExecutorService executor = Executors.newFixedThreadPool(listenersToShutdown.size(), factory);
		var timer = new PhaseTimer();
		long shutdownStartMillis = System.currentTimeMillis();
		for(Pair<ShutdownMode,List<DatarouterAppListener>> listenersByShutdownMode : listenersByShutdownMods){
			List<DatarouterAppListener> listeners = listenersByShutdownMode.getRight();
			ShutdownMode shutdownMode = listenersByShutdownMode.getLeft();
			logger.warn("shutting down {}: [{}", shutdownMode.display, listeners.stream()
					.map(listener -> listener.getClass().getSimpleName())
					.collect(Collectors.joining(", ")) + "]");
			if(shutdownMode == ShutdownMode.SYNCHRONOUS){
				Scanner.of(listeners)
						.map(listener -> {
							String className = listener.getClass().getSimpleName();
							logger.warn("shutting down listener={}", className);
							var phaseTimer = new PhaseTimer();
							listener.onShutDown();
							return phaseTimer.add(className);
						})
						.forEach(timer::add);
			}else if(shutdownMode == ShutdownMode.PARALLEL){
				long shutdownParallelStartMillis = System.currentTimeMillis();
				Scanner.of(listeners)
						.parallel(new ParallelScannerContext(executor, listeners.size(), true))
						.map(listener -> {
							String className = listener.getClass().getSimpleName();
							logger.warn("shutting down listener={}", className);
							var phaseTimer = new PhaseTimer();
							listener.onShutDown();
							return phaseTimer.add(className);
						})
						.forEach(timer::add);
				//TODO remove
				logger.warn("Parallel shutDown total={}", System.currentTimeMillis() - shutdownParallelStartMillis);
			}
		}
		logger.warn(String.format("shutDown [total=%d][%s]", System.currentTimeMillis() - shutdownStartMillis,
				timer.getPhaseNamesAndTimes().stream()
						.map(pair -> pair.getLeft() + "=" + pair.getRight())
						.collect(Collectors.joining("]["))));
		ExecutorServiceTool.shutdown(executor, Duration.ofSeconds(2));
		listenersByShutdownMods.clear();
		listenersToShutdown.clear();
	}

	private enum ShutdownMode{
		SYNCHRONOUS("synchronously"),
		PARALLEL("in parallel");

		public final String display;

		ShutdownMode(String display){
			this.display = display;
		}

	}

}
