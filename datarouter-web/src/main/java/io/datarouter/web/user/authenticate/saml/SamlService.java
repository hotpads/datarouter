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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
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

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrl;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrlKey;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.user.session.service.Session;
import io.datarouter.web.user.session.service.UserSessionService;

/**
 * To configure SAML:<ol>
 * <li>Bind implementations of {@link UserSessionService} and {@link RoleManager}.</li>
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
	private final RoleManager roleManager;
	private final Optional<SamlRegistrar> samlRegistrar;
	private final KeyPair signingKeyPair;
	private final BaseDatarouterSamlDao samlDao;
	private final ExceptionRecorder exceptionRecorder;
	private final AdminEmail adminEmail;
	private final SamlConfigService samlConfigService;

	@Inject
	public SamlService(
			DatarouterSamlSettings samlSettings,
			UserSessionService userSessionService,
			RoleManager roleManager,
			Optional<SamlRegistrar> jitSamlRegistrar,
			BaseDatarouterSamlDao samlDao,
			AdminEmail adminEmail,
			ExceptionRecorder exceptionRecorder,
			SamlConfigService samlConfigService){
		this.samlSettings = samlSettings;
		this.userSessionService = userSessionService;
		this.roleManager = roleManager;
		this.samlRegistrar = jitSamlRegistrar;
		this.samlDao = samlDao;
		this.exceptionRecorder = exceptionRecorder;
		this.adminEmail = adminEmail;
		this.samlConfigService = samlConfigService;

		PhaseTimer phaseTimer = new PhaseTimer();
		if(logger.isDebugEnabled()){
			logger.debug(Arrays.asList(Security.getProviders()).stream()
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
		if(samlSettings.getShouldProcess()){
			SamlTool.throwUnlessHttps(request);
			exceptionRecorder.recordHttpRequest(request);
			if(request.getMethod().equals("OPTIONS")){
				logger.warn("received OPTIONS request URL={}", request.getRequestURL());
			}
			try{
				samlRegistrar.ifPresent(SamlRegistrar::register);
			}catch(RuntimeException e){
				if(samlSettings.ignoreServiceProviderRegistrationFailures.get()){
					logger.warn("Ignoring failure to register with IdP.", e);
				}else{
					throw e;
				}
			}
			AuthnRequestMessageConfig config = new AuthnRequestMessageConfig(
					samlSettings.entityId.get(),
					SamlTool.getUrlInRequestContext(request, samlSettings.assertionConsumerServicePath.get()),
					samlSettings.idpSamlUrl.get(),
					"",
					null,
					signingKeyPair);
			MessageContext authnRequestContext = SamlTool.buildAuthnRequestAndContext(config);
			persistAuthnRequestIdRedirectUrl(authnRequestContext, request);
			SamlTool.redirectWithAuthnRequestContext(response, authnRequestContext);
		}else{
			throw new RuntimeException("SAML Configuration error");
		}
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
		String username = assertion.getSubject().getNameID().getValue();
		Set<Role> roles = determineRoles(assertion, username, samlSettings.getAttributeToRoleGroupIdMap());
		Session session = userSessionService.signInUserWithCreateIfNecessary(request, username, roles, "SAML User");
		userSessionService.setSessionCookies(response, session);
		return session;
	}

	private Set<Role> determineRoles(Assertion assertion, String username,
			Map<String,String> attributeToRoleGroupIdMap){
		Set<Role> rolesForDefaultGroup = roleManager.getRolesForDefaultGroup();
		List<Role> rolesForGroupAttributes = SamlTool.streamAttributeValuesByName(SamlTool.ROLE_GROUP_ATTRIBUTE_NAME,
				assertion)
				.map(attributeToRoleGroupIdMap::get)
				.filter(Objects::nonNull)
				.map(roleManager::getRolesForGroup)
				.flatMap(Set::stream)
				.collect(Collectors.toList());
		List<Role> rolesForRoleAttributes = SamlTool.streamAttributeValuesByName(SamlTool.ROLE_ATTRIBUTE_NAME,
				assertion)
				.map(roleManager::getRoleFromPersistentString)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Set<Role> superRolesForAdminUsers = username.equals(adminEmail.get())
				? roleManager.getRolesForSuperGroup()
				: Collections.emptySet();
		return Scanner.concat(rolesForDefaultGroup, rolesForGroupAttributes, rolesForRoleAttributes,
				superRolesForAdminUsers).collect(HashSet::new);
	}

	private void redirectAfterAuthentication(HttpServletRequest request, HttpServletResponse response,
			MessageContext responseMessageContext){
		String url = getRedirectUrl(request, responseMessageContext);
		logger.debug("Redirecting to requested URL: " + url);
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
		if(StringTool.isEmptyOrWhitespace(urlForChecking) || urlForChecking.endsWith("signin") || urlForChecking
				.endsWith("login")){
			return false;
		}
		return true;
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
