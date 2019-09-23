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
package io.datarouter.web.config;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.autoconfig.ConfigScanDto;
import io.datarouter.instrumentation.autoconfig.ConfigScanResponseTool;
import io.datarouter.web.user.DatarouterUserCreationService;
import io.datarouter.web.user.DatarouterUserNodes;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserKey;

@Singleton
public class DatarouterWebConfigScanner{

	@Inject
	private DatarouterUserNodes datarouterUserNodes;

	public ConfigScanDto checkForDefaultUserId(){
		Long defaultAdminId = DatarouterUserCreationService.ADMIN_ID;
		Optional<DatarouterUser> defaultUser = datarouterUserNodes.getUserNode()
				.find(new DatarouterUserKey(defaultAdminId));
		if(defaultUser.isEmpty()){
			return ConfigScanResponseTool.buildEmptyResponse();
		}
		String userName = defaultUser.get().getUsername();
		return ConfigScanResponseTool.buildResponse("Found a user with the default admin id=" + defaultAdminId
				+ " and username=" + userName);
	}

}
