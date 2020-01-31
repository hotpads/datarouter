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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.spi.Message;

import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.timer.PhaseTimer;

/**
 * Allow control of the Listeners order when using {@link javax.servlet.annotation.WebListener}
 */
public abstract class MultiServletContextListener implements ServletContextListener{
	private static final Logger logger = LoggerFactory.getLogger(MultiServletContextListener.class);

	private final List<ServletContextListener> listeners;

	public MultiServletContextListener(List<ServletContextListener> listeners){
		this.listeners = listeners;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce){
		PhaseTimer timer = new PhaseTimer();
		listeners.forEach(listener -> {
			try{
				listener.contextInitialized(sce);
			}catch(RuntimeException e){
				// workaround guice old asm version
				try{
					Collection<Message> messages = (Collection<Message>)ReflectionTool.get("messages", e);
					for(Message message : messages){
						logger.error("{} ", message.getMessage(), message.getCause());
					}
				}catch(Exception relectionException){
					// not a guice exception, throw the original
					throw e;
				}
				throw new RuntimeException("guice exception, see previous logs");
			}
			timer.add(listener.getClass().getSimpleName());
		});
		logger.warn("startUp {}", timer);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce){
		Collections.reverse(listeners);
		PhaseTimer timer = new PhaseTimer();
		listeners.forEach(listener -> {
			listener.contextDestroyed(sce);
			timer.add(listener.getClass().getSimpleName());
		});
		// logging framework will probably be off at this point, so using the container logging
		sce.getServletContext().log("shutDown " + timer);
	}

}
