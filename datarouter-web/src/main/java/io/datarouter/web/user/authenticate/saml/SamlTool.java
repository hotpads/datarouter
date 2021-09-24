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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Scoping;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import io.datarouter.util.Require;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;

public class SamlTool{
	private static final Logger logger = LoggerFactory.getLogger(SamlTool.class);

	private static final RandomIdentifierGenerationStrategy secureRandomIdGenerator =
			new RandomIdentifierGenerationStrategy();

	public static final String ROLE_GROUP_ATTRIBUTE_NAME = "groupAttributes";
	public static final String ROLE_ATTRIBUTE_NAME = "roleAttributes";

	public static final String DEFAULT_ENTITY_ID = "https://datarouter.io";

	// AuthnRequest

	public static MessageContext buildAuthnRequestAndContext(AuthnRequestMessageConfig config){
		AuthnRequest authnRequest = build(AuthnRequest.DEFAULT_ELEMENT_NAME);
		authnRequest.setIssueInstant(Instant.now());
		authnRequest.setDestination(config.identityProviderSingleSignOnServiceUrl);
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		authnRequest.setAssertionConsumerServiceURL(config.serviceProviderAssertionConsumerServiceUrl);
		authnRequest.setID(generateSecureRandomId());
		authnRequest.setIssuer(buildIssuer(config.serviceProviderEntityId));
		authnRequest.setNameIDPolicy(buildNameIdPolicy());
		authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext());
		config.proxyCount.ifPresent(proxyCount -> authnRequest.setScoping(buildScoping(proxyCount)));
		logSamlObject("SamlTool.buildAuthnRequestAndContext", authnRequest);

		MessageContext authnRequestContext = new MessageContext();
		authnRequestContext.setMessage(authnRequest);
		authnRequestContext.getSubcontext(SAMLBindingContext.class, true).setRelayState(config.relayState);
		authnRequestContext.getSubcontext(SAMLPeerEntityContext.class, true).getSubcontext(SAMLEndpointContext.class,
				true).setEndpoint(buildIdpEndpoint(config.identityProviderSingleSignOnServiceUrl));

