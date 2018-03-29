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
package io.datarouter.web.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.BooleanTool;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserHistory;
import io.datarouter.web.user.databean.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.util.PasswordTool;

@Singleton
public class DatarouterUserEditService{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserEditService.class);

	private final DatarouterUserHistoryService userHistoryService;

	@Inject
	public DatarouterUserEditService(DatarouterUserHistoryService userHistoryService){
		this.userHistoryService = userHistoryService;
	}

	public void editUser(DatarouterUser user, DatarouterUser editor, String[] requestedRoles, Boolean enabled,
			String signinUrl){
		DatarouterUserHistory history = new DatarouterUserHistory(user.getId(), new Date(), editor.getId(),
				DatarouterUserChangeType.EDIT, null);

		List<String> changes = new ArrayList<>();

		Set<DatarouterUserRole> allowedRoles = DatarouterUserDao.getAllowedUserRoles(editor, requestedRoles);
		Set<DatarouterUserRole> currentRoles = new TreeSet<>(user.getRoles());
		if(!allowedRoles.equals(currentRoles)){
			changes.add(change("roles", currentRoles, allowedRoles));
			user.setRoles(allowedRoles);
		}
		if(!BooleanTool.nullSafeSame(enabled, user.getEnabled())){
			changes.add(change("enabled", user.getEnabled(), enabled));
			user.setEnabled(enabled);
		}

		if(changes.size() > 0){
			history.setChanges(String.join(", ", changes));
			userHistoryService.recordEdit(user, history, signinUrl);
		}else{
			logger.warn("User {} submitted edit request for user {}, but no changes were made.", editor.toString(), user
					.toString());
		}
	}

	public void changePassword(DatarouterUser user, DatarouterUser editor, String newPassword, String signinUrl){
		DatarouterUserHistory history = new DatarouterUserHistory(user.getId(), new Date(), editor.getId(),
				DatarouterUserChangeType.RESET, null);
		updateUserPassword(user, newPassword);
		history.setChanges("password");
		userHistoryService.recordEdit(user, history, signinUrl);
	}

	private void updateUserPassword(DatarouterUser user, String password){
		String passwordSalt = PasswordTool.generateSalt();
		String passwordDigest = PasswordTool.digest(passwordSalt, password);
		user.setPasswordSalt(passwordSalt);
		user.setPasswordDigest(passwordDigest);
	}

	private static String change(String name, Object before, Object after){
		return name + ": " + before + " => " + after;
	}

}
