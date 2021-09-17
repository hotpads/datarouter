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
package io.datarouter.websocket.service;

import javax.inject.Inject;

import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.storage.config.properties.DatarouterServerTypeSupplier;
import io.datarouter.storage.config.properties.ServerPrivateIp;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.port.CompoundPortIdentifier;

public abstract class BaseServerAddressProvider implements ServerAddressProvider{

	private final ServletContextSupplier servletContext;
	private final CompoundPortIdentifier portIdentifier;
	private final ServerPrivateIp serverPrivateIp;
	private final DatarouterServerTypeSupplier serverType;

	private final String dispatcherUrl;

	@Inject
	public BaseServerAddressProvider(
			ServletContextSupplier servletContext,
			CompoundPortIdentifier portIdentifier,
			ServerPrivateIp serverPrivateIp,
			DatarouterServerTypeSupplier serverType,
			String dispatcherUrl){
		this.servletContext = servletContext;
		this.portIdentifier = portIdentifier;
		this.serverPrivateIp = serverPrivateIp;
		this.serverType = serverType;
		this.dispatcherUrl = dispatcherUrl;
	}

	@Override
	public String get(){
		String hostName = getHostName();
		int port = getPort();
		String contextPath = getContextPath();
		return hostName + ":" + port + contextPath + dispatcherUrl;
	}

	protected String getHostName(){
		String localIp = serverPrivateIp.get();
		if(localIp != null){
			return localIp;
		}
		return "localhost";
	}

	protected int getPort(){
		if(serverType.get().getPersistentString().equals(ServerType.DEV.getPersistentString())){
			return UrlConstants.PORT_HTTP_DEV;
		}
		return portIdentifier.getHttpPort();
	}

	private String getContextPath(){
		return servletContext.get().getContextPath();
	}

}
