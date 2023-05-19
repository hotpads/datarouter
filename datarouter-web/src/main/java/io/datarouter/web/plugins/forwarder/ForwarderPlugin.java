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
package io.datarouter.web.plugins.forwarder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.TypeLiteral;

import io.datarouter.web.config.BaseWebPlugin;

public class ForwarderPlugin extends BaseWebPlugin{

	private final List<Class<? extends ForwarderPluginInterceptor>> interceptors;

	public ForwarderPlugin(List<Class<? extends ForwarderPluginInterceptor>> interceptors){
		this.interceptors = interceptors;
		addRouteSet(ForwarderRouteSet.class);
	}

	@Override
	protected void configure(){
		bind(new TypeLiteral<List<Class<? extends ForwarderPluginInterceptor>>>(){}).toInstance(interceptors);
	}

	public static class ForwarderPluginBuilder{

		private final List<Class<? extends ForwarderPluginInterceptor>> interceptors = new ArrayList<>();

		public ForwarderPlugin build(){
			return new ForwarderPlugin(interceptors);
		}

		public ForwarderPluginBuilder addInterceptor(Class<? extends ForwarderPluginInterceptor> interceptor){
			interceptors.add(interceptor);
			return this;
		}

	}

	public interface ForwarderPluginInterceptor extends BiConsumer<HttpServletRequest,HttpServletResponse>{
	}

}
