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
package io.datarouter.secret.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.handler.SecretHandlerOpRequestDto.SecretOpDto;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.user.session.service.Session;

/**
 * This allows all users to view the list of secrets on any environment, but allows no other operations on production.
 */
@Singleton
public class DefaultSecretsHandlerPermissions implements SecretHandlerPermissions{

	@Inject
	private ServerTypeDetector serverTypeDetector;

	@Override
	public boolean isAuthorized(Session session, SecretOpDto secretOp){
		if(SecretOpDto.LIST_ALL == secretOp){
			return true;
		}
		if(serverTypeDetector.mightBeProduction()){
			return false;
		}
		return true;
	}

}
