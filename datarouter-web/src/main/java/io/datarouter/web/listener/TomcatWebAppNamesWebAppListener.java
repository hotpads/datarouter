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

import java.util.Map;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import io.datarouter.util.JmxTool;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Singleton;

@Singleton
public class TomcatWebAppNamesWebAppListener extends DatarouterWebAppListener{

	public static final String SERVLET_CONTEXT_ATTRIBUTE_NAME = "webApps";

	public Map<String,String> getTomcatWebApps(){
		Map<String,String> webApps = new TreeMap<>();
		MBeanServer server = getTomcat();
		if(server == null){
			return webApps;
		}
		for(ObjectName module : server.queryNames(JmxTool.newObjectName("Catalina:j2eeType=WebModule,*"), null)){
			String name = StringTool.getStringAfterLastOccurrence('/', module.getKeyProperty("name"));
			String href = "/" + name;
			if("".equals(name)){
				name = "ROOT";
			}
			webApps.put(name, href);
		}
		return webApps;
	}

	private static MBeanServer getTomcat(){
		for(String domain : JmxTool.SERVER.getDomains()){
			if("Catalina".equals(domain)){
				return JmxTool.SERVER;
			}
		}
		return null;
	}

	@Override
	public void onStartUp(){
		servletContext.setAttribute(SERVLET_CONTEXT_ATTRIBUTE_NAME, SingletonSupplier.of(this::getTomcatWebApps));
	}

	@Override
	public void onShutDown(){
		servletContext.removeAttribute(SERVLET_CONTEXT_ATTRIBUTE_NAME);
	}

	@Override
	public boolean safeToExecuteInParallel(){
		return false;
	}

}
