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
package io.datarouter.web.dispatcher;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.net.UrlTool;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.dispatcher.ApiKeyPredicate.ApiKeyPredicateCheck;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.params.MultipartParams;
import io.datarouter.web.handler.params.Params;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import io.datarouter.web.user.authenticate.saml.SamlService;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.util.RequestAttributeKey;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RequestTool;
import io.datarouter.web.util.http.ResponseTool;

@Singleton
public class Dispatcher{

	private static final String JSESSIONID_PATH_PARAM = ";jsessionid=";

	public static final RequestAttributeKey<Boolean> TRANSMITS_PII = new RequestAttributeKey<>("transmitsPii");

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterInjector injector;
	@Inject
	private DatarouterSessionManager sessionManager;
	@Inject
	private SamlService samlService;
	@Inject
	private DatarouterSamlSettings samlSettings;

	public RoutingResult handleRequestIfUrlMatch(
			HttpServletRequest request,
			HttpServletResponse response,
			RouteSet routeSet)
	throws ServletException, IOException{
		String uri = request.getRequestURI();
		BaseHandler handler = null;
		String afterContextPath = uri.substring(servletContext.get().getContextPath().length());
		if(afterContextPath.contains(JSESSIONID_PATH_PARAM)){
			afterContextPath = afterContextPath.substring(0, afterContextPath.indexOf(JSESSIONID_PATH_PARAM));
		}
		for(DispatchRule rule : routeSet.getDispatchRules()){
			if(rule.getPattern().matcher(afterContextPath).matches()){
				SecurityValidationResult securityCheckResult = rule.applySecurityValidation(request);
				request = securityCheckResult.getWrappedRequest();
				if(!securityCheckResult.isSuccess()){
					injector.getInstance(rule.getDefaultHandlerEncoder()).sendForbiddenResponse(request, response,
							securityCheckResult);
					return RoutingResult.FORBIDDEN;
				}
				if(authenticationConfig.useDatarouterAuthentication() && !rule.checkRoles(request)){
					handleMissingRoles(request, response, rule.getAllowedRoles());
					return RoutingResult.ROUTED;
				}
				handler = injector.getInstance(rule.getHandlerClass());
				handler.setDefaultHandlerEncoder(rule.getDefaultHandlerEncoder());
				handler.setDefaultHandlerDecoder(rule.getDefaultHandlerDecoder());
				RequestAttributeTool.set(request, BaseHandler.HANDLER_ENCODER_ATTRIBUTE, injector.getInstance(rule
						.getDefaultHandlerEncoder()));
				RequestAttributeTool.set(request, TRANSMITS_PII, rule.doesTransmitPii());
				if(rule.hasApiKey()){
					// TODO avoid re evaluating the rule
					ApiKeyPredicateCheck apiKeyPredicateExistsWithName = rule.getApiKeyPredicate().check(rule, request);
					if(apiKeyPredicateExistsWithName.allowed()){
						handler.setAccountName(apiKeyPredicateExistsWithName.accountName());
					}
				}
				break;
			}
		}

		Class<? extends BaseHandler> defaultHandlerClass = routeSet.getDefaultHandlerClass();
		if(handler == null){
			if(defaultHandlerClass == null){
				return RoutingResult.NOT_FOUND;
			}
			handler = injector.getInstance(defaultHandlerClass);
		}

		handler.setRequest(request);
		handler.setResponse(response);
		handler.setServletContext(servletContext.get());
		Params params = parseParams(request, handler.getDefaultMultipartCharset());
		handler.setParams(params);
		handler.handleWrapper();
		return RoutingResult.ROUTED;
	}

	private Params parseParams(HttpServletRequest request, Charset defaultCharset) throws ServletException{
		if(isMultipart(request)){
			try{
				return new MultipartParams(request, defaultCharset);
			}catch(FileUploadException e){
				throw new ServletException(e);
			}
		}
		return new Params(request);
	}

	private void handleMissingRoles(HttpServletRequest request, HttpServletResponse response, Set<Role> allowedRoles){
		Optional<DatarouterSession> session = DatarouterSessionManager.getFromRequest(request);
		String requestUrl = RequestTool.getRequestUrlString(request);
		if(session.map(DatarouterSession::isAnonymous).orElse(true)){
			if(samlSettings.getShouldProcess()){
				samlService.redirectToIdentityProvider(request, response);
			}else{
				sessionManager.addTargetUrlCookie(response, requestUrl);
				ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath()
						+ authenticationConfig.getSigninPath());
			}
			return;
		}
		if(session.map(sessionObj -> sessionObj.hasRole(DatarouterUserRole.REQUESTOR)).orElse(false)){
			String allowedRolesParam = allowedRoles.stream()
					.map(Role::getPersistentString)
					.collect(Collectors.joining(","));
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath()
					+ authenticationConfig.getPermissionRequestPath() + "?deniedUrl=" + UrlTool.encode(requestUrl)
					+ "&allowedRoles=" + allowedRolesParam);
			return;
		}
		try{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private static boolean isMultipart(HttpServletRequest request){
		return request.getContentType() != null
				&& request.getContentType().toLowerCase().contains("multipart/form-data");
	}

}
