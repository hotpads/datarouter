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
package io.datarouter.auth.web.deprovisioning;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.datarouter.auth.config.DatarouterAuthFiles;
import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.UserDeprovisioningService;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUser;
import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUserDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;

public class UserDeprovisioningHandler extends BaseHandler{

	private static final int DB_BATCH_SIZE = 1000;

	@Inject
	private Bootstrap4ReactPageFactory reactPageFactory;
	@Inject
	private DatarouterAuthFiles files;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DeprovisionedUserDao deprovisionedUserDao;
	@Inject
	private UserDeprovisioningService userDeprovisioningService;

	@Handler(defaultHandler = true)
	private Mav deprovisionedUsers(){
		return reactPageFactory.startBuilder(request)
				.withTitle("Datarouter - Deprovisioned Users")
				.withReactScript(files.js.deprovisionedUsersJsx)
				.withJsStringConstant("PATH", request.getContextPath() + paths.userDeprovisioning.toSlashedString())
				.buildMav();
	}

	//this endpoint has no batching because of its UI
	@Handler
	protected UserDeprovisioningHandlerGeneralDto fetchDeprovisionedUsers(){
		return UserDeprovisioningHandlerGeneralDto.fetchDeprovisionedUsers(deprovisionedUserDao.scan()
				.map(DeprovisionedUser::toDto)
				.sorted(DeprovisionedUserDto.COMPARATOR)
				.list());
	}

	@Handler
	protected void deprovisionUsers(@RequestBody UserDeprovisioningHandlerGeneralDto request){
		Scanner.of(Objects.requireNonNull(request.usernamesToDeprovision))
				.batch(DB_BATCH_SIZE)
				.forEach(userDeprovisioningService::deprovisionUsers);
	}

	@Handler
	protected UserDeprovisioningHandlerGeneralDto restoreUsers(
			@RequestBody UserDeprovisioningHandlerGeneralDto request){
		return Scanner.of(Objects.requireNonNull(request.usernamesToRestore))
				.batch(DB_BATCH_SIZE)
				.map(userDeprovisioningService::restoreDeprovisionedUsers)
				.concatenate(Scanner::of)
				.listTo(UserDeprovisioningHandlerGeneralDto::restoreUsersResponse);
	}

	public static class UserDeprovisioningHandlerGeneralDto{

		//deprovisionUsers
		public final List<String> usernamesToDeprovision;

		//restoreUsers
		public final List<String> usernamesToRestore;
		public final List<String> restoredUsernames;

		//fetchDeprovisionedUsers
		public final List<DeprovisionedUserDto> deprovisionedUsers;

		private UserDeprovisioningHandlerGeneralDto(List<String> usernamesToDeprovision,
				List<String> usernamesToRestore, List<String> restoredUsernames,
				List<DeprovisionedUserDto> deprovisionedUsers){
			this.usernamesToDeprovision = usernamesToDeprovision;
			this.usernamesToRestore = usernamesToRestore;
			this.restoredUsernames = restoredUsernames;
			this.deprovisionedUsers = deprovisionedUsers;
		}

		public static UserDeprovisioningHandlerGeneralDto restoreUsersResponse(List<String> restoredUsernames){
			return new UserDeprovisioningHandlerGeneralDto(null, null, restoredUsernames, null);
		}

		public static UserDeprovisioningHandlerGeneralDto fetchDeprovisionedUsers(
				List<DeprovisionedUserDto> deprovisionedUsers){
			return new UserDeprovisioningHandlerGeneralDto(null, null, null, deprovisionedUsers);
		}

	}

}
