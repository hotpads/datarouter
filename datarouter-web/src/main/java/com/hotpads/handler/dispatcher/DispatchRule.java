package com.hotpads.handler.dispatcher;

import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.user.CurrentDatarouterUserPredicate;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.security.ApiKeyPredicate;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.DefaultSignatureValidator;

public class DispatchRule{
	private static final Logger logger = LoggerFactory.getLogger(DispatchRule.class);

	private final String regex;
	private final Pattern pattern;
	private Class<? extends BaseHandler> handlerClass;
	private ApiKeyPredicate apiKeyPredicate;
	private CsrfValidator csrfValidator;
	private Long csrfTokenTimeout;
	private DefaultSignatureValidator signatureValidator;
	private boolean requireHttps;
	private boolean userAuthentication;
	private DatarouterUser user = null;
	private CurrentDatarouterUserPredicate userPredicate;

	@Inject
	public DispatchRule(String regex){
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}

	/**** builder pattern methods *******/

	public DispatchRule withHandler(Class <? extends BaseHandler> handlerClass){
		this.handlerClass = handlerClass;
		return this;
	}

	public DispatchRule withApiKey(ApiKeyPredicate apiKeyPredicate){
		this.apiKeyPredicate = apiKeyPredicate;
		return this;
	}

	public DispatchRule withCsrfToken(CsrfValidator csrfValidator){
		this.csrfValidator = csrfValidator;
		return this;
	}

	public DispatchRule withCsrfTokenTimeout(Long csrfTokenTimeout){
		this.csrfTokenTimeout = csrfTokenTimeout;
		return this;
	}

	public DispatchRule withSignature(DefaultSignatureValidator signatureValidator){
		this.signatureValidator = signatureValidator;
		return this;
	}

	public DispatchRule requireHttps(){
		requireHttps = true;
		return this;
	}

	public DispatchRule withUserAuthentication(CurrentDatarouterUserPredicate userPredicate){
		userAuthentication = true;
		this.userPredicate = userPredicate;
		return this;
	}

	/**** getters *****/

	public Pattern getPattern(){
		return pattern;
	}

	public Class<? extends BaseHandler> getHandlerClass(){
		return handlerClass;
	}

	public boolean hasApiKey(){
		return apiKeyPredicate != null;
	}

	public boolean hasCsrfToken(){
		return csrfValidator != null;
	}

	public boolean hasSignature(){
		return signatureValidator != null;
	}

	public boolean hasHttps(){
		return requireHttps;
	}

	private boolean checkApiKey(HttpServletRequest request){
		boolean result = apiKeyPredicate == null || apiKeyPredicate.check(request.getParameter(
				SecurityParameters.API_KEY));
		if(!result){
			logFailure("API key check failed", request);
		}
		return result;
	}

	private boolean checkCsrfToken(HttpServletRequest request){
		String csrfToken = request.getParameter(SecurityParameters.CSRF_TOKEN);
		String csrfIv = request.getParameter(SecurityParameters.CSRF_IV);
		String apiKey = request.getParameter(SecurityParameters.API_KEY);
		boolean result = csrfValidator == null || csrfValidator.check(csrfToken, csrfIv, apiKey);
		if(!result){
			Long requestTimeMs = csrfValidator.getRequestTimeMs(csrfToken, csrfIv);
			Long differenceMs = null;
			if(requestTimeMs != null){
				differenceMs = System.currentTimeMillis() - requestTimeMs;
			}

			logFailure("CSRF token check failed, request time:" + requestTimeMs + " is " + differenceMs
					+ "ms > current time", request);
		}
		return result;
	}

	private boolean checkSignature(HttpServletRequest request){
		String signature = request.getParameter(SecurityParameters.SIGNATURE);
		if(userAuthentication){
			signatureValidator = new DefaultSignatureValidator(user.getSecretKey());
		}
		boolean result = signatureValidator == null
				|| signatureValidator.checkHexSignatureMulti(request.getParameterMap(), signature);
		if(!result){
			logFailure("Signature validation failed", request);
		}
		return result;
	}

	private boolean checkHttps(HttpServletRequest request){
		boolean result = !requireHttps || requireHttps && request.isSecure();
		if(!result){
			logFailure("HTTPS check failed", request);
		}
		return result;
	}

	private void logFailure(String message, HttpServletRequest request){
		logger.warn(message + ". IP:[{}] URI:[{}]", RequestTool.getIpAddress(request), request.getRequestURI());
	}

	public boolean apply(HttpServletRequest request){
		if(userAuthentication){
			user = getUserFromApiKey(request);
		}
		return checkApiKey(request)
				&& checkCsrfToken(request)
				&& checkSignature(request)
				&& checkHttps(request);
	}

	private DatarouterUser getUserFromApiKey(HttpServletRequest request){
		return userPredicate.get(request.getParameter(SecurityParameters.API_KEY));
	}

	/*-------------------- Object -------------------------*/

	@Override
	public String toString(){
		return regex + ":" + pattern.toString() + ":" + handlerClass.getCanonicalName();
	}

}
