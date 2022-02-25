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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.encoder.DefaultEncoder;
import io.datarouter.web.handler.encoder.HandlerEncoder;
import io.datarouter.web.handler.types.DefaultDecoder;
import io.datarouter.web.handler.types.HandlerDecoder;
import io.datarouter.web.security.CsrfValidator;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.security.SecurityValidator;
import io.datarouter.web.security.SignatureValidator;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionManager;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleEnum;
import io.datarouter.web.util.http.RequestTool;

public class DispatchRule{
	private static final Logger logger = LoggerFactory.getLogger(DispatchRule.class);

	private final BaseRouteSet routeSet;
	private final String regex;
	private final Pattern pattern;
	private final List<SecurityValidator> securityValidators;

	private Class<? extends BaseHandler> handlerClass;
	private ApiKeyPredicate apiKeyPredicate;
	private CsrfValidator csrfValidator;
	private SignatureValidator signatureValidator;
	private boolean requireHttps;
	private Set<Role> allowedRoles;
	private boolean allowAnonymous;
	private Class<? extends HandlerEncoder> defaultHandlerEncoder = DefaultEncoder.class;
	private Class<? extends HandlerDecoder> defaultHandlerDecoder = DefaultDecoder.class;
	private String persistentString;
	private boolean transmitsPii;
	private Tag tag = Tag.APP;

	public DispatchRule(){
		this(null, "");
	}

	public DispatchRule(BaseRouteSet routeSet, String regex){
		this.routeSet = routeSet;
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
		this.allowedRoles = new HashSet<>();
		this.securityValidators = new ArrayList<>();
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

	public DispatchRule allowRoles(RoleEnum<?>... roles){
		return allowRoles(Arrays.asList(roles));
	}

	public DispatchRule allowRoles(Collection<RoleEnum<?>> roles){
		Scanner.of(roles)
				.map(RoleEnum::getRole)
				.forEach(allowedRoles::add);
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

	public ApiKeyPredicate getApiKeyPredicate(){
		return apiKeyPredicate;
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

	public Set<Role> getAllowedRoles(){
		return allowedRoles;
	}

	public Class<? extends HandlerEncoder> getDefaultHandlerEncoder(){
		return defaultHandlerEncoder;
	}

	public Class<? extends HandlerDecoder> getDefaultHandlerDecoder(){
		return defaultHandlerDecoder;
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

	private SecurityValidationResult checkApiKey(HttpServletRequest request){
		Pair<Boolean,String> result;
		if(apiKeyPredicate == null){
			result = new Pair<>(true, "");
		}else{
			result = apiKeyPredicate.check(this, request);
		}
		String message = "API key check failed, " + result.getRight();
		if(!result.getLeft()){
			logFailure(message, request);
		}
		return new SecurityValidationResult(request, result.getLeft(), message);
	}

	private SecurityValidationResult checkCsrfToken(HttpServletRequest request){
		boolean result = csrfValidator == null || csrfValidator.check(request);
		if(!result){
			try{
				Long requestTimeMs = csrfValidator.getRequestTimeMs(request);
				Long differenceMs = null;
				if(requestTimeMs != null){
					differenceMs = System.currentTimeMillis() - requestTimeMs;
				}

				logFailure("CSRF token check failed, request time:" + requestTimeMs + " is " + differenceMs
						+ "ms > current time", request);
			}catch(Exception e){
				logFailure("CSRF token time could not be extracted", request);
			}
		}
		return new SecurityValidationResult(request, result, "CSRF token check failed");
	}

	private SecurityValidationResult checkSignature(HttpServletRequest request){
		SecurityValidationResult result = SecurityValidationResult.success(request);
		if(signatureValidator != null){
			result = signatureValidator.validate(request);
		}
		if(!result.isSuccess()){
			result.setFailureMessage(Optional.ofNullable(result)
					.map(SecurityValidationResult::getFailureMessage)
					.orElse("Signature validation failed"));
			logFailure(result.getFailureMessage(), request);
		}
		return result;
	}

	private SecurityValidationResult checkHttps(HttpServletRequest request){
		String message = "HTTPS check failed";
		boolean result = !requireHttps || requireHttps && request.isSecure();
		if(!result){
			logFailure(message, request);
		}
		return new SecurityValidationResult(request, result, message);
	}

	private void logFailure(String message, HttpServletRequest request){
		logger.warn(message + ". IP={} URI={} userAgent={}", RequestTool.getIpAddress(request),
				request.getRequestURI(), RequestTool.getUserAgent(request));
	}

	public SecurityValidationResult applySecurityValidation(HttpServletRequest request){
		SecurityValidationResult result = SecurityValidationResult.of(this::checkApiKey, request)
				.combinedWith(this::checkCsrfToken)
				.combinedWith(this::checkSignature)
				.combinedWith(this::checkHttps);
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
