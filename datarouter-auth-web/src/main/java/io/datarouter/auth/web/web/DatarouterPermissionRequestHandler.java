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
import static j2html.TagCreator.script;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.web.config.DatarouterAuthPaths;
import io.datarouter.auth.web.service.DatarouterUserEditService;
import io.datarouter.auth.web.service.PermissionRequestUserInfo.PermissionRequestUserInfoSupplier;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.email.DatarouterHtmlEmailService;
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
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;
import jakarta.inject.Inject;

public class DatarouterPermissionRequestHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterPermissionRequestHandler.class);

	private static final String P_REASON = "reason";
	private static final String P_REQUESTED_ROLES = "requestedRoles";
	private static final String P_DENIED_URL = "deniedUrl";
	private static final String P_ALLOWED_ROLES = "allowedRoles";
	private static final String P_SPECIFICS = "specifics";
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
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DatarouterUserEditService userEditService;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterEmailSubscriberSettings subscribersSettings;
	@Inject
	private PermissionRequestUserInfoSupplier userInfoSupplier;
	@Inject
	private DatarouterUserDao datarouterUserDao;
	@Inject
	private RoleManager roleManager;

	@Handler(defaultHandler = true)
	public Mav showForm(Optional<String> deniedUrl, Optional<String> allowedRoles, Optional<String> validationError){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}

		DatarouterUser user = getCurrentUser();
		DatarouterPermissionRequest currentRequest = datarouterPermissionRequestDao
				.scanOpenPermissionRequestsForUser(user.getId())
				.findMax(Comparator.comparing(request -> request.getKey().getRequestTime()))
				.orElse(null);

		Optional<String> defaultSpecifics = deniedUrl
				.map(url -> "Attempted request to: " + url + "." + allowedRoles
						.map(" These are its allowed roles at the time of this request: "::concat)
						.orElse(""));

		String declinePath = paths.permissionRequest.declineAll.join("/");

		DivTag existingRequest = new DivTag();
		if(currentRequest != null){
			existingRequest = div(
					p("You already have an open permission request for " + serviceName.get()
						+ ". You may submit another request to replace it."),
					p("Time Requested: " + currentRequest.getKey().getRequestTime()),
					p("Request Text: " + currentRequest.getRequestText()),
					p(join("Click ", a("here").withHref(declinePath), " to decline it.")));
		}
		DivTag insufficientPermissionAction = new DivTag();
		if(deniedUrl.isPresent() && allowedRoles.isPresent()){
			insufficientPermissionAction = div(
					p("You made a request to: %s. This action requires one of these roles: %s."
							.formatted(deniedUrl.get(), allowedRoles.get())));
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
				.withRows(Scanner.of(roleManager.getAllRoles())
						.map(role -> {
							boolean userHasRole = user.getRolesIgnoreSaml().contains(role);
							return new Row(
									role.getPersistentString(),
									List.of(role.getPersistentString(), role.getDescription()),
									userHasRole,
									userHasRole);
						})
						.list())
				.required();
		form.addHiddenField(P_REQUESTED_ROLES, "");
		form.addHiddenField(P_SPECIFICS, defaultSpecifics.orElse(null));
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
			@Param(HtmlFormTimezoneSelect.TIMEZONE_FIELD_NAME) Optional<String> timezone,
			@Param(P_SPECIFICS) Optional<String> specifics){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}

		if(StringTool.isEmpty(reason)){
			return showForm(
					deniedUrl,
					allowedRoles,
					Optional.of("Reason is required."));
		}
		if(StringTool.isEmpty(requestedRoleString)){
			return showForm(
					deniedUrl,
					allowedRoles,
					Optional.of("At least one requested role is required."));
		}
		String specificString = specifics
				.map(str -> str + " ")
				.orElse("")
				.concat("Requested Roles: " + requestedRoleString);
		DatarouterUser user = getCurrentUser();

		timezone.map(ZoneId::of)
				.ifPresent(zoneId -> {
					user.setZoneId(zoneId);
					datarouterUserDao.put(user);
				});

		datarouterPermissionRequestDao.createPermissionRequest(new DatarouterPermissionRequest(user.getId(), new Date(),
				"reason: " + reason + ", specifics: " + specificString, null, null));
		Set<Role> requestedRoles = new HashSet<>(Scanner.of(requestedRoleString.split(","))
				.map(roleManager::findRoleFromPersistentString)
				.map(optionalRole -> optionalRole.orElseThrow(
						() -> new IllegalArgumentException(
								"Permission request made with unknown role(s): " + requestedRoleString)))
				.list());
		Set<String> additionalRecipients = roleManager.getAdditionalPermissionRequestEmailRecipients(user,
				requestedRoles);
		sendRequestEmail(user, reason, specificString, additionalRecipients);

		//not just requestor, so send them to the home page after they make their request
		if(datarouterUserService.getUserRolesWithSamlGroups(user).size() > 1){
			return new InContextRedirectMav(request, paths.home);
		}

		return showForm(Optional.empty(), Optional.empty(), Optional.empty());
	}

	@Handler
	private Mav declineAll(Optional<Long> userId, Optional<String> redirectPath){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}
		DatarouterUser currentUser = getCurrentUser();
		//only allow DATAROUTER_ADMIN and self to decline requests
		if(!userId.orElse(currentUser.getId()).equals(currentUser.getId())
				&& !datarouterUserService.isDatarouterAdmin(currentUser)){
			return new MessageMav("You do not have permission to decline this request.");
		}
		datarouterPermissionRequestDao.declineAll(userId.orElse(currentUser.getId()));

		DatarouterUser editedUser = currentUser;
		if(!userId.orElse(currentUser.getId()).equals(getCurrentUser().getId())){
			editedUser = datarouterUserService.findUserById(userId.get(), true).get();
		}
		sendDeclineEmail(editedUser, currentUser);

		if(redirectPath.isEmpty()){
			if(datarouterUserService.getUserRolesWithSamlGroups(currentUser).size() > 1){
				return new InContextRedirectMav(request, paths.home);
			}
			return showForm(Optional.empty(), Optional.empty(), Optional.empty());
		}
		return new GlobalRedirectMav(redirectPath.get());
	}

	//TODO (same time as DATAROUTER-2788) extract common code copied from Mav declineAll
	@Handler
	private DeclinePermissionRequestDto declinePermissionRequests(String userId){
		long userIdLong = Long.parseLong(userId);
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new DeclinePermissionRequestDto(false, noDatarouterAuthentication());
		}
		DatarouterUser currentUser = getCurrentUser();
		//only allow DATAROUTER_ADMIN and self to decline requests
		if(userIdLong != currentUser.getId() && !datarouterUserService.isDatarouterAdmin(currentUser)){
			return new DeclinePermissionRequestDto(false, "You do not have permission to decline this request.");
		}
		datarouterPermissionRequestDao.declineAll(userIdLong);

		DatarouterUser editedUser = currentUser;
		if(userIdLong != getCurrentUser().getId()){
			editedUser = datarouterUserService.getUserById(userIdLong, true);
		}
		sendDeclineEmail(editedUser, currentUser);
		return new DeclinePermissionRequestDto(true, null);
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
	}

	private void sendRequestEmail(
			DatarouterUser user,
			String reason,
			String specifics,
			Set<String> additionalRecipients){
		String userEmail = user.getUsername();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.admin.editUser.toSlashedString())
				.withParam("userId", user.getId() + "")
				.build();
		var table = table(tbody()
				.with(createLabelValueTr("Service", text(serviceName.get()))
				.with(userInfoSupplier.get().getUserInformation(user)))
				.with(createLabelValueTr("Reason", text(reason)))
				.condWith(StringTool.notEmpty(specifics), createLabelValueTr("Specifics", text(specifics))))
				.withStyle("border-spacing: 0");
		var content = div(table, p(a("Edit user profile").withHref(primaryHref)));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userEditService.getPermissionRequestEmailSubject(user))
				.withTitle(EMAIL_TITLE)
				.withTitleHref(primaryHref)
				.withContent(content)
				.from(userEmail)
				.to(userEmail)
				.to(additionalRecipients)
				.to(permissionRequestEmailType, serverTypeDetector.mightBeProduction())
				.toAdmin(serverTypeDetector.mightBeDevelopment());
		if(subscribersSettings.includeSubscribers.get()){
			emailBuilder.toSubscribers();
		}
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private void sendDeclineEmail(DatarouterUser editedUser, DatarouterUser currentUser){
		String titleHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.admin.editUser.toSlashedString())
				.withParam("userId", editedUser.getId() + "")
				.build();
		String message = String.format("Permission requests declined for user %s by user %s",
				editedUser.getUsername(),
				currentUser.getUsername());
		var content = p(message);
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userEditService.getPermissionRequestEmailSubject(editedUser))
				.withTitle(EMAIL_TITLE)
				.withTitleHref(titleHref)
				.withContent(content)
				.from(editedUser.getUsername())
				.to(editedUser.getUsername())
				.to(permissionRequestEmailType, serverTypeDetector.mightBeProduction())
				.toSubscribers(serverTypeDetector.mightBeProduction())
				.toAdmin(serverTypeDetector.mightBeDevelopment());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	public static TrTag createLabelValueTr(String label, DomContent...values){
		return tr(td(b(label + ' ')).withStyle("text-align: right"), td().with(values).withStyle("padding-left: 8px"))
				.withStyle("vertical-align: top");
	}

	private String noDatarouterAuthentication(){
		logger.warn("{} went to non-DR permission request page.", getSessionInfo().getRequiredSession().getUsername());
		return "This is only available when using datarouter authentication. Please email " + adminEmail.get()
				+ " for assistance.";
	}

	public static class PermissionRequestDto{
		public final String requestTime;
		public final Long requestTimeMs;
		public final String requestText;
		public final String resolutionTime;
		public final Long resolutionTimeMs;
		public final String resolution;
		public final String editor;

		public PermissionRequestDto(Instant requestTime, String requestText, Optional<Instant> resolutionTime,
				String resolution, ZoneId zoneId, String editor){
			this.requestTime = ZonedDateFormatterTool.formatInstantWithZone(requestTime, zoneId);
			this.requestTimeMs = requestTime.toEpochMilli();
			this.requestText = requestText;
			this.resolutionTime = resolutionTime
					.map(instant -> ZonedDateFormatterTool.formatInstantWithZone(instant, zoneId))
					.orElse(null);
			this.resolutionTimeMs = resolutionTime
					.map(Instant::toEpochMilli)
					.orElse(null);
			this.resolution = resolution;
			this.editor = editor;
		}

	}

	//TODO DATAROUTER-2788 refactor/remove this class
	protected record DeclinePermissionRequestDto(
			boolean success,
			String message){
	}

}
