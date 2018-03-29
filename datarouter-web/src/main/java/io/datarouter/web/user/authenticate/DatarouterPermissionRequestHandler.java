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
package io.datarouter.web.user.authenticate;

import java.util.Comparator;
import java.util.Date;

import javax.inject.Inject;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.util.DatarouterEmailTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.app.WebAppName;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.user.DatarouterPermissionRequestDao;
import io.datarouter.web.user.DatarouterUserDao;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.SamlSettings;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterUser;

public class DatarouterPermissionRequestHandler extends BaseHandler{
	private static final String JSP = "/jsp/authentication/permissionRequest.jsp";
	private static final String P_REASON = "reason";

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private WebAppName webAppName;
	@Inject
	private SamlSettings samlSettings;
	@Inject
	private DatarouterProperties datarouterProperties;

	@Handler(defaultHandler = true)
	protected Mav showForm(){
		Mav mav = new Mav(JSP);
		mav.put("appName", webAppName.getName());
		mav.put("permissionRequestPath", authenticationConfig.getPermissionRequestPath());
		DatarouterUser user = getCurrentUser();
		mav.put("currentRequest", datarouterPermissionRequestDao.streamOpenPermissionRequestsForUser(user.getId())
				.max(Comparator.comparing(request -> request.getKey().getRequestTime()))
				.orElse(null));
		mav.put("email", samlSettings.permissionRequestEmail.getValue());
		return mav;
	}

	@Handler
	protected Mav request(OptionalString specifics){
		String reason = params.required(P_REASON);
		if(StringTool.isEmpty(reason)){
			throw new IllegalArgumentException("Reason is required.");
		}
		String specificString = specifics.orElse("");
		DatarouterUser user = getCurrentUser();

		datarouterPermissionRequestDao.createPermissionRequest(new DatarouterPermissionRequest(user.getId(), new Date(),
				"reason: " + reason + ", specifics: " + specificString, null, null));
		sendEmail(user, reason, specificString);

		return showForm();
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserDao.getAndValidateCurrentUser(params.getSession());
	}

	private void sendEmail(DatarouterUser user, String reason, String specifics){
		//cut off the path from the whole url, then add the context back
		String webAppRequestUrlWithContext = StringTool.getStringBeforeLastOccurrence(request.getRequestURI(), request
				.getRequestURL().toString()) + request.getContextPath();
		String editUserUrl = webAppRequestUrlWithContext + authenticationConfig.getEditUserPath() + "?userId=" + user
				.getId();
		//I don't think there's any way to build the cluster setting path robustly.
		String clusterSettingUrl = webAppRequestUrlWithContext
				+ "/datarouter/settings?submitAction=browseSettings&name=" + samlSettings.getName();

		StringBuilder body = new StringBuilder()
				.append("User ")
				.append(user.getUsername())
				.append(" requests elevated permissions.")
				.append("\nReason: ").append(reason);
		if(StringTool.notEmpty(specifics)){
			body.append("\nSpecific: ").append(specifics);
		}
		body.append("\nEdit here: ").append(editUserUrl)
				.append("\n\nThe email address these emails are sent to can be configured here: ")
				.append(clusterSettingUrl).append('.');

		DatarouterEmailTool.trySendEmail(datarouterProperties.getAdministratorEmail(), samlSettings
				.permissionRequestEmail.getValue(), "User permissions request for " + webAppName.getName(), body
				.toString());
	}
}
