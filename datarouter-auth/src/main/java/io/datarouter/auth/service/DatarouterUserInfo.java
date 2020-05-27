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
package io.datarouter.auth.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.cache.DatarouterUserByIdCache;
import io.datarouter.auth.cache.DatarouterUserByUserTokenCache;
import io.datarouter.auth.cache.DatarouterUserByUsernameCache;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.session.service.Role;

@Singleton
public class DatarouterUserInfo implements UserInfo{

	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterUserByUsernameCache datarouterUserByUsernameCache;
	@Inject
	private DatarouterUserByUserTokenCache datarouterUserByUserTokenCache;
	@Inject
	private DatarouterUserByIdCache datarouterUserByIdCache;

	@Override
	public Scanner<DatarouterUser> scanAllUsers(boolean enabledOnly, Set<Role> includedRoles){
		if(includedRoles.isEmpty()){
			return Scanner.empty();
		}
		return userDao.scan()
				.include(user -> !enabledOnly || user.getEnabled())
				.include(user -> user.getRoles().stream().anyMatch(includedRoles::contains));
	}

	@Override
	public Optional<DatarouterUser> getUserByUsername(String username, boolean allowCached){
		if(StringTool.isEmptyOrWhitespace(username)){
			return Optional.empty();
		}
		if(allowCached){
			return datarouterUserByUsernameCache.get(username);
		}
		return Optional.ofNullable(userDao.getByUsername(new DatarouterUserByUsernameLookup(username)));
	}

	@Override
	public Optional<DatarouterUser> getUserByToken(String token, boolean allowCached){
		if(StringTool.isEmptyOrWhitespace(token)){
			return Optional.empty();
		}
		if(allowCached){
			return datarouterUserByUserTokenCache.get(token);
		}
		return userDao.find(new DatarouterUserByUserTokenLookup(token));
	}

	@Override
	public Optional<DatarouterUser> getUserById(Long id, boolean allowCached){
		if(id == null){
			return Optional.empty();
		}
		if(allowCached){
			return datarouterUserByIdCache.get(id);
		}
		return userDao.find(new DatarouterUserKey(id));
	}

	private Set<Role> getRolesFromUser(Optional<? extends DatarouterUser> user){
		return user.map(DatarouterUser::getRoles)
				.map(HashSet::new)
				.orElseGet(HashSet::new);
	}

	@Override
	public Set<Role> getRolesByUsername(String username, boolean allowCached){
		return getRolesFromUser(getUserByUsername(username, allowCached));
	}

	@Override
	public Set<Role> getRolesByToken(String token, boolean allowCached){
		return getRolesFromUser(getUserByToken(token, allowCached));
	}

	@Override
	public Set<Role> getRolesById(Long id, boolean allowCached){
		return getRolesFromUser(getUserById(id, allowCached));
	}

}
