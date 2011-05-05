package com.hotpads.handler;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;
import com.hotpads.util.core.MapTool;

public abstract class Dispatcher{

	protected Injector injector;
	protected String servletContextPath, urlPrefix, combinedPrefix;
	protected Map<Pattern,Class<? extends BaseHandler>> handlerByClass;
	 
	public Dispatcher(Injector injector, String servletContextPath, String urlPrefix){
		this.injector = injector;
		this.servletContextPath = servletContextPath;
		this.urlPrefix = urlPrefix;
		this.combinedPrefix = servletContextPath + urlPrefix;
		this.handlerByClass = MapTool.createHashMap();
	}
	
	protected Dispatcher handle(String regex, Class<? extends BaseHandler> handlerClass){
		Pattern pattern = Pattern.compile(regex);
		handlerByClass.put(pattern, handlerClass);
		return this;
	}
	
	protected Dispatcher handleOthers(Class<? extends BaseHandler> handlerClass){
		handle("", handlerClass);
		return this;
	}
	
	public void handleRequestIfUrlMatch(ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response){
		String uri = request.getRequestURI();
		if(!uri.startsWith(combinedPrefix)){ return; }
		BaseHandler handler = null;
		for(Map.Entry<Pattern,Class<? extends BaseHandler>> entry : handlerByClass.entrySet()){
			String afterPrefix = uri.substring(combinedPrefix.length());
			if(entry.getKey().matcher(afterPrefix).matches()){
//				handler = ReflectionTool.create(entry.getValue());
				handler = injector.getInstance(entry.getValue());
				break;
			}
		}
		if(handler==null){
			//url not found
			return;
		}
		
		Params params = new Params(request, response);
		handler.setRequest(request);
		handler.setResponse(response);
		try{
			handler.setOut(response.getWriter());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		handler.setServletContext(servletContext);
		handler.setParams(params);
		handler.handleWrapper();
	}
}
