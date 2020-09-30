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
package io.datarouter.gcp.spanner.client;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.SystemTool;
import io.datarouter.util.lang.ObjectTool;

@Singleton
public class SpannerClientOptions{
	private static final Logger logger = LoggerFactory.getLogger(SpannerClientOptions.class);

	private static final String PREFIX_SPANNER = "spanner.";

	protected static final String PROP_projectId = "projectId";
	protected static final String PROP_instanceId = "instanceId";
	protected static final String PROP_databaseName = "databaseName";
	protected static final String PROP_credentialsLocation = "credentialsLocation";

	@Inject
	private ClientOptions clientOptions;

	public String projectId(String clientName){
		return clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_projectId));
	}

	public String instanceId(String clientName){
		return clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_instanceId));
	}

	public String findProjectId(String clientName){
		return clientOptions.optString(clientName, makeSpannerKey(PROP_projectId)).orElse("");
	}

	public String findInstanceId(String clientName){
		return clientOptions.optString(clientName, makeSpannerKey(PROP_instanceId)).orElse("");
	}

	public String databaseName(String clientName){
		return clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_databaseName));
	}

	public String credentialsLocation(String clientName){
		String provided = clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_credentialsLocation));
		String corrected = provided.replace("~", SystemTool.getUserHome());
		if(ObjectTool.notEquals(provided, corrected)){
			logger.warn("updated credentialsLocation from {} to {}", provided, corrected);
		}
		return corrected;
	}

	protected static String makeSpannerKey(String propertyKey){
		return PREFIX_SPANNER + propertyKey;
	}

}
