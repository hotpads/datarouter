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
import java.util.function.Supplier;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.tuple.Pair;

@Singleton
public class WildFlyPortIdentifier implements PortIdentifier{

	private static final String
			HTTP = "socket-binding-group=standard-sockets,socket-binding=http",
			HTTPS = "socket-binding-group=standard-sockets,socket-binding=https",
			PORT_ATTRIBUTE = "port";

	private final Supplier<Pair<Integer,Integer>> ports;

	public WildFlyPortIdentifier(){
		this.ports = SingletonSupplier.of(() -> {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			try{
				ObjectName objectName = new ObjectName(CompoundPortIdentifier.JBOSS_JMX_DOMAIN + ":" + HTTP);
				int http = (int)server.getAttribute(objectName, PORT_ATTRIBUTE);
				objectName = new ObjectName(CompoundPortIdentifier.JBOSS_JMX_DOMAIN + ":" + HTTPS);
				int https = (int)server.getAttribute(objectName, PORT_ATTRIBUTE);
				return new Pair<>(http, https);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Integer getHttpPort(){
		return ports.get().getLeft();
	}

	@Override
	public Integer getHttpsPort(){
		return ports.get().getRight();
	}

}
