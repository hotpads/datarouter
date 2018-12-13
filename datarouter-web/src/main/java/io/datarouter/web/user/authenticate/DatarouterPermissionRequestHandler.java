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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.util.DatarouterEmailService;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.user.DatarouterPermissionRequestDao;
import io.datarouter.web.user.DatarouterUserDao;
import io.datarouter.web.user.DatarouterUserEditService;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.authenticate.saml.DatarouterSamlSettings;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.session.CurrentUserSessionInfo;

public class DatarouterPermissionRequestHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterPermissionRequestHandler.class);

	private static final String P_REASON = "reason";

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private CurrentUserSessionInfo currentUserSessionInfo;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterSamlSettings samlSettings;
	@Inject
	private DatarouterEmailService datarouterEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterWebFiles webFiles;
	@Inject
	private DatarouterUserEditService userEditService;

	@Handler(defaultHandler = true)
	protected Mav showForm(OptionalString deniedUrl){
		if(!authenticationConfig.useDatarouterAuthentication()){
			 return noDatarouterAuthenticationMav();
		}
		Mav mav = new Mav(webFiles.jsp.authentication.permissionRequestJsp);
		mav.put("appName", webappName.getName());
		mav.put("permissionRequestPath", authenticationConfig.getPermissionRequestPath());
		mav.put("defaultSpecifics", deniedUrl.map("I tried to go to this URL: "::concat));
		DatarouterUser user = getCurrentUser();
		mav.put("currentRequest", datarouterPermissionRequestDao.streamOpenPermissionRequestsForUser(user.getId())
				.max(Comparator.comparing(request -> request.getKey().getRequestTime()))
				.orElse(null));
		mav.put("email", samlSettings.permissionRequestEmail.get());
		return mav;
	}

	@Handler
	protected Mav request(OptionalString specifics){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return noDatarouterAuthenticationMav();
		}
		String reason = params.required(P_REASON);
		if(StringTool.isEmpty(reason)){
			throw new IllegalArgumentException("Reason is required.");
		}
		String specificString = specifics.orElse("");
		DatarouterUser user = getCurrentUser();

		datarouterPermissionRequestDao.createPermissionRequest(new DatarouterPermissionRequest(user.getId(), new Date(),
				"reason: " + reason + ", specifics: " + specificString, null, null));
		sendEmail(user, reason, specificString);

		//not just requestor, so send them to the home page after they make their request
		if(user.getRoles().size() > 1){
			return new InContextRedirectMav(request, authenticationConfig.getHomePath());
		}

		return showForm(new OptionalString(null));
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserDao.getAndValidateCurrentUser(params.getSession());
	}

	private void sendEmail(DatarouterUser user, String reason, String specifics){
		//cut off the path from the whole url, then add the context back
		String webappRequestUrlWithContext = StringTool.getStringBeforeLastOccurrence(request.getRequestURI(),
				request.getRequestURL().toString()) + request.getContextPath();
		String editUserUrl = webappRequestUrlWithContext + authenticationConfig.getEditUserPath() + "?userId=" + user
				.getId();
		//I don't think there's any way to build the cluster setting path robustly.
		String clusterSettingUrl = webappRequestUrlWithContext
				+ "/datarouter/settings?submitAction=browseSettings&name=" + samlSettings.getName();
		String userEmail = user.getUsername();
		String recipients = userEditService.getUserEditEmailRecipients(user);

		String subject = userEditService.getPermissionRequestEmailSubject(user, webappName.getName());
		StringBuilder body = new StringBuilder()
				.append("User ")
				.append(userEmail)
				.append(" requests elevated permissions.")
				.append("\nReason: ").append(reason);
		if(StringTool.notEmpty(specifics)){
			body.append("\nSpecific: ").append(specifics);
		}
		body.append("\nEdit here: ").append(editUserUrl)
				.append("\n\nThe email address these emails are sent to can be configured here: ")
				.append(clusterSettingUrl).append('.');

		datarouterEmailService.trySendEmail(userEmail, recipients, subject, body.toString());
	}

	private Mav noDatarouterAuthenticationMav(){
		logger.warn("{} went to non-DR permission request page.", currentUserSessionInfo.getUsername(request).get());
		return new MessageMav("This is only available when using datarouter authentication. Please email "
				+ datarouterProperties.getAdministratorEmail() + " for assistance.");
	}

}
