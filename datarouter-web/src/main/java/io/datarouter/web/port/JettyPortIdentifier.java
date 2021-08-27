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

import javax.inject.Singleton;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.datarouter.util.MxBeans;

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
		ObjectName jettyServerNames = new ObjectName(CompoundPortIdentifier.JETTY_SERVER_JMX_DOMAIN + ":*");
		MxBeans.SERVER.queryNames(jettyServerNames, null).stream()
				.filter(objectName -> objectName.getKeyPropertyList().get(TYPE_PROPERTY)
						.equals(SERVERCONNECTOR_MBEAN_TYPE))
				.forEach(objectName -> {
					int port;
					String defaultProtocol;
					try{
						port = (int)MxBeans.SERVER.getAttribute(objectName, PORT);
						defaultProtocol = (String)MxBeans.SERVER.getAttribute(objectName, DEFAULT_PROTOCOL);
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
			throw new RuntimeException("JettyPortIdentifier didn't find port numbers in jmx");
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
