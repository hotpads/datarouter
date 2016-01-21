package com.hotpads.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.inject.DatarouterInjector;

public abstract class BaseDispatcher{
		
	private static final String REGEX_ONE_DIRECTORY = "[/]?[^/]*";

	private DatarouterInjector injector;
	private String servletContextPath;
	private String combinedPrefix;
	private Map<Pattern, Class<? extends BaseHandler>> handlerByClass;
	private Class<? extends BaseHandler> defaultHandlerClass;
	private List<DispatchRule> dispatchRules;

	public BaseDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		this.injector = injector;
		this.servletContextPath = servletContextPath;
		this.combinedPrefix = servletContextPath + urlPrefix;
		this.handlerByClass = new HashMap<>();
		this.dispatchRules = new ArrayList<>();
	}

	protected BaseDispatcher handleOthers(Class<? extends BaseHandler> defaultHandlerClass){
		this.defaultHandlerClass = defaultHandlerClass;
		return this;
	}
	
	protected DispatchRule handleDir(String regex){
		return handle(regex + REGEX_ONE_DIRECTORY);
	}

	protected DispatchRule handle(String regex){
		DispatchRule rule = new DispatchRule(regex);
		this.dispatchRules.add(rule);
		return rule;
	}

	public boolean handleRequestIfUrlMatch(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response){
		String uri = request.getRequestURI();
		if (!uri.startsWith(combinedPrefix)){
			return false;
		}
		BaseHandler handler = null;
		for (Map.Entry<Pattern, Class<? extends BaseHandler>> entry : handlerByClass.entrySet()){
			String afterPrefix = uri.substring(servletContextPath.length());
			if (entry.getKey().matcher(afterPrefix).matches()){
				handler = injector.getInstance(entry.getValue());
				break;
			}
		}
		for (DispatchRule rule : dispatchRules){
			String afterPrefix = uri.substring(servletContextPath.length());
			if (rule.getPattern().matcher(afterPrefix).matches()){
				if (!rule.apply(request)){
					return false;
				}
				handler = injector.getInstance(rule.getHandlerClass());
				break;
			}
		}
		
		if (handler == null){
			if (defaultHandlerClass == null){
				return false;// url not found
			}
			handler = injector.getInstance(defaultHandlerClass);
		}
		
		handler.setRequest(request);
		handler.setResponse(response);
		try{
			handler.setOut(response.getWriter());
		}catch (IOException e){
			throw new RuntimeException(e);
		}
		handler.setServletContext(servletContext);
		handler.setParams(new Params(request, response));
		handler.handleWrapper();
		return true;
	}
	
	public List<DispatchRule> getDispatchRules(){
		return this.dispatchRules;
	}
}
