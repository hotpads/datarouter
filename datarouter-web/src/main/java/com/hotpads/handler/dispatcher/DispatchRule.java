package com.hotpads.handler.dispatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.security.ApiKeyPredicate;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

public class DispatchRule{
	private static final Logger logger = LoggerFactory.getLogger(DispatchRule.class);

	private final String regex;
	private final Pattern pattern;
	private Class<? extends BaseHandler> handlerClass;
	private ApiKeyPredicate apiKeyPredicate;
	private CsrfValidator csrfValidator;
	private SignatureValidator signatureValidator;
	private boolean requireHttps;
	private Set<DatarouterUserRole> allowedRoles;
	private boolean allowAnonymous;

	public DispatchRule(String regex){
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
		this.allowedRoles = new HashSet<>();
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

	public DispatchRule withSignature(SignatureValidator signatureValidator){
		this.signatureValidator = signatureValidator;
		return this;
	}

	public DispatchRule requireHttps(){
		requireHttps = true;
		return this;
	}

	public DispatchRule allowRoles(DatarouterUserRole... roles){
		allowedRoles.addAll(Arrays.asList(roles));
		return this;
	}

	public DispatchRule allowAnonymous(){
		allowAnonymous = true;
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

	public boolean getAllowAnonymous(){
		return allowAnonymous;
	}

	public Set<DatarouterUserRole> getAllowedRoles(){
		return allowedRoles;
	}

	private boolean checkApiKey(HttpServletRequest request){
		String apiKeyCandidate = request.getParameter(SecurityParameters.API_KEY);
		boolean result = apiKeyPredicate == null || apiKeyCandidate != null && apiKeyPredicate.check(apiKeyCandidate);
		if(!result){
			logFailure("API key check failed", request);
		}
		return result;
	}

	private boolean checkCsrfToken(HttpServletRequest request){
		boolean result = csrfValidator == null || csrfValidator.check(request);
		if(!result){
			Long requestTimeMs = csrfValidator.getRequestTimeMs(request);
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
		boolean result = signatureValidator == null
				|| signatureValidator.checkHexSignatureMulti(request);
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
		return checkApiKey(request)
				&& checkCsrfToken(request)
				&& checkSignature(request)
				&& checkHttps(request);
	}

	/*-------------------- Object -------------------------*/

	@Override
	public String toString(){
		return regex + ":" + pattern.toString() + ":" + handlerClass.getCanonicalName();
	}

}
