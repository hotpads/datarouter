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
package io.datarouter.auth.web.service;

import java.time.Duration;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.storage.cache.CaffeineLoadingCache;
import io.datarouter.storage.cache.CaffeineLoadingCache.CaffeineLoadingCacheBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountRefererService{

	private final CaffeineLoadingCache<String,RefererCheck> checkByAccountName;

	@Inject
	public DatarouterAccountRefererService(DatarouterAccountDao accountDao){
		checkByAccountName = new CaffeineLoadingCacheBuilder<String,RefererCheck>()
				.withName("AccountRefererCheck")
				.withExpireTtl(Duration.ofMinutes(1))
				.withLoadingFunction(accountName -> {
					DatarouterAccount account = accountDao.get(new DatarouterAccountKey(accountName));
					String referer = account.getReferrer();
					if(referer == null){
						return _ -> new DatarouterAccountRefererCheck(true, false);
					}
					return request -> new DatarouterAccountRefererCheck(refererAllowed(request, referer), true);
				})
				.build();
	}

	public DatarouterAccountRefererCheck validateAccountReferer(String accountName, HttpServletRequest request){
		return checkByAccountName.getOrThrow(accountName).apply(request);
	}

	public record DatarouterAccountRefererCheck(
			boolean allowed,
			boolean hasRefererValidation){
	}

	private static boolean refererAllowed(HttpServletRequest request, String referer){
		String reqReferer = request.getHeader(HttpHeaders.REFERER);
		return reqReferer != null && reqReferer.startsWith(referer);
	}

	//unnecessary, just to shorten type definition
	private interface RefererCheck extends Function<HttpServletRequest,DatarouterAccountRefererCheck>{}

}
