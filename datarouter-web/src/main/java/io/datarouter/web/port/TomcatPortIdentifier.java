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

import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.util.MxBeans;

@Singleton
public class TomcatPortIdentifier implements PortIdentifier{

	@Override
	public PortIdentificationResult identify(){
		var httpPort = new MutableInt();
		var httpsPort = new MutableInt();
		var query = JmxTool.newObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=ProtocolHandler,*");
		var handlers = new ArrayList<String>();
		MxBeans.SERVER.queryNames(query, null).forEach(protocolHandler -> {
			handlers.add(protocolHandler.toString());
			int port = (int)JmxTool.getAttribute(protocolHandler, "port");
			var connector = JmxTool.newObjectName(CompoundPortIdentifier.CATALINA_JMX_DOMAIN + ":type=Connector,port="
					+ port);
			String scheme = (String)JmxTool.getAttribute(connector, "scheme");
			if(scheme.equals(UrlScheme.HTTPS.getStringRepresentation())){
				httpsPort.setValue(port);
			}else if(scheme.equals(UrlScheme.HTTP.getStringRepresentation())){
				httpPort.setValue(port);
			}
		});
		if(httpPort.intValue() == 0 || httpsPort.intValue() == 0){
			return PortIdentificationResult.errorWithdefaults("port not found httpPort=" + httpPort + " httpsPort="
					+ httpsPort + " handlers=" + handlers);
		}
		return PortIdentificationResult.success(httpPort.intValue(), httpsPort.intValue());
	}

}