		//since we use redirect, the AuthnRequest itself can't be signed, but this specifies signing of the query params
		config.signingKeyPair.ifPresent(keyPair -> {
			SignatureSigningParameters params = new SignatureSigningParameters();
			params.setSigningCredential(new BasicCredential(keyPair.getPublic(), keyPair.getPrivate()));
			params.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
			authnRequestContext.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(
					params);
		});
		return authnRequestContext;
	}

	//limit number of times the next IdP can proxy an AuthnRequest
	private static Scoping buildScoping(Integer proxyCount){
		Scoping scoping = build(Scoping.DEFAULT_ELEMENT_NAME);
		scoping.setProxyCount(proxyCount);
		return scoping;
	}

	public static void redirectWithAuthnRequestContext(HttpServletResponse httpServletResponse,
			MessageContext authnRequestContext){
		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		encoder.setHttpServletResponse(httpServletResponse);
		encoder.setMessageContext(authnRequestContext);
		try{
			encoder.initialize();
			encoder.encode();
		}catch(ComponentInitializationException | MessageEncodingException e){
			throw new RuntimeException(e);
		}
	}

	private static NameIDPolicy buildNameIdPolicy(){
		NameIDPolicy nameIdPolicy = build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
		nameIdPolicy.setAllowCreate(false);
		nameIdPolicy.setFormat(NameIDType.EMAIL);
		return nameIdPolicy;
	}

	private static RequestedAuthnContext buildRequestedAuthnContext(){
		AuthnContextClassRef passwordAuthnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
		passwordAuthnContextClassRef.setURI(AuthnContext.PASSWORD_AUTHN_CTX);

		RequestedAuthnContext requestedAuthnContext = build(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
		requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
		requestedAuthnContext.getAuthnContextClassRefs().add(passwordAuthnContextClassRef);
		return requestedAuthnContext;
	}

	private static Endpoint buildIdpEndpoint(String identityProviderSingleSignOnServiceUrl){
		SingleSignOnService endpoint = build(SingleSignOnService.DEFAULT_ELEMENT_NAME);
		endpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		endpoint.setLocation(identityProviderSingleSignOnServiceUrl);
		return endpoint;
	}

	// response

	public static MessageContext getAndValidateResponseMessageContext(HttpServletRequest request,
			Credential signatureCredential){
		MessageContext responseMessageContext = decodeResponse(request);
		logSamlObject("SamlTool.getAndValidateResponseMessageContext", (SAMLObject)responseMessageContext.getMessage());
		validateMessageContext(responseMessageContext, request);
		Response response = (Response)responseMessageContext.getMessage();
		verifySignature(response, signatureCredential);
		for(Assertion assertion : response.getAssertions()){
			verifySignature(assertion, signatureCredential);
		}
		return responseMessageContext;
	}

	private static void validateMessageContext(MessageContext context, HttpServletRequest request){
		// handler to check timing
		MessageLifetimeSecurityHandler lifetimeSecurityHandler = new MessageLifetimeSecurityHandler();
		lifetimeSecurityHandler.setClockSkew(Duration.ofMillis(1000));
		lifetimeSecurityHandler.setMessageLifetime(Duration.ofMinutes(1));
		lifetimeSecurityHandler.setRequiredRule(true);

		// handler to check that this is the right destination
		ReceivedEndpointSecurityHandler receivedEndpointSecurityHandler = new ReceivedEndpointSecurityHandler();
		receivedEndpointSecurityHandler.setHttpServletRequest(request);

		// run handlers
		List<MessageHandler> handlers = new ArrayList<>();
		handlers.add(lifetimeSecurityHandler);
		handlers.add(receivedEndpointSecurityHandler);
		BasicMessageHandlerChain handlerChain = new BasicMessageHandlerChain();
		handlerChain.setHandlers(handlers);
		try{
			handlerChain.initialize();
			handlerChain.doInvoke(context);
		}catch(ComponentInitializationException | MessageHandlerException e){
			throw new RuntimeException(e);
		}
	}

	//signatures

	public static KeyPair generateKeyPair(){
		try{
			return KeySupport.generateKeyPair("RSA", 4096, null);
		}catch(NoSuchAlgorithmException | NoSuchProviderException e){
			throw new RuntimeException("Failed to generate signingKeyPair", e);
		}
	}

	public static void signSamlObject(SignableSAMLObject samlObject, KeyPair signatureKeyPair){
		BasicCredential credential = CredentialSupport.getSimpleCredential(signatureKeyPair.getPublic(),
				signatureKeyPair.getPrivate());
		Signature signature = SamlTool.build(Signature.DEFAULT_ELEMENT_NAME);
		signature.setSigningCredential(credential);
		signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		samlObject.setSignature(signature);
		try{
			XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(samlObject).marshall(samlObject);
			Signer.signObject(signature);
		}catch(MarshallingException | SignatureException e){
			throw new RuntimeException(e);
		}
	}

	private static void verifySignature(SignableSAMLObject signedObject, Credential signatureCredential){
		if(!signedObject.isSigned()){
			throw new RuntimeException("The SAML object was not signed.");
		}
		Signature signature = signedObject.getSignature();
		try{
			SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
			profileValidator.validate(signature);
			SignatureValidator.validate(signature, signatureCredential);
		}catch(SignatureException e){
			throw new RuntimeException(e);
		}
	}

	public static Credential getCredentialFromEncodedRsaPublicKey(String encodedPublicKey){
		try{
			byte[] keyBytes = Base64.getDecoder().decode(encodedPublicKey);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			PublicKey key = KeyFactory.getInstance("RSA").generatePublic(keySpec);
			return new BasicCredential(key);
		}catch(InvalidKeySpecException | NoSuchAlgorithmException e){
			logger.error("SAML KeyFactory failure", e);
			throw new RuntimeException(e);
		}
	}

	public static Credential getCredentialFromEncodedX509Certificate(String encodedX509Certificate){
		try{
			byte[] certBytes = Base64.getDecoder().decode(encodedX509Certificate);
			ByteArrayInputStream certByteStream = new ByteArrayInputStream(certBytes);
			X509Certificate cert = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(
					certByteStream);
			return new BasicX509Credential(cert);
		}catch(CertificateException e){
			logger.error("SAML CertificateFactory failure", e);
			throw new RuntimeException(e);
		}
	}

	//encode/decode

	private static MessageContext decodeResponse(HttpServletRequest request){
		HTTPPostDecoder samlMessageDecoder = new HTTPPostDecoder();
		samlMessageDecoder.setHttpServletRequest(request);
		try{
			samlMessageDecoder.initialize();
			samlMessageDecoder.decode();
		}catch(ComponentInitializationException | MessageDecodingException e){
			throw new RuntimeException(e);
		}
		return samlMessageDecoder.getMessageContext();
	}

	//general purpose/helpers

	public static Issuer buildIssuer(String serviceProviderEntityId){
		Issuer issuer = build(Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(serviceProviderEntityId);
		return issuer;
	}

	public static Stream<String> streamAttributeValuesByName(String attributeName, Assertion assertion){
		return assertion.getAttributeStatements()
				.stream()
				.map(AttributeStatement::getAttributes)
				.flatMap(List::stream)
				.filter(attribute -> attributeName.equals(attribute.getName()))
				.map(Attribute::getAttributeValues)
				.flatMap(List::stream)
				.filter(xmlObject -> xmlObject instanceof XSString)
				.map(xmlObject -> ((XSString)xmlObject).getValue());
	}

	public static String getUrlInRequestContext(HttpServletRequest request, String path){
		try{
			return new URL(new URL(request.getRequestURL().toString()), request.getContextPath() + path).toString();
		}catch(MalformedURLException e){
			throw new RuntimeException("Failed to build URL. context: " + request.getRequestURL() + " path " + path);
		}
	}

	public static void logSamlObject(String callsite, SAMLObject object){
		if(object == null){
			logger.debug(callsite + " - SAMLObject is null");
			return;
		}

		Element element = object.getDOM();
		if(element == null){
			try{
				Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(object);
				out.marshall(object);
				element = object.getDOM();
			}catch(MarshallingException e){
				logger.error(callsite + " - Failed to marshall SAMLObject", e);
				return;
			}
		}
		try{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(element);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			logger.debug(callsite + " - " + xmlString);
		}catch(TransformerException e){
			logger.error(callsite + " - Failed to log SAML object.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T build(QName qName){
		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
		return (T)builderFactory.getBuilder(qName).buildObject(qName);
	}

	public static String generateSecureRandomId(){
		return secureRandomIdGenerator.generateIdentifier();
	}

	public static void throwUnlessHttps(HttpServletRequest request){
		Require.equals("https", request.getScheme().toLowerCase(), "https is required for SAML authentication.");
	}

}
