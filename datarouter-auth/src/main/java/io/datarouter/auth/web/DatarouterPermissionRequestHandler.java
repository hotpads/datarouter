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
package io.datarouter.auth.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.join;
import static j2html.TagCreator.p;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterUserEditService;
import io.datarouter.auth.service.DatarouterUserInfo;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.service.PermissionRequestUserInfo;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.storage.config.DatarouterSubscribersSupplier;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.config.service.ServiceName;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalLong;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.role.DatarouterUserRole;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class DatarouterPermissionRequestHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterPermissionRequestHandler.class);

	private static final String P_REASON = "reason";
	private static final String EMAIL_TITLE = "Permission Request";

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
	private DatarouterUserInfo datarouterUserInfo;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private AdminEmail adminEmail;
	@Inject
	private DatarouterSubscribersSupplier subscibersEmail;
	@Inject
	private DatarouterEmailSubscriberSettings subscribersSettings;
	@Inject
	private PermissionRequestUserInfo userInfo;


	@Handler(defaultHandler = true)
	public Mav showForm(OptionalString deniedUrl, OptionalString allowedRoles){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}

		DatarouterUser user = getCurrentUser();
		DatarouterPermissionRequest currentRequest = datarouterPermissionRequestDao
				.scanOpenPermissionRequestsForUser(user.getId())
				.findMax(Comparator.comparing(request -> request.getKey().getRequestTime()))
				.orElse(null);

		Optional<String> defaultSpecifics = deniedUrl.map(url -> {
			return "I tried to go to this URL: " + url + "." + allowedRoles
					.map(" These are its allowed roles at the time of this request: "::concat)
					.orElse("");
		});

		Set<String> emails = new HashSet<>();
		if(serverTypeDetector.mightBeProduction()){
			emails.addAll(permissionRequestEmailType.tos);
		}
		emails.add(adminEmail.get());
		if(subscribersSettings.includeSubscribers.get()){
			emails.addAll(subscibersEmail.get());
		}

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

		DivTag introContent = div()
				.with(p(join("Welcome to ", serviceName.get(), ". ",
						a("Sign out.").withHref(request.getContextPath() + "/" + paths.signout.getValue()))))
				.with(existingRequest)
				.condWith(currentRequest == null, p(join("If you need (additional) permissions to use ",
						serviceName.get(), ", submit the form below, and the administrator will follow up.")))
				.with(p(join("You will need to ", a("Sign out")
						.withHref(request.getContextPath() + "/" + paths.signout.getValue()),
						" and sign back in to refresh your permissions")))
				.with(p(join("If you have any questions, you may email the administrator(s) at ",
						a(String.join(",", emails)).withHref("mailto: " + String.join(",", emails)))));

		var form = new HtmlForm()
				.withAction("?" + BaseHandler.SUBMIT_ACTION + "=submit")
				.withMethod("post");
		form.addTextAreaField()
				.withDisplay(String.format("Why you want to access %s:", this.serviceName.get()))
				.withName("reason")
				.isRequired();
		form.addTextField()
				.withDisplay("Additional information we have detected: ")
				.withName("specifics")
				.withValue(defaultSpecifics.orElse(null))
				.withReadOnly(true);

		form.addButton()
				.withDisplay("Submit");
		DivTag formContent = div()
				.with(div(Bootstrap4FormHtml.render(form)))
				.withClasses("card card-body bg-light control-group");
		DivTag pageContent = div()
				.with(introContent)
				.with(formContent)
				.withClass("container-fluid");
		return bootstrap4PageFactory.startBuilder(request)
					.withTitle("Datarouter - Permission Request")
					.withContent(pageContent)
					.buildMav();
	}

	@Handler
	private Mav submit(OptionalString specifics){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}

		String reason = params.required(P_REASON);
		if(StringTool.isEmpty(reason)){
			throw new IllegalArgumentException("Reason is required.");
		}
		String specificString = specifics.orElse("");
		DatarouterUser user = getCurrentUser();

		datarouterPermissionRequestDao.createPermissionRequest(new DatarouterPermissionRequest(user.getId(), new Date(),
				"reason: " + reason + ", specifics: " + specificString, null, null));
		sendRequestEmail(user, reason, specificString);
		//not just requestor, so send them to the home page after they make their request
		if(user.getRoles().size() > 1){
			return new InContextRedirectMav(request, paths.home);
		}

		return showForm(new OptionalString(null), new OptionalString(null));
	}

	@Handler
	private Mav declineAll(OptionalLong userId, OptionalString redirectPath){
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new MessageMav(noDatarouterAuthentication());
		}
		DatarouterUser currentUser = getCurrentUser();
		//only allow DATAROUTER_ADMIN and self to decline requests
		if(!userId.orElse(currentUser.getId()).equals(currentUser.getId())
				&& !currentUser.getRoles().contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole())){
			return new MessageMav("You do not have permission to decline this request.");
		}
		datarouterPermissionRequestDao.declineAll(userId.orElse(currentUser.getId()));

		DatarouterUser editedUser = currentUser;
		if(!userId.orElse(currentUser.getId()).equals(getCurrentUser().getId())){
			editedUser = datarouterUserInfo.getUserById(userId.get(), true).get();
		}
		sendDeclineEmail(editedUser, currentUser);

		if(redirectPath.isEmpty()){
			if(currentUser.getRoles().size() > 1){
				return new InContextRedirectMav(request, paths.home);
			}
			return showForm(new OptionalString(null), new OptionalString(null));
		}
		return new GlobalRedirectMav(redirectPath.get());
	}

	//TODO (same time as DATAROUTER-2788) extract common code copied from Mav declineAll
	@Handler
	private SuccessAndMessageDto declinePermissionRequests(String userId){
		long userIdLong = Long.parseLong(userId);
		if(!authenticationConfig.useDatarouterAuthentication()){
			return new SuccessAndMessageDto(false, noDatarouterAuthentication());
		}
		DatarouterUser currentUser = getCurrentUser();
		//only allow DATAROUTER_ADMIN and self to decline requests
		if(userIdLong != currentUser.getId() && !currentUser.getRoles().contains(DatarouterUserRole.DATAROUTER_ADMIN
				.getRole())){
			return new SuccessAndMessageDto(false, "You do not have permission to decline this request.");
		}
		datarouterPermissionRequestDao.declineAll(userIdLong);

		DatarouterUser editedUser = currentUser;
		if(userIdLong != getCurrentUser().getId()){
			editedUser = datarouterUserInfo.getUserById(userIdLong, true).get();
		}
		sendDeclineEmail(editedUser, currentUser);
		return new SuccessAndMessageDto();
	}

	private DatarouterUser getCurrentUser(){
		return datarouterUserService.getAndValidateCurrentUser(getSessionInfo().getRequiredSession());
	}

	private void sendRequestEmail(DatarouterUser user, String reason, String specifics){
		String userEmail = user.getUsername();
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.admin.editUser.toSlashedString())
				.withParam("userId", user.getId() + "")
				.build();
		var table = table(tbody()
				.with(createLabelValueTr("Service", text(serviceName.get()))
				.with(userInfo.getUserInformation(user)))
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
				.to(permissionRequestEmailType, serverTypeDetector.mightBeProduction())
				.toSubscribers(serverTypeDetector.mightBeProduction())
				.toAdmin(serverTypeDetector.mightBeDevelopment());
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
		public final String requestText;
		public final String resolutionTime;
		public final String resolution;

		public PermissionRequestDto(Instant requestTime, String requestText, Optional<Instant> resolutionTime,
				String resolution, ZoneId zoneId){
			this.requestTime = ZonedDateFormatterTool.formatInstantWithZone(requestTime, zoneId);
			this.requestText = requestText;
			this.resolutionTime = resolutionTime
					.map(instant -> ZonedDateFormatterTool.formatInstantWithZone(instant, zoneId))
					.orElse(null);
			this.resolution = resolution;
		}

	}

	//TODO DATAROUTER-2788 refactor/remove this class
	private static class SuccessAndMessageDto{

		@SuppressWarnings("unused")
		public final Boolean success;
		@SuppressWarnings("unused")
		public final String message;

		protected SuccessAndMessageDto(){
			this.success = true;
			this.message = "";
		}

		protected SuccessAndMessageDto(boolean success, String message){
			this.success = success;
			this.message = Objects.requireNonNull(message);
		}
	}

}
