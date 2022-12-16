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

@Singleton
public class WildFlyPortIdentifier implements PortIdentifier{

	private static final String
			HTTP = "socket-binding-group=standard-sockets,socket-binding=http",
			HTTPS = "socket-binding-group=standard-sockets,socket-binding=https",
			PORT_ATTRIBUTE = "port";

	@Override
	public PortIdentificationResult identify(){
		var httpSocketBinding = JmxTool.newObjectName(CompoundPortIdentifier.JBOSS_JMX_DOMAIN + ":" + HTTP);
		int http = (int)JmxTool.getAttribute(httpSocketBinding, PORT_ATTRIBUTE);
		var httpsSocketBinding = JmxTool.newObjectName(CompoundPortIdentifier.JBOSS_JMX_DOMAIN + ":" + HTTPS);
		int https = (int)JmxTool.getAttribute(httpsSocketBinding, PORT_ATTRIBUTE);
		return PortIdentificationResult.success(http, https);
	}

}
