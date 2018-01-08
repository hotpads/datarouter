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
package io.datarouter.storage.client;

import java.util.Collection;
import java.util.Properties;

import io.datarouter.storage.routing.BaseRouter;
import io.datarouter.util.properties.TypedProperties;

public class RouterOptions extends TypedProperties{

	public RouterOptions(Collection<Properties> propertiesList){
		super(propertiesList);
	}

	public RouterOptions(String propertiesPath){
		super(propertiesPath);
	}

	protected String getRouterPrefix(String routerName){
		return "router." + routerName + ".";
	}

	protected String getClientPrefix(String clientName){
		return "client." + clientName + ".";
	}

	protected String prependRouterPrefix(String routerName, String toAppend){
		return getRouterPrefix(routerName) + toAppend;
	}

	protected String prependClientPrefix(String clientName, String toAppend){
		return getClientPrefix(clientName) + toAppend;
	}

	/***************** actual variables *********************************/

	public String getClientType(String clientName){
		String typeNameKey = prependClientPrefix(clientName, "type");
		String typeName = getString(typeNameKey);
		if(typeName != null){
			return typeName;
		}
		throw new RuntimeException("Client " + clientName + " does not have a client type in its configuration file");
	}

	public String getMode(String routerName){
		return getString(prependRouterPrefix(routerName, "mode"), BaseRouter.MODE_production);
	}

}