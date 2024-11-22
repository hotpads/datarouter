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
package io.datarouter.auth.web.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.join;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.script;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.auth.web.service.DatarouterPermissionRequestEmailService;
import io.datarouter.auth.web.service.PermissionRequestService;
import io.datarouter.auth.web.service.PermissionRequestService.DeclinePermissionRequestDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.MilliTime;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormCheckboxTable;
import io.datarouter.web.html.form.HtmlFormCheckboxTable.Column;
import io.datarouter.web.html.form.HtmlFormCheckboxTable.Row;
import io.datarouter.web.html.form.HtmlFormTimezoneSelect;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterPermissionRequestHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterPermissionRequestHandler.class);

	private static final String P_REASON = "reason";
	private static final String P_REQUESTED_ROLES = "requestedRoles";
	private static final String P_DENIED_URL = "deniedUrl";
	private static final String P_ALLOWED_ROLES = "allowedRoles";
	private static final String P_SPECIFICS = "specifics";
	private static final String P_VALIDATION_ERROR = "validationError";
	private static final String EMAIL_TITLE = "Permission Request";
	private static final String FORM_ID = "permissionRequestForm";
	private static final String ROLE_TABLE_ID = "roleTable";

	@Inject
	private Bootstrap4PageFactory bootstrap4PageFactory;
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private ServiceName serviceName;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private RoleManager roleManager;
	@Inject
	private PermissionRequestService permissionRequestService;
	@Inject
	private DatarouterPermissionRequestEmailService permissionRequestEmailService;

	@Handler(defaultHandler = true)
	public Mav showForm(
			Optional<String> deniedUrl,
			Optional<String> allowedRoles,
			@Param(P_VALIDATION_ERROR)
			Optional<String> validationError){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}

		DatarouterUser user = getCurrentUser();
		PermissionRequest currentRequest = datarouterPermissionRequestDao
				.scanOpenPermissionRequestsForUser(user.getId())
				.findMax(Comparator.comparing(request -> request.getKey().getRequestTime()))
				.orElse(null);

		String declinePath = paths.permissionRequest.declineAll.join("/");

		DivTag existingRequest = new DivTag();
		if(currentRequest != null){
			existingRequest = div(
					p("You already have an open permission request for " + serviceName.get()
						+ ". You may submit another request to replace it."),
					p("Time Requested: " + currentRequest.getKey().getRequestTime()),
					p(b("Request Text:\n")),
					// pre-wrap doesn't work with j2html.ContainerTag.renderFormatted, so use pre tag instead
					pre(currentRequest.getRequestText()).withStyle("margin-left: 2em;"),
					p(join("Click ", a("here").withHref(declinePath), " to decline it.")));
		}
		DivTag insufficientPermissionAction = new DivTag();
		if(deniedUrl.isPresent() && allowedRoles.isPresent()){
			insufficientPermissionAction = div(p(join(
					"You made a request to: %s. This action requires one of these roles: ".formatted(deniedUrl.get()),
					b(allowedRoles.get() + "."))));
		}

		DivTag introContent = div()
				.with(h1("Permission Request: " + serviceName.get()))
				.with(existingRequest)
				.with(insufficientPermissionAction)
				.with(p(b("Request the least amount of permissions necessary for your role.")));

		String userTimezone = user.getZoneId()
				.map(ZoneId::getId)
				.orElse(null);

		var form = new HtmlForm(HtmlFormMethod.POST)
				.withId(FORM_ID)
				.withAction("?" + BaseHandler.SUBMIT_ACTION + "=submit");
		form.addTextAreaField()
				.withLabel(
						String.format("Why your role necessitates these permissions in %s:", this.serviceName.get()))
				.withName(P_REASON)
				.withPlaceholder("explain reason here")
				.required();

		HtmlFormCheckboxTable roleTable = form.addCheckboxTableField()
				.withId("roleTable")
				.withLabel("Available Roles to Request")
				.withColumns(List.of(new Column("role", "Role"), new Column("description", "Description")))
				.withRows(Scanner.of(roleManager.getRequestableRoles(user))
						.map(role -> {
							boolean userHasRole = user.getRolesIgnoreSaml().contains(role);
							return new Row(
									role.persistentString(),
									List.of(role.persistentString(), role.description()),
									userHasRole,
									userHasRole);
						})
						.sort(Comparator.comparing(Row::name))
						.list())
				.required();
		form.addHiddenField(P_REQUESTED_ROLES, "");
		form.addHiddenField(P_DENIED_URL, deniedUrl.orElse(null));
		form.addHiddenField(P_ALLOWED_ROLES, allowedRoles.orElse(null));
		form.addHiddenField(HtmlFormTimezoneSelect.TIMEZONE_FIELD_NAME, userTimezone);
		form.addButton()
				.withLabel("Submit");
		DivTag formContent = div();
		if(validationError.isPresent()){
			formContent = formContent.with(div(validationError.get())
					.withClass("alert alert-danger")
					.attr("role", "alert"));
		}
		formContent = formContent
				.with(div(Bootstrap4FormHtml.render(form)))
				.withClasses("card card-body bg-light control-group");
		DivTag pageContent = div()
				.with(introContent)
				.with(formContent)
				.withClass("container-fluid");
		return bootstrap4PageFactory.startBuilder(request)
				.withTitle("Datarouter - Permission Request")
				.withContent(pageContent)
				.withScript(script(HtmlFormTimezoneSelect.HIDDEN_TIMEZONE_JS))
				.withScript(script(
						roleTable.getCollectValuesJs(FORM_ID, ROLE_TABLE_ID, P_REQUESTED_ROLES)))
				.buildMav();
	}

	@Handler
	public String getUserTimezone(){
		DatarouterUser user = getCurrentUser();
		return user.getZoneId()
				.map(ZoneId::getId)
				.orElse(null);
	}

	@Handler
	public void setTimezone(String timezone){
		DatarouterUser user = getCurrentUser();
		user.setZoneId(ZoneId.of(timezone));
		datarouterUserDao.put(user);
	}

	@Handler
	private Mav submit(
			@Param(P_REASON) String reason,
			@Param(P_REQUESTED_ROLES) String requestedRoleString,
			@Param(P_DENIED_URL) Optional<String> deniedUrl,
			@Param(P_ALLOWED_ROLES) Optional<String> allowedRoles,
			@Param(HtmlFormTimezoneSelect.TIMEZONE_FIELD_NAME) Optional<String> timezone){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}

		if(StringTool.isEmptyOrWhitespace(reason)){
			return new InContextRedirectMav(
					request,
					paths.permissionRequest,
					Map.of(P_VALIDATION_ERROR, "Reason is required."));
		}
		reason = reason.trim();
		if(StringTool.isEmpty(requestedRoleString)){
			return new InContextRedirectMav(
					request,
					paths.permissionRequest,
					Map.of(P_VALIDATION_ERROR, "At least one requested role is required."));
		}
		String specifics = "Request Reason: \"%s\"\nRequested Roles: %s.".formatted(reason, requestedRoleString)
				+ deniedUrl.map(url -> "\nAttempted request to: " + url + ".").orElse("")
				+ allowedRoles.map(roles -> "\nAllowed Roles: " + roles + ".").orElse("");
		DatarouterUser user = getCurrentUser();

		timezone.map(ZoneId::of)
				.ifPresent(zoneId -> {
					user.setZoneId(zoneId);
					datarouterUserDao.put(user);
				});

		datarouterPermissionRequestDao.createPermissionRequest(new PermissionRequest(
				user.getId(),
				MilliTime.now(),
				specifics,
				null,
				null));
		Set<Role> requestedRoles = new HashSet<>(Scanner.of(requestedRoleString.split(","))
				.map(roleManager::findRoleFromPersistentString)
				.map(optionalRole -> optionalRole.orElseThrow(
						() -> new IllegalArgumentException(
								"Permission request made with unknown role(s): " + requestedRoleString)))
				.list());
		Set<String> additionalRecipients = roleManager.getAdditionalPermissionRequestEmailRecipients(user,
				requestedRoles);
		permissionRequestEmailService.sendRequestEmail(user, reason, specifics, EMAIL_TITLE, additionalRecipients);

		//not just requestor, so send them to the home page after they make their request
		if(datarouterUserService.getUserRolesWithSamlGroups(user).size() > 1){
			return new InContextRedirectMav(request, paths.home);
		}

		return new InContextRedirectMav(request, paths.permissionRequest);
	}

	@Handler
	private Mav createCustomPermissionRequest(
			@Param(P_REASON) String reason,
			@Param(P_SPECIFICS) String specifics){
		if(StringTool.isEmptyOrWhitespace(reason)){
			return new InContextRedirectMav(
					request,
					paths.permissionRequest,
					Map.of(P_VALIDATION_ERROR, "Reason is required."));
		}
		if(StringTool.isEmptyOrWhitespace(specifics)){
			return new InContextRedirectMav(
					request,
					paths.permissionRequest,
					Map.of(P_VALIDATION_ERROR, "Specifics are required."));
		}
		reason = reason.trim();
		specifics = specifics.trim();
		DatarouterUser user = getCurrentUser();
		datarouterPermissionRequestDao.createPermissionRequest(new PermissionRequest(
				user.getId(),
				MilliTime.now(),
				specifics,
				null,
				null));
		permissionRequestEmailService.sendRequestEmail(user, reason, specifics, EMAIL_TITLE, Set.of());

		//not just requestor, so send them to the home page after they make their request
		if(datarouterUserService.getUserRolesWithSamlGroups(user).size() > 1){
			return new InContextRedirectMav(request, paths.home);
		}

		return new InContextRedirectMav(request, paths.permissionRequest);
	}

	@Handler
	private Mav declineAll(Optional<Long> userId, Optional<String> redirectPath){
		DeclinePermissionRequestDto dto = declinePermissionRequests(userId.orElse(getCurrentUser().getId()).toString());
		if(!dto.success()){
			return new MessageMav(dto.message());
		}

		if(redirectPath.isEmpty()){
			if(datarouterUserService.getUserRolesWithSamlGroups(getCurrentUser()).size() > 1){
				return new InContextRedirectMav(request, paths.home);
			}
			return new InContextRedirectMav(request, paths.permissionRequest);
		}
		return new GlobalRedirectMav(redirectPath.get());
	}

	@Handler
	private DeclinePermissionRequestDto declinePermissionRequests(String userId){
		long userIdLong = Long.parseLong(userId);
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new DeclinePermissionRequestDto(false, noDatarouterAuthentication());
		}
		DatarouterUser editor = getCurrentUser();
		DatarouterUser editedUser = userIdLong == editor.getId() ? editor
				: datarouterUserService.getUserById(userIdLong, true);
		return permissionRequestService.declinePermissionRequests(editedUser, editor);
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
	}

	private String noDatarouterAuthentication(){
		logger.warn("{} went to non-DR permission request page.", getSessionInfo().getRequiredSession().getUsername());
		return "This is only available when using datarouter authentication. Please email " + adminEmail.get()
				+ " for assistance.";
	}

}
