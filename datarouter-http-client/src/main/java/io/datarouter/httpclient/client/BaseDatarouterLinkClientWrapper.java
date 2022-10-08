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
package io.datarouter.httpclient.client;

import io.datarouter.httpclient.endpoint.link.BaseLink;
import io.datarouter.httpclient.endpoint.link.LinkType;

public abstract class BaseDatarouterLinkClientWrapper<
		L extends LinkType>
implements DatarouterLinkClient<L>{

	private final DatarouterLinkClient<L> client;

	public BaseDatarouterLinkClientWrapper(DatarouterLinkClient<L> client){
		this.client = client;
	}

	@Override
	public String toUrl(BaseLink<L> link){
		return client.toUrl(link);
	}

	@Override
	public void shutdown(){
		client.shutdown();
	}

	@Override
	public void initUrlPrefix(BaseLink<L> link){
		client.initUrlPrefix(link);
	}

}
