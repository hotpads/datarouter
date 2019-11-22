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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.inject.InjectorRetriever;

/**
 * Use this to configure ServletContextListeners in Java rather than web.xml.  Listeners should implement the
 * DatarouterWebAppListener interface and be added to the getWebAppListeners() method
 */
public abstract class BaseDatarouterServletContextListener implements ServletContextListener, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterServletContextListener.class);

	private final List<Class<? extends DatarouterAppListener>> listenerClasses;
	private final List<Class<? extends DatarouterWebAppListener>> webListenerClasses;

	private List<DatarouterAppListener> listenersToShutdown;

	public BaseDatarouterServletContextListener(
			List<Class<? extends DatarouterAppListener>> listenerClasses,
			List<Class<? extends DatarouterWebAppListener>> webListenerClasses){
		this.listenerClasses = listenerClasses;
		this.webListenerClasses = webListenerClasses;
		this.listenersToShutdown = new ArrayList<>();
	}

	@Override
	public void contextInitialized(ServletContextEvent event){
		DatarouterInjector injector = getInjector(event.getServletContext());

		PhaseTimer timer = new PhaseTimer();

		for(Class<? extends DatarouterAppListener> listenerClass : listenerClasses){
			DatarouterAppListener listener = injector.getInstance(listenerClass);
			listenersToShutdown.add(listener);
			listener.onStartUp();
			timer.add(listener.getClass().getSimpleName());
		}

		for(Class<? extends DatarouterWebAppListener> listenerClass : webListenerClasses){
			DatarouterWebAppListener listener = injector.getInstance(listenerClass);
			listenersToShutdown.add(listener);
			listener.setServletContext(event.getServletContext());
			listener.onStartUp();
			timer.add(listener.getClass().getSimpleName());
		}
		logger.warn("startUp {}", timer);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){
		Collections.reverse(listenersToShutdown);
		PhaseTimer timer = new PhaseTimer();
		for(DatarouterAppListener listener : listenersToShutdown){
			listener.onShutDown();
			timer.add(listener.getClass().getSimpleName());
		}
		logger.warn("shutDown {}", timer);
		listenersToShutdown.clear();
		listenersToShutdown = null;
	}

	public List<Class<? extends DatarouterAppListener>> getAppListenerClasses(){
		List<Class<? extends DatarouterAppListener>> list = new ArrayList<>();
		list.addAll(listenerClasses);
		list.addAll(webListenerClasses);
		return list;
	}

}
