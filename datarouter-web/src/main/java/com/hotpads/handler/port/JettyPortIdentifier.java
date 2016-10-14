package com.hotpads.handler.port;

import java.lang.management.ManagementFactory;

import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

@Singleton
public class JettyPortIdentifier implements PortIdentifier{

	private static final String
			TYPE_PROPERTY = "type",
			SERVERCONNECTOR_MBEAN_TYPE = "serverconnector",
			PORT = "port",
			DEFAULT_PROTOCOL = "defaultProtocol",
			SSL_PROTOCOL = "SSL";

	private int httpPort;
	private int httpsPort;

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
					}catch(JMException e){
						throw new RuntimeException(e);
					}
					if(defaultProtocol.equals(SSL_PROTOCOL)){
						this.httpsPort = port;
					}else{
						this.httpPort = port;
					}
				});
		if(this.httpPort == 0 || this.httpsPort == 0){
			throw new RuntimeException("JettyPortIdentifier didn't found port numbers in jmx");
		}
	}

	@Override
	public Integer getHttpPort(){
		return httpPort;
	}

	@Override
	public Integer getHttpsPort(){
		return httpsPort;
	}

}
