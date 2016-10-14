package com.hotpads.handler.port;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.util.core.collections.Triple;

@Singleton
public class CompoundPortIdentifier implements PortIdentifier{
	private static final Logger logger = LoggerFactory.getLogger(CompoundPortIdentifier.class);

	public static final String
			COMPOUND_PORT_IDENTIFIER = "compoundPortIdentifier",
			CATALINA_JMX_DOMAIN = "Catalina",
			JETTY_SERVER_JMX_DOMAIN = "org.eclipse.jetty.server",
			JBOSS_JMX_DOMAIN = "jboss.as";

	private static final List<Triple<String,String,Class<? extends PortIdentifier>>> IDENTIFIERS = Arrays.asList(
			new Triple<>("Tomcat", CATALINA_JMX_DOMAIN, TomcatPortIdentifier.class),
			new Triple<>("Jetty", JETTY_SERVER_JMX_DOMAIN, JettyPortIdentifier.class),
			new Triple<>("Wildfly (JBoss)", JBOSS_JMX_DOMAIN, WildFlyPortIdentifier.class));

	private PortIdentifier portIdentifier;

	@Inject
	public CompoundPortIdentifier(DatarouterInjector injector){
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		List<String> domains = Arrays.asList(server.getDomains());
		Optional<Triple<String,String,Class<? extends PortIdentifier>>> optIdentifier = IDENTIFIERS.stream()
				.filter(triple -> domains.contains(triple.getSecond()))
				.findAny();
		if(!optIdentifier.isPresent()){
			logger.error("Servlet container not detected. Expect some features to not work.");
			return;
		}
		Triple<String,String,Class<? extends PortIdentifier>> identifier = optIdentifier.get();
		logger.info("{} detected as servlet container", identifier.getFirst());
		portIdentifier = injector.getInstance(identifier.getThird());
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
