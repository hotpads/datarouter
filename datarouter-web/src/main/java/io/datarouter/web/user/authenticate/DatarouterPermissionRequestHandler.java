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
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.DatarouterEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalLong;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.user.BaseDatarouterPermissionRequestDao;
import io.datarouter.web.user.DatarouterUserEditService;
import io.datarouter.web.user.DatarouterUserService;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.role.DatarouterUserRole;
import io.datarouter.web.user.session.CurrentUserSessionInfo;
import io.datarouter.web.user.session.service.DatarouterUserInfo;

public class DatarouterPermissionRequestHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterPermissionRequestHandler.class);

	private static final String P_REASON = "reason";

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private BaseDatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private CurrentUserSessionInfo currentUserSessionInfo;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterEmailService datarouterEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterWebFiles webFiles;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private DatarouterUserEditService userEditService;
	@Inject
	private DatarouterUserInfo datarouterUserInfo;
	@Inject
	private DatarouterAdministratorEmailService administratorEmailService;
	@Inject
	private DatarouterUserExternalDetailService userExternalDetailService;
	@Inject
	private PermissionRequestAdditionalEmails permissionRequestAdditionalEmails;

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
		mav.put("currentRequest", datarouterPermissionRequestDao.scanOpenPermissionRequestsForUser(user.getId())
				.max(Comparator.comparing(request -> request.getKey().getRequestTime()))
				.orElse(null));
		Set<String> additionalPermissionEmails = permissionRequestAdditionalEmails.get();
		mav.put("email", administratorEmailService.getAdministratorEmailAddressesCsv(additionalPermissionEmails));
		mav.put("submitPath", paths.permissionRequest.submit.toSlashedStringWithoutLeadingSlash());
		mav.put("declinePath", paths.permissionRequest.declineAll.toSlashedStringWithoutLeadingSlash());
		return mav;
	}

	@Handler
	protected Mav submit(OptionalString specifics){
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

	@Handler
	protected Mav declineAll(OptionalLong userId, OptionalString redirectPath){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return noDatarouterAuthenticationMav();
		}
		DatarouterUser currentUser = getCurrentUser();
		//only allow DATAROUTER_ADMIN and self to decline requests
		if(!userId.orElse(currentUser.getId()).equals(currentUser.getId()) && !currentUser.getRoles().contains(
				DatarouterUserRole.DATAROUTER_ADMIN.getRole())){
			return new MessageMav("You do not have permission to decline this request.");
		}
		datarouterPermissionRequestDao.declineAll(userId.orElse(currentUser.getId()));

		DatarouterUser editedUser = currentUser;
		if(!userId.orElse(currentUser.getId()).equals(getCurrentUser().getId())){
			editedUser = datarouterUserInfo.getUserById(userId.get()).get();
		}
		String body = "Permission requests declined for user " + editedUser.getUsername() + " by user " + currentUser
				.getUsername();
		datarouterEmailService.trySendEmail(editedUser.getUsername(),
				userEditService.getUserEditEmailRecipients(editedUser),
				userEditService.getPermissionRequestEmailSubject(editedUser, webappName.getName()),
				body);

		if(redirectPath.isEmpty()){
			if(currentUser.getRoles().size() > 1){
				return new InContextRedirectMav(request, authenticationConfig.getHomePath());
			}
			return showForm(new OptionalString(null));
		}
		return new GlobalRedirectMav(redirectPath.get());
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(params.getSession());
	}

	private void sendEmail(DatarouterUser user, String reason, String specifics){
		//cut off the path from the whole url, then add the context back
		String webappRequestUrlWithContext = StringTool.getStringBeforeLastOccurrence(request.getRequestURI(),
				request.getRequestURL().toString()) + request.getContextPath();
		String editUserUrl = webappRequestUrlWithContext + authenticationConfig.getEditUserPath() + "?userId=" + user
				.getId();
		String userEmail = user.getUsername();
		String recipients = userEditService.getUserEditEmailRecipients(user);

		String subject = userEditService.getPermissionRequestEmailSubject(user, webappName.getName());
		StringBuilder body = new StringBuilder()
				.append("User ")
				.append(userEmail)
				.append(" requests elevated permissions.");
		userExternalDetailService.getUserProfileUrl(user)
				.ifPresent(url -> body.append("\nUser Profile: ").append(url));
		body.append("\nReason: ").append(reason);
		if(StringTool.notEmpty(specifics)){
			body.append("\nSpecific: ").append(specifics);
		}
		body.append("\nEdit here: ").append(editUserUrl);

		datarouterEmailService.trySendEmail(userEmail, recipients, subject, body.toString());
	}

	private Mav noDatarouterAuthenticationMav(){
		logger.warn("{} went to non-DR permission request page.", currentUserSessionInfo.getRequiredSession(request)
				.getUsername());
		return new MessageMav("This is only available when using datarouter authentication. Please email "
				+ datarouterProperties.getAdministratorEmail() + " for assistance.");
	}

}
