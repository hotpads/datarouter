/**
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
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.collection.SetTool;
import io.datarouter.util.string.StringTool;
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
	private final DatarouterSamlRouter samlRouter;

	@Inject
	public SamlService(DatarouterSamlSettings samlSettings, UserSessionService userSessionService,
			RoleManager roleManager, Optional<SamlRegistrar> jitSamlRegistrar, DatarouterSamlRouter samlRouter){
		this.samlSettings = samlSettings;
		this.userSessionService = userSessionService;
		this.roleManager = roleManager;
		this.samlRegistrar = jitSamlRegistrar;
		this.samlRouter = samlRouter;

		if(logger.isDebugEnabled()){
			logger.debug(Arrays.asList(Security.getProviders()).stream()
					.map(Provider::getInfo)
					.collect(Collectors.joining(", ", "Security providers: ", "")));
		}

		// this ensures that all of OpenSAML's required crypto operations are available
		try{
			new JavaCryptoValidationInitializer().init();// checks JCE
			InitializationService.initialize();// initializes OpenSAML
		}catch(InitializationException e){
			throw new RuntimeException("Initialization failed", e);
		}

		this.signingKeyPair = RandomSamlKeyPair.getKeyPair();
	}

	public Mav mavSignout(HttpServletResponse response){
		userSessionService.clearSessionCookies(response);
		return new GlobalRedirectMav(samlSettings.idpHomeUrl.get());
	}

	public void redirectToIdentityProvider(HttpServletRequest request, HttpServletResponse response){
		if(samlSettings.getShouldProcess()){
			try{
				samlRegistrar.ifPresent(registrar -> registrar.register());
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
			MessageContext<SAMLObject> authnRequestContext = SamlTool.buildAuthnRequestAndContext(config);
			persistAuthnRequestIdRedirectUrl(authnRequestContext, request);
			SamlTool.redirectWithAuthnRequestContext(response, authnRequestContext);
		}else{
			throw new RuntimeException("SAML Configuration error");
		}
	}

	private void persistAuthnRequestIdRedirectUrl(MessageContext<SAMLObject> authnRequest, HttpServletRequest request){
		String requestedUrl = request.getRequestURL().toString();
		if(!isUrlOkForRedirect(requestedUrl)){
			//skip persisting bad URLs
			logger.info("URL is not OK for redirect: {}", requestedUrl);
			return;
		}
		String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";

		String authnRequestId = ((AuthnRequest)authnRequest.getMessage()).getID();
		SamlAuthnRequestRedirectUrl redirect = new SamlAuthnRequestRedirectUrl(authnRequestId,
				requestedUrl + queryString);
		samlRouter.samlAuthnRequestRedirectUrl.put(redirect, null);
	}

	public void consumeAssertion(HttpServletRequest request, HttpServletResponse response){
		if(!samlSettings.getShouldProcess() || request.getParameter(SAML_RESPONSE) == null){
			send403(response);
			return;
		}

		MessageContext<SAMLObject> responseMessageContext = SamlTool.getAndValidateResponseMessageContext(request,
				samlSettings.getSignatureCredential());
		Response message = (Response)responseMessageContext.getMessage();

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
		Set<Role> roles = determineRoles(assertion, samlSettings.getAttributeToRoleGroupIdMap());
		Session session = userSessionService.signInUserWithCreateIfNecessary(request, username, roles, "SAML User");
		userSessionService.setSessionCookies(response, session);
		return session;
	}

	private Set<Role> determineRoles(Assertion assertion, Map<String,String> attributeToRoleGroupIdMap){
		if(!samlSettings.shouldAllowRoleGroups.get()){
			// if groups are turned off, only return roles that every user should have
			return roleManager.getRolesForDefaultGroup();
		}
		return SetTool.union(roleManager.getRolesForDefaultGroup(), SamlTool.streamGroupNameValues(assertion)
				.map(attributeToRoleGroupIdMap::get)
				.filter(Objects::nonNull)
				.map(roleManager::getRolesForGroup)
				.flatMap(Set::stream)
				.collect(Collectors.toSet()));
	}

	private void redirectAfterAuthentication(HttpServletRequest request, HttpServletResponse response,
			MessageContext<SAMLObject> responseMessageContext){
		String url = getRedirectUrl(request, responseMessageContext);
		logger.debug("Redirecting to requested URL: " + url);
		try{
			response.sendRedirect(url);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private String getRedirectUrl(HttpServletRequest request, MessageContext<SAMLObject> responseContext){
		String authnRequestId = ((Response)responseContext.getMessage()).getInResponseTo();
		SamlAuthnRequestRedirectUrl redirect = samlRouter.samlAuthnRequestRedirectUrl.get(
				new SamlAuthnRequestRedirectUrlKey(authnRequestId), null);
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

	private static String getRedirectUrlFromResponseContext(MessageContext<SAMLObject> responseContext,
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
