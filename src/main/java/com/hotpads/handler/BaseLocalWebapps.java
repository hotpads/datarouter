package com.hotpads.handler;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import javax.servlet.ServletContext;

import com.google.inject.Provider;
import com.hotpads.util.core.ListTool;
import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Li;
import com.hp.gagawa.java.elements.Ul;

public class BaseLocalWebapps{

	public static class BaseLocalWebAppsProvider implements Provider<BaseLocalWebapps>{
		private ServletContext servletContext;
		public BaseLocalWebAppsProvider(ServletContext servletContext){
			this.servletContext=servletContext;
		}
		@Override
		public BaseLocalWebapps get(){
			return new BaseLocalWebapps(servletContext);
		}
	}
	
	protected List<String> localWebApps = ListTool.create();
	protected ServletContext servletContext;

	public BaseLocalWebapps(){
	}
	public BaseLocalWebapps(ServletContext servletContext){
		this.servletContext = servletContext;
		String path = servletContext.getRealPath("");
		this.localWebApps = ListTool.createArrayList(listWebapps(path));
		servletContext.setAttribute("commonNavbarHtml", createHtml().write());
	}

	protected Node createHtml(){
		Ul ul = new Ul();
		Li li;
		A a;
		String webApp;
		for(int index = 0; index < localWebApps.size(); index++){
			webApp = localWebApps.get(index);
			li = new Li();
			li.setId("common-menu-" + webApp);
			a = new A();
			a.setHref(getHref(webApp));
			a.setTitle("Go to " + getName(webApp));
			a.setCSSClass(getCssClass(webApp));
			a.appendText(getName(webApp));
			li.appendChild(a);
			ul.appendChild(li);

		}

		li = new Li();
		li.appendText("|");
		ul.appendChild(li);
		li = new Li();
		li.setId("common-menu-datarouter");
		a = new A();
		a.setHref(servletContext.getContextPath() + "/datarouter");
		a.setTitle("Go to datarouter");
		a.setCSSClass("isLocal");
		a.appendText(getName("Datarouter"));
		li.appendChild(a);
		ul.appendChild(li);
		return ul;
	}

	protected String getName(String webApp){
		if(webApp.toLowerCase().equals("root")){
			return "/";
		}else{
			return webApp;
		}
	}

	protected String getCssClass(String webApp){
		return "isLocal";

	}

	protected String getHref(String webApp){
		if(webApp.equals("root")){ return "/"; }
		return "/" + webApp;

	}

	protected boolean isLocal(String webApp){
		return localWebApps.contains(webApp);
	}

	protected List<String> listWebapps(String pathCurrentWebApp){
		File file = new File(pathCurrentWebApp + "/..");
		String[] webapps = file.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return new File(dir, name).isDirectory();
			}
		});
		List<String> directories = ListTool.create();
		for(String webapp : webapps){
			if(webapp.toLowerCase().equals("root")){
				if(checkRootProjectDeployed(pathCurrentWebApp + "/../ROOT")){
					directories.add("ROOT");
				}
			}else{
				directories.add(webapp);
			}
		}

		return directories;
	}

	protected boolean checkRootProjectDeployed(String rootFolderPath){
		return true;
	}

}
