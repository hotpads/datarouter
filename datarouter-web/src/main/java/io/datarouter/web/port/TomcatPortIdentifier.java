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

import java.lang.management.ManagementFactory;

import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.datarouter.httpclient.security.UrlScheme;

@Singleton
public class TomcatPortIdentifier implements PortIdentifier{

	private Integer httpPort;
	private Integer httpsPort;

	public TomcatPortIdentifier() throws MalformedObjectNameException{
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		ObjectName query = new ObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=ProtocolHandler,*");
		server.queryNames(query, null).forEach(objectName -> {
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
			throw new RuntimeException("TomcatPortIdentifier didn't find port numbers in jmx");
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
