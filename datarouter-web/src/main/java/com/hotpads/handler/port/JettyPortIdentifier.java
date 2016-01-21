package com.hotpads.handler.port;

import java.lang.management.ManagementFactory;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.hotpads.util.core.concurrent.Lazy;

@Singleton
public class JettyPortIdentifier implements PortIdentifier{

	@Singleton
	public static class JettyPortIdentifierProvider extends Lazy<JettyPortIdentifier>{

		@Override
		protected JettyPortIdentifier load(){
			try{
				return new JettyPortIdentifier();
			}catch(MalformedObjectNameException e){
				throw new RuntimeException(e);
			}
		}

	}

	private static final String
			TYPE_PROPERTY = "type",
			SERVERCONNECTOR_MBEAN_TYPE = "serverconnector",
			PORT = "port",
			DEFAULT_PROTOCOL = "defaultProtocol",
			SSL_PROTOCOL = "SSL";

	private int http;
	private int https;

	public JettyPortIdentifier() throws MalformedObjectNameException{
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		ObjectName jettyServerNames = new ObjectName(CompoundPortIdentifier.JETTY_SERVER_JMX_DOMAIN + ":*");
		server.queryNames(jettyServerNames, null).stream()
				.filter(objectName -> objectName.getKeyPropertyList().get(TYPE_PROPERTY)
						.equals(SERVERCONNECTOR_MBEAN_TYPE))
				.forEach(objectName -> {
					int port;
					String defaultProtocol;
					try{
						port = (int)server.getAttribute(objectName, PORT);
						defaultProtocol = (String)server.getAttribute(objectName, DEFAULT_PROTOCOL);
					}catch(Exception e){
						throw new RuntimeException(e);
					}
					if(defaultProtocol.equals(SSL_PROTOCOL)){
						https = port;
					}else{
						http = port;
					}
				});
		if(http == 0 || https == 0){
			throw new RuntimeException("JettyPortIdentifier didn't found port numbers in jmx");
		}
	}

	@Override
	public Integer getHttpPort(){
		return http;
	}

	@Override
	public Integer getHttpsPort(){
		return https;
	}

}
