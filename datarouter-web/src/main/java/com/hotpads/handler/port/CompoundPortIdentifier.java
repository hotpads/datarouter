package com.hotpads.handler.port;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.port.JettyPortIdentifier.JettyPortIdentifierProvider;
import com.hotpads.handler.port.TomcatServerXmlPortIdentifier.TomcatServerXmlPortIdentifierProvider;
import com.hotpads.handler.port.WildFlyPortIdentifier.WildFlyPortIdentifierProvider;

@Singleton
public class CompoundPortIdentifier implements PortIdentifier{
	private static final Logger logger = LoggerFactory.getLogger(CompoundPortIdentifier.class);

	public static final String
			COMPOUND_PORT_IDENTIFIER = "compoundPortIdentifier",
			CATALINA_JMX_DOMAIN = "Catalina",
			JETTY_SERVER_JMX_DOMAIN = "org.eclipse.jetty.server",
			JBOSS_JMX_DOMAIN = "jboss.as";

	private PortIdentifier portIdentifier;

	@Inject
	public CompoundPortIdentifier(TomcatServerXmlPortIdentifierProvider tomcatServerXmlPortIdentifier,
			JettyPortIdentifierProvider jettyPortIdentifier, WildFlyPortIdentifierProvider wildFlyPortIdentifier){
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		List<String> domains = Arrays.asList(server.getDomains());
		if(domains.contains(JETTY_SERVER_JMX_DOMAIN)){
			logger.warn("Jetty detected as servlet container");
			portIdentifier = jettyPortIdentifier.get();
		}else if(domains.contains(CATALINA_JMX_DOMAIN)){
			logger.warn("Tomcat detected as servlet container");
			// TODO use JMX
			portIdentifier = tomcatServerXmlPortIdentifier.get();
		}else if(domains.contains(JBOSS_JMX_DOMAIN)){
			logger.warn("Wildfly (JBoss) detected as servlet container");
			portIdentifier = wildFlyPortIdentifier.get();
		}else{
			logger.error("Servlet container not detected. Expect some features to not work.");
		}
	}

	@Override
	public Integer getHttpPort(){
		return portIdentifier.getHttpPort();
	}

	@Override
	public Integer getHttpsPort(){
		return portIdentifier.getHttpsPort();
	}

}
