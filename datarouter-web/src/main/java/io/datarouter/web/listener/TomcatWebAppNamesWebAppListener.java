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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.datarouter.util.lazy.Lazy;
import io.datarouter.util.string.StringTool;

@Singleton
public class TomcatWebAppNamesWebAppListener extends DatarouterWebAppListener{

	public static final String SERVLET_CONTEXT_ATTRIBUTE_NAME = "webApps";

	public Map<String,String> getTomcatWebApps(){
		Map<String,String> webApps = new TreeMap<>();
		MBeanServer server = getTomcat();
		if(server == null){
			return webApps;
		}
		try{
			Set<ObjectName> modules = server.queryNames(new ObjectName("Catalina:j2eeType=WebModule,*"), null);
			for(ObjectName module : modules){
				String name = StringTool.getStringAfterLastOccurrence('/', module.getKeyProperty("name"));
				String href = "/" + name;
				if("".equals(name)){
					name = "ROOT";
				}
				webApps.put(name, href);
			}
		}catch(MalformedObjectNameException e){
			throw new RuntimeException(e);
		}
		return webApps;
	}

	private static MBeanServer getTomcat(){
		for(MBeanServer server : MBeanServerFactory.findMBeanServer(null)){
			for(String domain : server.getDomains()){
				if("Catalina".equals(domain)){
					return server;
				}
			}
		}
		return null;
	}

	@Override
	public void onStartUp(){
		servletContext.setAttribute(SERVLET_CONTEXT_ATTRIBUTE_NAME, Lazy.of(this::getTomcatWebApps));
	}

	@Override
	public void onShutDown(){
		servletContext.removeAttribute(SERVLET_CONTEXT_ATTRIBUTE_NAME);
	}

}
