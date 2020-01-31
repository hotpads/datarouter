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
package io.datarouter.auth.web;

import javax.inject.Inject;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import io.datarouter.web.user.authenticate.saml.SamlService;
import io.datarouter.web.user.session.DatarouterSessionManager;

public class DatarouterSignoutHandler extends BaseHandler{

	@Inject
	private DatarouterSessionManager datarouterSessionManager;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DatarouterSamlSettings samlSettings;
	@Inject
	private SamlService samlService;

	@Handler(defaultHandler = true)
	private Mav signout(){
		if(samlSettings.getShouldProcess()){//redirect to IDP, since pages here should require SAML
			return samlService.mavSignout(response);
		}
		datarouterSessionManager.clearUserTokenCookie(response);
		datarouterSessionManager.clearSessionTokenCookie(response);
		return new InContextRedirectMav(request, paths.signin);
	}

}
