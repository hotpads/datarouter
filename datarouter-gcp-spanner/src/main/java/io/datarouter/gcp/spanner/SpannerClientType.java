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
package io.datarouter.gcp.spanner;

import javax.inject.Inject;

import io.datarouter.gcp.spanner.web.SpannerWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;

public class SpannerClientType implements ClientType<SpannerClientNodeFactory,SpannerClientManager>{

	public static final String NAME = "spanner";

	@Inject
	public SpannerClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, SpannerWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<SpannerClientNodeFactory> getClientNodeFactoryClass(){
		return SpannerClientNodeFactory.class;
	}

	@Override
	public Class<SpannerClientManager> getClientManagerClass(){
		return SpannerClientManager.class;
	}

	@Override
	public boolean supportsOffsetSampling(){
		return true;
	}

}
