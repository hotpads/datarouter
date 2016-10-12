package com.hotpads.handler.port;

import java.lang.management.ManagementFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.hotpads.util.http.security.UrlScheme;

@Singleton
public class TomcatPortIdentifier implements PortIdentifier{

	private Integer httpPort;
	private Integer httpsPort;

	@Inject
	public TomcatPortIdentifier() throws MalformedObjectNameException{
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		ObjectName query = new ObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=ProtocolHandler,*");
		server.queryNames(query, null).stream().forEach(objectName -> {
			int port;
			boolean sslEnabled;
			String scheme;
			try{
				port = (int)server.getAttribute(objectName, "port");
				sslEnabled = (boolean)server.getAttribute(objectName, "sSLEnabled");
				objectName = new ObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=Connector,port="
						+ port);
				scheme = (String)server.getAttribute(objectName, "scheme");
			}catch(JMException e){
				throw new RuntimeException(e);
			}
			if(sslEnabled){
				this.httpsPort = port;
			}else{
				if(scheme.equals(UrlScheme.HTTP.getStringRepresentation())){
					this.httpPort = port;
				}
			}
		});
		if(this.httpPort == null || this.httpsPort == null){
			throw new RuntimeException("TomcatPortIdentifier didn't found port numbers in jmx");
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
