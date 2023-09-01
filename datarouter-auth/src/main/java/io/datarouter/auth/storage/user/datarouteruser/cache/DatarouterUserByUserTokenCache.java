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
package io.datarouter.auth.storage.user.datarouteruser.cache;

import java.time.Duration;

import io.datarouter.auth.exception.InvalidCredentialsException;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.util.cache.LoadingCache.LoadingCacheBuilder;
import io.datarouter.util.cache.LoadingCacheWrapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserByUserTokenCache extends LoadingCacheWrapper<String,DatarouterUser>{

	@Inject
	public DatarouterUserByUserTokenCache(DatarouterUserDao datarouterUserDao){
		super(new LoadingCacheBuilder<String,DatarouterUser>()
				.withLoadingFunction(key -> datarouterUserDao.getByUserToken(new DatarouterUserByUserTokenLookup(key)))
				.withExpireTtl(Duration.ofSeconds(6))
				.withExceptionFunction(key -> new InvalidCredentialsException("userToken not found (" + key + ")"))
				.build());
	}

}
