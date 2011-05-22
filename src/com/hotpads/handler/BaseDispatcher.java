package com.hotpads.handler;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;
import com.hotpads.util.core.MapTool;

public abstract class BaseDispatcher{

	protected Injector injector;
	protected String servletContextPath, urlPrefix, combinedPrefix;
	protected Map<Pattern,Class<? extends BaseHandler>> handlerByClass;
	protected Class<? extends BaseHandler> defaultHandlerClass;
	 
	public BaseDispatcher(Injector injector, String servletContextPath, String urlPrefix){
		this.injector = injector;
		this.servletContextPath = servletContextPath;
		this.urlPrefix = urlPrefix;
		this.combinedPrefix = servletContextPath + urlPrefix;
		this.handlerByClass = MapTool.createHashMap();
	}
	
	protected BaseDispatcher handle(String regex, Class<? extends BaseHandler> handlerClass){
		Pattern pattern = Pattern.compile(regex);
		handlerByClass.put(pattern, handlerClass);
		return this;
	}
	
	protected BaseDispatcher handleOthers(Class<? extends BaseHandler> defaultHandlerClass){
		this.defaultHandlerClass = defaultHandlerClass;
		return this;
	}
	
	public boolean handleRequestIfUrlMatch(ServletContext servletContext,
			HttpServletRequest request, HttpServletResponse response){
		String uri = request.getRequestURI();
		if(!uri.startsWith(combinedPrefix)){ return false; }
		BaseHandler handler = null;
		for(Map.Entry<Pattern,Class<? extends BaseHandler>> entry : handlerByClass.entrySet()){
			String afterPrefix = uri.substring(servletContextPath.length());
			if(entry.getKey().matcher(afterPrefix).matches()){
				handler = injector.getInstance(entry.getValue());
				break;
			}
		}
		if(handler==null){
			if(defaultHandlerClass==null){
				return false;//url not found
			}else{
				handler = injector.getInstance(defaultHandlerClass);
			}
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
		return true;
	}
}
