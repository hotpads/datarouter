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

import java.util.function.Supplier;

import javax.inject.Singleton;
import javax.management.ObjectName;

import io.datarouter.util.MxBeans;
import io.datarouter.util.singletonsupplier.SingletonSupplier;

@Singleton
public class WildFlyPortIdentifier implements PortIdentifier{

	private static final String
			HTTP = "socket-binding-group=standard-sockets,socket-binding=http",
			HTTPS = "socket-binding-group=standard-sockets,socket-binding=https",
			PORT_ATTRIBUTE = "port";

	private final Supplier<Port> ports;

	public WildFlyPortIdentifier(){
		this.ports = SingletonSupplier.of(() -> {
			try{
				ObjectName objectName = new ObjectName(CompoundPortIdentifier.JBOSS_JMX_DOMAIN + ":" + HTTP);
				int http = (int)MxBeans.SERVER.getAttribute(objectName, PORT_ATTRIBUTE);
				objectName = new ObjectName(CompoundPortIdentifier.JBOSS_JMX_DOMAIN + ":" + HTTPS);
				int https = (int)MxBeans.SERVER.getAttribute(objectName, PORT_ATTRIBUTE);
				return new Port(http, https);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Integer getHttpPort(){
		return ports.get().httpPort();
	}

	@Override
	public Integer getHttpsPort(){
		return ports.get().httpsPort();
	}

	private record Port(
			Integer httpPort,
			Integer httpsPort){
	}

}
