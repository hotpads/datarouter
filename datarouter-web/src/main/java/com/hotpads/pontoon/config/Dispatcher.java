package com.hotpads.pontoon.config;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;

import com.hotpads.datarouter.config.ServletContextProvider;
import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.dispatcher.BaseDispatcherRoutes;
import com.hotpads.handler.dispatcher.DispatchRule;
import com.hotpads.handler.params.MultipartParams;
import com.hotpads.handler.params.Params;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionManager;
import com.hotpads.util.http.ResponseTool;

@Singleton
public class Dispatcher{

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private ServletContextProvider servletContextProvider;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private DatarouterSessionManager sessionManager;

	public boolean handleRequestIfUrlMatch(HttpServletRequest request, HttpServletResponse response,
			BaseDispatcherRoutes dispatcher) throws ServletException{
		String uri = request.getRequestURI();
		if(!uri.startsWith(servletContextProvider.get().getContextPath() + dispatcher.getUrlPrefix())){
			return false;
		}
		BaseHandler handler = null;
		String afterContextPath = uri.substring(servletContextProvider.get().getContextPath().length());
		for(DispatchRule rule : dispatcher.getDispatchRules()){
			if(rule.getPattern().matcher(afterContextPath).matches()){
				if(!rule.apply(request)){
					// TODO improve this. Right now it returns 404 and log "dispatcher could not find Handler for /uri"
					// while it's more an "access denied"
					return false;
				}
				if(authenticationConfig.useDatarouterAuthentication() && !checkRoles(rule, request)){
					handleMissingRoles(request, response);
					return true;
				}
				handler = injector.getInstance(rule.getHandlerClass());
				break;
			}
		}

		Class<? extends BaseHandler> defaultHandlerClass = dispatcher.getDefaultHandlerClass();
		if(handler == null){
			if(defaultHandlerClass == null){
				return false;// url not found
			}
			handler = injector.getInstance(defaultHandlerClass);
		}

		handler.setRequest(request);
		handler.setResponse(response);
		handler.setServletContext(servletContextProvider.get());
		if(isMultipart(request)){
			try{
				handler.setParams(new MultipartParams(request));
			}catch(FileUploadException e){
				throw new ServletException(e);
			}
		}else{
			handler.setParams(new Params(request));
		}
		handler.handleWrapper();
		return true;
	}

	private boolean checkRoles(DispatchRule dispatchRule, HttpServletRequest request){
		if(dispatchRule.getAllowAnonymous()){
			return true;
		}
		return sessionManager.getFromRequest(request)
				.map(DatarouterSession::getRoles)
				.map(roles -> roles.stream().anyMatch(dispatchRule.getAllowedRoles()::contains))
				.orElse(false);
	}

	private void handleMissingRoles(HttpServletRequest request, HttpServletResponse response){
		boolean isAnonymous = sessionManager.getFromRequest(request)
				.map(DatarouterSession::isAnonymous)
				.orElse(true);
		if(isAnonymous){
			String url = request.getRequestURL() + DrStringTool.nullSafe(request.getQueryString());
			sessionManager.addTargetUrlCookie(response, url);
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath()
					+ authenticationConfig.getSigninPath());
		}else{
			try{
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
	}

	private static boolean isMultipart(HttpServletRequest request){
		return request.getContentType() != null
				&& request.getContentType().toLowerCase().contains("multipart/form-data");
	}

}
