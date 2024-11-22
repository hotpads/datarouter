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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.session.DatarouterSessionManager;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.ApiKeyPredicate.ApiKeyPredicateCheck;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.BaseHandlerCodec;
import io.datarouter.web.handler.encoder.DefaultEncoder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.types.DefaultDecoder;
import io.datarouter.web.handler.types.HandlerDecoder;
import io.datarouter.web.security.CsrfValidationResult;
import io.datarouter.web.security.CsrfValidator;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.security.SecurityValidator;
import io.datarouter.web.security.SignatureValidator;
import io.datarouter.web.util.http.RequestTool;

public class DispatchRule{
	private static final Logger logger = LoggerFactory.getLogger(DispatchRule.class);

	private final BaseRouteSet routeSet;
	private final String regex;
	private final Pattern pattern;
	private final List<SecurityValidator> securityValidators;
	private final Set<Role> allowedRoles;

	private Class<? extends BaseHandler> handlerClass;
	private final List<ApiKeyPredicate> apiKeyPredicates;
	private CsrfValidator csrfValidator;
	private SignatureValidator signatureValidator;
	private boolean requireHttps;
	private boolean allowAnonymous;
	private Class<? extends HandlerEncoder> defaultHandlerEncoder = DefaultEncoder.class;
	private Class<? extends HandlerDecoder> defaultHandlerDecoder;
	private String persistentString;
	private boolean transmitsPii;
	private Tag tag = Tag.APP;
	private DispatchType dispatchType = DispatchType.DEFAULT;
	private String redirectUrl; // can be full URL or partial path
	private boolean skipBackwardCompatibilityChecking;

	public DispatchRule(){
		this(null, "");
	}

	public DispatchRule(BaseRouteSet routeSet, String regex){
		this.routeSet = routeSet;
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
		this.allowedRoles = new HashSet<>();
		this.securityValidators = new ArrayList<>();
		this.apiKeyPredicates = new ArrayList<>();
	}

	/*---------------------- builder pattern methods ------------------------*/

	public DispatchRule withHandler(Class<? extends BaseHandler> handlerClass){
		this.handlerClass = handlerClass;
		return this;
	}

	public DispatchRule addSecurityValidator(SecurityValidator securityValidator){
		this.securityValidators.add(securityValidator);
		return this;
	}

