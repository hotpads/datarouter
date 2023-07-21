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
package io.datarouter.auth.config;

import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DefaultDatarouterUserPasswordSupplier;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.listener.DatarouterAppListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserConfigAppListener implements DatarouterAppListener{

	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private DatarouterUserCreationService datarouterUserCreationService;
	@Inject
	private DefaultDatarouterUserPasswordSupplier defaultDatarouterUserPassword;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	@Override
	public void onStartUp(){
		if(datarouterUserDao.hasAny()){
			return;
		}
		if(!serverTypeDetector.mightBeProduction()){
			datarouterUserCreationService.createFirstAdminUser(defaultDatarouterUserPassword.get());
		}
	}

}
