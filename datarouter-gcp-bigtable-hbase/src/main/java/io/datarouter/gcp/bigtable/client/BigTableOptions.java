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
package io.datarouter.gcp.bigtable.client;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.client.HBaseOptions;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.SystemTool;
import io.datarouter.util.lang.ObjectTool;

@Singleton
public class BigTableOptions extends HBaseOptions{
	private static final Logger logger = LoggerFactory.getLogger(BigTableOptions.class);

	private static final String PREFIX_bigtable = "bigtable.";

	protected static final String PROP_projectId = "projectId";
	protected static final String PROP_instanceId = "instanceId";
	protected static final String PROP_credentialsLocation = "credentialsLocation";

	@Inject
	private ClientOptions clientOptions;

	public String projectId(String clientName){
		return clientOptions.getRequiredString(clientName, makeBigtableKey(PROP_projectId));
	}

	public String instanceId(String clientName){
		return clientOptions.getRequiredString(clientName, makeBigtableKey(PROP_instanceId));
	}

	public String credentialsLocation(String clientName){
		String provided = clientOptions.getRequiredString(clientName, makeBigtableKey(PROP_credentialsLocation));
		String corrected = provided.replace("~", SystemTool.getUserHome());
		if(ObjectTool.notEquals(provided, corrected)){
			logger.warn("updated credentialsLocation from {} to {}", provided, corrected);
		}
		return corrected;
	}

	protected static String makeBigtableKey(String propertyKey){
		return PREFIX_bigtable + propertyKey;
	}

}
