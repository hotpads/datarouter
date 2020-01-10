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
package io.datarouter.client.memcached.web;

import javax.inject.Inject;

import io.datarouter.client.memcached.DatarouterMemcachedFiles;
import io.datarouter.client.memcached.MemcachedClientType;
import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.MemcachedOptions;
import io.datarouter.client.memcached.client.SpyMemcachedClient;
import io.datarouter.web.browse.DatarouterClientWebInspector;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory;
import io.datarouter.web.browse.dto.DatarouterWebRequestParamsFactory.DatarouterWebRequestParams;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.params.Params;

public class MemcachedWebInspector implements DatarouterClientWebInspector{

	@Inject
	private MemcachedOptions memcachedOptions;
	@Inject
	private DatarouterWebRequestParamsFactory datarouterWebRequestParamsFactory;
	@Inject
	private MemcachedClientManager memcachedClientManager;
	@Inject
	private DatarouterMemcachedFiles files;

	@Override
	public Mav inspectClient(Params params){
		DatarouterWebRequestParams<MemcachedClientType> routerParams = datarouterWebRequestParamsFactory
				.new DatarouterWebRequestParams<>(params, MemcachedClientType.class);
		SpyMemcachedClient spyClient = memcachedClientManager.getSpyMemcachedClient(routerParams.getClientId());
		Mav mav = new Mav(files.jsp.admin.datarouter.memcached.memcachedClientSummaryJsp);
		mav.put("client", spyClient);
		mav.put("nodes", memcachedOptions.getServers(routerParams.getClientId().getName()));
		mav.put("memcachedStats", spyClient.getStats());
		return mav;
	}

}
