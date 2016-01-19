package com.hotpads.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.listener.DatarouterWebAppListener;

@Singleton
public class TomcatWebAppNamesWebAppListener extends DatarouterWebAppListener{
	public static final String SERVLET_CONTEXT_ATTRIBUTE_NAME = "webApps";

	private Map<String,String> webApps;
	
	public Map<String,String> getTomcatWebApps(){
		if(webApps != null){
			return webApps;
		}
		webApps = new HashMap<>();
		MBeanServer server = getTomcat();
		if(server == null){
			return webApps;
		}
		try{
			Set<ObjectName> modules = server.queryNames(new ObjectName("Catalina:j2eeType=WebModule,*"), null);
			for(ObjectName module : modules){
				String name = DrStringTool.getStringAfterLastOccurrence('/', module.getKeyProperty("name"));
				String href = "/" + name;
				if(name.equals("")){
					name = "site";
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
				if(domain.equals("Catalina")){
					return server;
				}
			}
		}
		return null;
	}

	@Override
	protected void onStartUp(){
		servletContext.setAttribute(SERVLET_CONTEXT_ATTRIBUTE_NAME, this);
	}

	@Override
	protected void onShutDown(){
		servletContext.removeAttribute(SERVLET_CONTEXT_ATTRIBUTE_NAME);
	}

}
