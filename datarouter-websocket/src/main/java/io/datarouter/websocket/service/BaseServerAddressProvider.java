/**
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
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.web.config.ServletContextSupplier;

public abstract class BaseServerAddressProvider implements ServerAddressProvider{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private ServletContextSupplier servletContext;

	private final String dispatcherUrl;

	public BaseServerAddressProvider(String dispatcherUrl){
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
		String localIp = datarouterProperties.getServerPrivateIp();
		if(localIp != null){
			return localIp;
		}
		return "localhost";
	}

	protected int getPort(){
		if(datarouterProperties.getServerType().getPersistentString().equals(ServerType.DEV
				.getPersistentString())){
			return UrlConstants.PORT_HTTP_DEV;
		}
		return UrlConstants.PORT_HTTP_STANDARD;
	}

	private String getContextPath(){
		return servletContext.get().getContextPath();
	}

}
