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
package io.datarouter.aws.s3;

import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.aws.s3.client.S3ClientNodeFactory;
import io.datarouter.aws.s3.client.S3WebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class S3ClientType implements ClientType<S3ClientNodeFactory,S3ClientManager>{

	public static final String NAME = "s3";

	@Inject
	public S3ClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, S3WebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<S3ClientNodeFactory> getClientNodeFactoryClass(){
		return S3ClientNodeFactory.class;
	}

	@Override
	public Class<S3ClientManager> getClientManagerClass(){
		return S3ClientManager.class;
	}

}
