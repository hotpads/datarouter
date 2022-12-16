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
package io.datarouter.web.port;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.util.MxBeans;

@Singleton
public class CompoundPortIdentifier{
	private static final Logger logger = LoggerFactory.getLogger(CompoundPortIdentifier.class);

	public static final String
			CATALINA_JMX_DOMAIN = "Catalina",
			JETTY_SERVER_JMX_DOMAIN = "org.eclipse.jetty.server",
			JBOSS_JMX_DOMAIN = "jboss.as";

	private static final List<Identifier> IDENTIFIERS = List.of(
			new Identifier("Tomcat", CATALINA_JMX_DOMAIN, TomcatPortIdentifier.class),
			new Identifier("Jetty", JETTY_SERVER_JMX_DOMAIN, JettyPortIdentifier.class),
			new Identifier("Wildfly (JBoss)", JBOSS_JMX_DOMAIN, WildFlyPortIdentifier.class));

	private PortIdentificationResult portIdentificationResult;

	@Inject
	public CompoundPortIdentifier(DatarouterInjector injector){
		List<String> jmxDomains = Arrays.asList(MxBeans.SERVER.getDomains());
		Optional<Identifier> optIdentifier = IDENTIFIERS.stream()
				.filter(identifier -> jmxDomains.contains(identifier.jmxDomain()))
				.findAny();
		PortIdentifier portIdentifier = null;
		if(optIdentifier.isEmpty()){
			portIdentificationResult = PortIdentificationResult.errorWithdefaults("servlet container not detected");
		}else{
			Identifier identifier = optIdentifier.get();
			logger.info("{} detected as servlet container", identifier.name());
			portIdentifier = injector.getInstance(identifier.portIdentifier());
			portIdentificationResult = portIdentifier.identify();
		}
		logger.warn("Using ports http={} https={} from {} error: {}",
				portIdentificationResult.httpPort(),
				portIdentificationResult.httpsPort(),
				portIdentifier,
				portIdentificationResult.errorMessage());
	}

	public Integer getHttpPort(){
		return portIdentificationResult.httpPort();
	}

	public Integer getHttpsPort(){
		return portIdentificationResult.httpsPort();
	}

	private record Identifier(
			String name,
			String jmxDomain,
			Class<? extends PortIdentifier> portIdentifier){
	}

}