	public DispatchRule withApiKey(ApiKeyPredicate apiKeyPredicate){
		this.apiKeyPredicates.add(apiKeyPredicate);
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

	public DispatchRule allowRoles(Collection<Role> roles){
		Scanner.of(roles).forEach(allowedRoles::add);
		return this;
	}

	public DispatchRule allowRoles(Role... roles){
		Scanner.of(roles).forEach(allowedRoles::add);
		return this;
	}

	public DispatchRule allowAnonymous(){
		allowAnonymous = true;
		return this;
	}

	public DispatchRule withDefaultHandlerEncoder(Class<? extends HandlerEncoder> defaultHandlerEncoder){
		this.defaultHandlerEncoder = defaultHandlerEncoder;
		return this;
	}

	public DispatchRule withDefaultHandlerDecoder(Class<? extends HandlerDecoder> defaultHandlerDecoder){
		this.defaultHandlerDecoder = defaultHandlerDecoder;
		return this;
	}

	public DispatchRule withDefaultHandlerCodec(BaseHandlerCodec handlerCodec){
		this.defaultHandlerEncoder = handlerCodec.encoderClass;
		this.defaultHandlerDecoder = handlerCodec.decoderClass;
		return this;
	}

	public DispatchRule withPersistentString(String persistentString){
		this.persistentString = persistentString;
		return this;
	}

	public DispatchRule transmitsPii(){
		transmitsPii = true;
		return this;
	}

	public DispatchRule withTag(Tag tag){
		this.tag = tag;
		return this;
	}

	public DispatchRule withDispatchType(DispatchType dispatchType){
		this.dispatchType = dispatchType;
		return this;
	}

	public DispatchRule withRedirect(String redirectUrl){
		this.redirectUrl = redirectUrl;
		return this;
	}

	public DispatchRule skipBackwardCompatibilityChecking(boolean skipBackwardCompatibilityChecking){
		this.skipBackwardCompatibilityChecking = skipBackwardCompatibilityChecking;
		return this;
	}

	/*------------------------------ getters --------------------------------*/

	public BaseRouteSet getRouteSet(){
		return routeSet;
	}

	public Pattern getPattern(){
		return pattern;
	}

	public String getRegex(){
		return regex;
	}

	public Class<? extends BaseHandler> getHandlerClass(){
		return handlerClass;
	}

	public List<ApiKeyPredicate> getApiKeyPredicates(){
		return apiKeyPredicates;
	}

	public boolean hasApiKey(){
		return !apiKeyPredicates.isEmpty();
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

	public Set<Role> getAllowedRoles(){
		return allowedRoles;
	}

	public Class<? extends HandlerEncoder> getDefaultHandlerEncoder(){
		return defaultHandlerEncoder;
	}

	public Class<? extends HandlerDecoder> getDefaultHandlerDecoder(){
		if(defaultHandlerDecoder != null){
			return defaultHandlerDecoder;
		}
		return DefaultDecoder.class;
	}

	public Optional<String> getPersistentString(){
		return Optional.ofNullable(persistentString);
	}

	public boolean doesTransmitPii(){
		return transmitsPii;
	}

	public Tag getTag(){
		return tag;
	}

	public DispatchType getDispatchType(){
		return dispatchType;
	}

	public Optional<String> getRedirectUrl(){
		return Optional.ofNullable(redirectUrl);
	}

	public boolean getSkipBackwardCompatibilityChecking(){
		return skipBackwardCompatibilityChecking;
	}

	private SecurityValidationResult checkApiKey(HttpServletRequest request, String ip){
		ApiKeyPredicateCheck result;
		if(apiKeyPredicates.isEmpty()){
			result = new ApiKeyPredicateCheck(true, "");
		}else{
			var firstPredicateRef = new AtomicReference<ApiKeyPredicateCheck>();
			result = Scanner.of(apiKeyPredicates)
					.map(predicate -> predicate.check(this, request))
					.peekFirst(firstPredicateRef::set)
					.include(ApiKeyPredicateCheck::allowed)
					.findFirst()
					.orElseGet(firstPredicateRef::get);
		}
		String message = "API key check failed, " + result.accountName();
		if(!result.allowed()){
			logFailure(message, request, ip);
		}
		return new SecurityValidationResult(request, result.allowed(), message);
	}

	private SecurityValidationResult checkCsrfToken(HttpServletRequest request){
		if(csrfValidator == null){
			return SecurityValidationResult.success(request);
		}
		CsrfValidationResult result = csrfValidator.check(request);
		return new SecurityValidationResult(
				request,
				result.success(),
				"CSRF token check failed: " + result.errorMessage());
	}

	private SecurityValidationResult checkSignature(HttpServletRequest request, String ip){
		SecurityValidationResult result = SecurityValidationResult.success(request);
		if(signatureValidator != null){
			result = signatureValidator.validate(request);
		}
		if(!result.isSuccess()){
			result.setFailureMessage(Optional.ofNullable(result)
					.map(SecurityValidationResult::getFailureMessage)
					.orElse("Signature validation failed"));
			logFailure(result.getFailureMessage(), request, ip);
		}
		return result;
	}

	private SecurityValidationResult checkHttps(HttpServletRequest request, String ip){
		String message = "HTTPS check failed";
		boolean result = !requireHttps || request.isSecure();
		if(!result){
			logFailure(message, request, ip);
		}
		return new SecurityValidationResult(request, result, message);
	}

	private void logFailure(String message, HttpServletRequest request, String ip){
		logger.warn("{}. IP={} URI={} userAgent={}",
				message,
				ip,
				request.getRequestURI(),
				RequestTool.getUserAgent(request));
	}

	public SecurityValidationResult applySecurityValidation(HttpServletRequest request, String ip){
		SecurityValidationResult result = SecurityValidationResult
				.of(req -> checkApiKey(req, ip), request)
				.combinedWith(this::checkCsrfToken)
				.combinedWith(req -> checkSignature(req, ip))
				.combinedWith(req -> checkHttps(req, ip));
		for(SecurityValidator securityValidator : securityValidators){
			result = result.combinedWith(securityValidator::check);
		}
		return result;
	}

	public boolean checkRoles(HttpServletRequest request){
		if(getAllowAnonymous()){
			return true;
		}
		return DatarouterSessionManager.getFromRequest(request)
				.map(DatarouterSession::getRoles)
				.map(roles -> roles.stream().anyMatch(getAllowedRoles()::contains))
				.orElse(false);
	}

	/*------------------------------ object ---------------------------------*/

	@Override
	public String toString(){
		return regex + ":" + pattern.toString() + ":" + handlerClass.getCanonicalName();
	}

}
