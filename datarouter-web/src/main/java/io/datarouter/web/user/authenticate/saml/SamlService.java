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
package io.datarouter.web.user.authenticate.saml;

import java.io.IOException;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.config.impl.JavaCryptoValidationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.authenticate.saml.AuthnRequestMessageConfig;
import io.datarouter.auth.authenticate.saml.RandomSamlKeyPair;
import io.datarouter.auth.authenticate.saml.SamlTool;
import io.datarouter.auth.model.dto.InterpretedSamlAssertion;
import io.datarouter.auth.session.Session;
import io.datarouter.auth.session.UserSessionService;
import io.datarouter.auth.storage.user.saml.BaseDatarouterSamlDao;
import io.datarouter.auth.storage.user.saml.SamlAuthnRequestRedirectUrl;
import io.datarouter.auth.storage.user.saml.SamlAuthnRequestRedirectUrlKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * To configure SAML:<ol>
 * <li>Bind implementations of {@link UserSessionService}.</li>
 * <li>Serve {@link SamlAssertionConsumerServlet} (BEFORE /*). <li>
 * Add {@link DatarouterSamlSettings} to your settings and set it up appropriately.</li>
 * </ol>
 */
@Singleton
public class SamlService{
	private static final Logger logger = LoggerFactory.getLogger(SamlService.class);

	private static final String SAML_RESPONSE = "SAMLResponse";

	private final DatarouterSamlSettings samlSettings;
	private final UserSessionService userSessionService;
	private final KeyPair signingKeyPair;
	private final BaseDatarouterSamlDao samlDao;
	private final ExceptionRecorder exceptionRecorder;
	private final SamlConfigService samlConfigService;

	@Inject
	public SamlService(
			DatarouterSamlSettings samlSettings,
			UserSessionService userSessionService,
			BaseDatarouterSamlDao samlDao,
			ExceptionRecorder exceptionRecorder,
			SamlConfigService samlConfigService){
		this.samlSettings = samlSettings;
		this.userSessionService = userSessionService;
		this.samlDao = samlDao;
		this.exceptionRecorder = exceptionRecorder;
		this.samlConfigService = samlConfigService;

		PhaseTimer phaseTimer = new PhaseTimer();
		if(logger.isDebugEnabled()){
			logger.debug(Arrays.stream(Security.getProviders())
					.map(Provider::getInfo)
					.collect(Collectors.joining(", ", "Security providers: ", "")));
			phaseTimer.add("log");
		}

		// this ensures that all of OpenSAML's required crypto operations are available
		try{
			new JavaCryptoValidationInitializer().init();// checks JCE
			phaseTimer.add("JavaCryptoValidationInitializer");
			InitializationService.initialize();// initializes OpenSAML
			phaseTimer.add("InitializationService");
		}catch(InitializationException e){
			throw new RuntimeException("Initialization failed", e);
		}

		this.signingKeyPair = RandomSamlKeyPair.getKeyPair();
		phaseTimer.add("RandomSamlKeyPair");
		logger.warn("{}", phaseTimer);
	}

	public Mav mavSignout(HttpServletResponse response){
		userSessionService.clearSessionCookies(response);
		return new GlobalRedirectMav(samlSettings.idpHomeUrl.get());
	}

	public void redirectToIdentityProvider(HttpServletRequest request, HttpServletResponse response){
		if(!samlSettings.getShouldProcess()){
			throw new RuntimeException("SAML Configuration error");
		}

		SamlTool.throwUnlessHttps(request);
		exceptionRecorder.recordHttpRequest(request);
		if(request.getMethod().equals("OPTIONS")){
			logger.warn("received OPTIONS request URL={}", request.getRequestURL());
		}
		AuthnRequestMessageConfig config = new AuthnRequestMessageConfig(
				samlSettings.entityId.get(),
				SamlTool.getUrlInRequestContext(request, samlSettings.assertionConsumerServicePath.get()),
				samlSettings.idpSamlUrl.get(),
				"",
				Optional.empty(),
				Optional.of(signingKeyPair));
		MessageContext authnRequestContext = SamlTool.buildAuthnRequestAndContext(config);
		persistAuthnRequestIdRedirectUrl(authnRequestContext, request);
		SamlTool.redirectWithAuthnRequestContext(response, authnRequestContext);
	}

	private void persistAuthnRequestIdRedirectUrl(MessageContext authnRequest, HttpServletRequest request){
		String requestUrl = samlConfigService.getRequestUrl(request);
		if(!isUrlOkForRedirect(requestUrl)){
			//skip persisting bad URLs
			logger.info("URL is not OK for redirect: {}", requestUrl);
			return;
		}

		String authnRequestId = ((AuthnRequest)authnRequest.getMessage()).getID();
		SamlAuthnRequestRedirectUrl redirect = new SamlAuthnRequestRedirectUrl(authnRequestId, requestUrl);
		samlDao.put(redirect);
	}

	public void consumeAssertion(HttpServletRequest request, HttpServletResponse response){
		if(!samlSettings.getShouldProcess() || request.getParameter(SAML_RESPONSE) == null){
			send403(response);
			return;
		}
		SamlTool.throwUnlessHttps(request);

		MessageContext responseMessageContext = SamlTool.getAndValidateResponseMessageContext(request,
				samlSettings.getSignatureCredential());
		Response message = (Response)responseMessageContext.getMessage();
		SamlTool.logSamlObject("SamlService.consumeAssertion", message);

		for(Assertion assertion : message.getAssertions()){
			Session session = createAndSetSession(request, response, assertion);
			if(session != null){
				redirectAfterAuthentication(request, response, responseMessageContext);
				return;
			}
		}
		send403(response);
	}

	private void send403(HttpServletResponse response){
		try{
			response.sendError(403);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private Session createAndSetSession(HttpServletRequest request, HttpServletResponse response, Assertion assertion){
		InterpretedSamlAssertion interpretedSamlAssertion = interpretSamlAssertion(assertion);
		logger.warn("login in user from okta username={}", interpretedSamlAssertion.username());
		Session session = userSessionService.signInUserFromSamlResponse(request, interpretedSamlAssertion);
		userSessionService.setSessionCookies(response, session);
		return session;
	}

	private InterpretedSamlAssertion interpretSamlAssertion(Assertion assertion){
		Set<String> roleGroupAttributes =
				SamlTool.streamAttributeValuesByName(SamlTool.ROLE_GROUP_ATTRIBUTE_NAME, assertion)
						.collect(Collectors.toSet());
		Set<String> mappedRoleGroups = Scanner.of(roleGroupAttributes)
				.map(samlSettings.getAttributeToRoleGroupIdMap()::get)
				.collect(HashSet::new);
		Set<String> combinedRoleGroups = Scanner.concat(roleGroupAttributes, mappedRoleGroups)
				.exclude(Objects::isNull)
				.collect(HashSet::new);

		Set<String> roleAttributes = SamlTool.streamAttributeValuesByName(SamlTool.ROLE_ATTRIBUTE_NAME, assertion)
				.collect(Collectors.toSet());
		return new InterpretedSamlAssertion(
				assertion.getSubject().getNameID().getValue(),
				combinedRoleGroups,
				roleAttributes);
	}

	private void redirectAfterAuthentication(HttpServletRequest request, HttpServletResponse response,
			MessageContext responseMessageContext){
		String url = getRedirectUrl(request, responseMessageContext);
		logger.debug("Redirecting to requested URL: {}", url);
		try{
			response.sendRedirect(url);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private String getRedirectUrl(HttpServletRequest request, MessageContext responseContext){
		String authnRequestId = ((Response)responseContext.getMessage()).getInResponseTo();
		SamlAuthnRequestRedirectUrl redirect = samlDao.get(
				new SamlAuthnRequestRedirectUrlKey(authnRequestId));
		if(redirect != null){
			//prefer the URL most recently persisted for this authnRequest (no character limit)
			return redirect.getRedirectUrl();
		}
		return getRedirectUrlFromResponseContext(responseContext, request);
	}

	private static boolean isUrlOkForRedirect(String url){
		//get rid of query params if any
		String urlForChecking = StringTool.nullSafe(url).toLowerCase().split("\\?")[0];
		//try to avoid infinite login loops
		return !StringTool.isEmptyOrWhitespace(urlForChecking)
				&& !urlForChecking.endsWith("signin")
				&& !urlForChecking.endsWith("login");
	}

	private static String getRedirectUrlFromResponseContext(MessageContext responseContext,
			HttpServletRequest request){
		SAMLBindingContext responseBindingContext = responseContext.getSubcontext(SAMLBindingContext.class, true);
		String url = responseBindingContext.getRelayState();
		if(isUrlOkForRedirect(url)){
			return url;
		}
		logger.info("URL is not OK for redirect: {}", url);
		return SamlTool.getUrlInRequestContext(request, "/");
	}

}
