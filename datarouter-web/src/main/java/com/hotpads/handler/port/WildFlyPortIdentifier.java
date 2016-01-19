package com.hotpads.handler.port;

import java.lang.management.ManagementFactory;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.concurrent.Lazy;

@Singleton
public class WildFlyPortIdentifier implements PortIdentifier{

	private static final String
			HTTP = "socket-binding-group=standard-sockets,socket-binding=http",
			HTTPS = "socket-binding-group=standard-sockets,socket-binding=https",
			PORT_ATTRIBUTE = "port";

	public static class WildFlyPortIdentifierProvider extends Lazy<WildFlyPortIdentifier>{

		@Override
		protected WildFlyPortIdentifier load(){
			return new WildFlyPortIdentifier();
		}

	}

	private final Lazy<Pair<Integer,Integer>> ports;

	public WildFlyPortIdentifier(){
		ports = Lazy.of(()->{
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			try {
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
