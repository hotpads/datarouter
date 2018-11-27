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
package io.datarouter.web.user.cache;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.web.cache.LookupCacheFactory;
import io.datarouter.web.cache.LookupCacheFactory.LookupCacheFactoryConfig;
import io.datarouter.web.exception.InvalidCredentialsException;
import io.datarouter.web.user.DatarouterUserNodes;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserKey;

@Singleton
public class DatarouterUserByIdCache extends BaseDatarouterUserLookupCacheWrapper<Long>{

	@Inject
	public DatarouterUserByIdCache(DatarouterUserNodes userNodes, LookupCacheFactory lookupCacheFactory){
		super(lookupCacheFactory, new LookupCacheFactoryConfig<Long,DatarouterUser>()
				.withLookup(key -> userNodes.getUserNode().get(new DatarouterUserKey(key), null))
				.withExceptionFunction(key -> new InvalidCredentialsException("user id not found (" + key + ")")));
	}

}
