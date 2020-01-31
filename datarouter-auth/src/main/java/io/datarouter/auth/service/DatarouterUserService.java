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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.RoleManager;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserService{

	@Inject
	private DatarouterUserDao nodes;
	@Inject
	private RoleManager roleManager;

	public DatarouterUser getAndValidateCurrentUser(DatarouterSession session){
		DatarouterUser user = getUserBySession(session);
		if(user == null || !user.getEnabled()){
			throw new RuntimeException("Current user does not exist or is not enabled.");
		}
		return user;
	}

	public DatarouterUser getUserBySession(DatarouterSession session){
		if(session == null || session.getUserId() == null){
			return null;
		}
		return nodes.get(new DatarouterUserKey(session.getUserId()));
	}

	public DatarouterUser getUserById(Long id){
		return nodes.get(new DatarouterUserKey(id));
	}

	public boolean canEditUser(DatarouterUser user, DatarouterUser editor){
		return user.equals(editor)
				|| !isAdmin(user)
				&& roleManager.isAdmin(editor.getRoles())
				&& editor.getEnabled();
	}

	public boolean canHavePassword(DatarouterUser user){
		return user.getPasswordDigest() != null || isAdmin(user);
	}

	public boolean isPasswordCorrect(DatarouterUser user, String rawPassword){
		if(user == null || rawPassword == null){
			return false;
		}
		String passwordDigest = PasswordTool.digest(user.getPasswordSalt(), rawPassword);
		return Objects.equals(user.getPasswordDigest(), passwordDigest);
	}

	public boolean isPasswordCorrect(String email, String rawPassword){
		DatarouterUser user = nodes.getByUsername(new DatarouterUserByUsernameLookup(email));
		return isPasswordCorrect(user, rawPassword);
	}

	public Set<Role> getAllowedUserRoles(DatarouterUser currentUser, String[] userRoleStrings){
		Set<Role> userRoles = ArrayTool.mapToSet(roleManager::getRoleFromPersistentString, userRoleStrings);
		Collection<Role> validRoles = roleManager.getConferrableRoles(currentUser.getRoles());
		userRoles.retainAll(validRoles);
		userRoles.add(DatarouterUserRole.REQUESTOR.getRole());// everyone should have this
		return userRoles;
	}

	public void assertUserDoesNotExist(Long id, String userToken, String username){
		DatarouterUser userWithId = getUserById(id);
		if(userWithId != null){
			throw new IllegalArgumentException("DatarouterUser already exists with id=" + id);
		}
		DatarouterUser userWithUserToken = nodes.getByUserToken(new DatarouterUserByUserTokenLookup(userToken));
		if(userWithUserToken != null){
			throw new IllegalArgumentException("DatarouterUser already exists with userToken=" + userToken);
		}
		DatarouterUser userWithEmail = nodes.getByUsername(new DatarouterUserByUsernameLookup(username));
		if(userWithEmail != null){
			throw new IllegalArgumentException("DatarouterUser already exists with username=" + username);
		}
	}

	public boolean isAdmin(DatarouterUser user){
		return user.getRoles().contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole());
	}

}
