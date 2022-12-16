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

import java.util.ArrayList;

import javax.inject.Singleton;

import org.apache.commons.lang3.mutable.MutableInt;

import io.datarouter.util.MxBeans;

@Singleton
public class JettyPortIdentifier implements PortIdentifier{

	@Override
	public PortIdentificationResult identify(){
		var httpPort = new MutableInt();
		var httpsPort = new MutableInt();
		var jettyServerNames = JmxTool.newObjectName(CompoundPortIdentifier.JETTY_SERVER_JMX_DOMAIN + ":*");
		var serverConnectors = new ArrayList<String>();
		MxBeans.SERVER.queryNames(jettyServerNames, null).stream()
				.filter(objectName -> "serverconnector".equals(objectName.getKeyPropertyList().get("type")))
				.forEach(serverConnector -> {
					serverConnectors.add(serverConnector.toString());
					int port = (int)JmxTool.getAttribute(serverConnector, "port");
					String defaultProtocol = (String)JmxTool.getAttribute(serverConnector, "defaultProtocol");
					if("SSL".equals(defaultProtocol)){
						httpsPort.setValue(port);
					}else{
						httpPort.setValue(port);
					}
				});
		if(httpPort.intValue() == 0 || httpsPort.intValue() == 0){
			return PortIdentificationResult.errorWithdefaults("port not found httpPort=" + httpPort + " httpsPort="
					+ httpsPort + " serverConnectors=" + serverConnectors);
		}
		return PortIdentificationResult.success(httpPort.intValue(), httpsPort.intValue());
	}

}
