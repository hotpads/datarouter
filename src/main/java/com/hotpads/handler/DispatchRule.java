package com.hotpads.handler;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.util.http.security.ApiKeyPredicate;
import com.hotpads.util.http.security.CsrfValidator;
import com.hotpads.util.http.security.SecurityParameters;
import com.hotpads.util.http.security.SignatureValidator;

public class DispatchRule{
	
	private Logger logger = LoggerFactory.getLogger(DispatchRule.class);
	
	private Pattern pattern;
	private Class<? extends BaseHandler> handlerClass;
	private ApiKeyPredicate apiKeyPredicate;
	private CsrfValidator csrfValidator;
	private SignatureValidator signatureValidator;
	private boolean requireHttps;
	
	public DispatchRule(String regex){
		pattern = Pattern.compile(regex);
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
	
	/**** getters *****/

	public Pattern getPattern(){
		return this.pattern;
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
		boolean result = apiKeyPredicate == null || apiKeyPredicate.check(request.getParameter(SecurityParameters.API_KEY));
		if(!result)
			logger.warn("API key check failed");
		return result;
	}
	
	private boolean checkCsrfToken(HttpServletRequest request){
		boolean result = csrfValidator == null || csrfValidator.check(request.getParameter(SecurityParameters.CSRF_TOKEN));
		if(!result)
			logger.warn("CSRF token check failed");
		return result; 
	}
	
	private boolean checkSignature(HttpServletRequest request){
		String signature =  request.getParameter(SecurityParameters.SIGNATURE);
		boolean result = signatureValidator == null 
				|| signatureValidator.checkHexSignatureMulti(request.getParameterMap(), signature);
		if(!result)
			logger.warn("Signature validation failed");
		return result;
	}
	
	private boolean checkHttps(HttpServletRequest request){
		boolean result = !requireHttps || (requireHttps && request.isSecure());
		if(!result)
			logger.warn("HTTPS check failed");
		return result;
	}

	public boolean apply(HttpServletRequest request){
		return checkApiKey(request)
				&& checkCsrfToken(request)
				&& checkSignature(request)
				&& checkHttps(request);
	}

}
