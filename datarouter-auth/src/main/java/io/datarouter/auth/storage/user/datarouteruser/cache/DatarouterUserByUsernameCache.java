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
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.storage.cache.CaffeineLoadingCache.CaffeineLoadingCacheBuilder;
import io.datarouter.storage.cache.CaffeineLoadingCacheWrapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserByUsernameCache extends CaffeineLoadingCacheWrapper<String,DatarouterUser>{

	@Inject
	public DatarouterUserByUsernameCache(DatarouterUserDao datarouterUserDao){
		super(new CaffeineLoadingCacheBuilder<String,DatarouterUser>()
				.withLoadingFunction(key -> datarouterUserDao.getByUsername(new DatarouterUserByUsernameLookup(key)))
				.withExpireTtl(Duration.ofSeconds(6))
				.withExceptionFunction(key -> new InvalidCredentialsException("username not found (" + key + ")"))
				.withStatsRecording()
				.build());
	}

}
