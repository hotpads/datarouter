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
package io.datarouter.auth.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.p;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;

import java.util.List;
import java.util.Set;

import io.datarouter.auth.link.EditUserLink;
import io.datarouter.auth.service.PermissionRequestUserInfo.PermissionRequestUserInfoSupplier;
import io.datarouter.auth.service.PermissionRequestUserInfo.UserInfo;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.link.DatarouterEmailLinkClient;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.instrumentation.relay.dto.RelayStartThreadRequestDto;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlCollectors;
import io.datarouter.instrumentation.relay.rml.RmlStyle;
import io.datarouter.relay.DatarouterRelaySender;
import io.datarouter.relay.DatarouterRelayTopics;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.config.setting.DatarouterEmailSubscriberSettings;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.util.string.StringTool;
import j2html.tags.DomContent;
import j2html.tags.specialized.TrTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterPermissionRequestEmailService{

	public static final String FROM_NAME = "Datarouter Auth";
	private static final String EMAIL_TITLE = "Permission Request";

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterUserHistoryService userHistoryService;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private DatarouterEmailSubscriberSettings subscribersSettings;
	@Inject
	private PermissionRequestUserInfoSupplier userInfoSupplier;
	@Inject
	private DatarouterRelayTopics relayTopics;
	@Inject
	private DatarouterRelaySender relaySender;
	@Inject
	private DatarouterEmailLinkClient linkClient;

	public void sendRequestEmail(
			DatarouterUser user,
			String reason,
			String specifics,
			Set<String> additionalRecipients){
		String userEmail = user.getUsername();
		String primaryHref = linkClient.toUrl(new EditUserLink().withUserId(user.getId()));
		List<UserInfo> userInfo = userInfoSupplier.get().getUserInformation(user);
		var table = table(tbody()
				.with(createLabelValueTr("Service", text(serviceName.get()))
						.with(userInfo.stream()
								.map(info -> createLabelValueTr(
										info.relation(),
										info.link()
												.<DomContent>map(link -> a(info.name()).withHref(link))
												.orElseGet(() -> text(info.name())),
										info.title()
												.map(title -> text(" - (%s)".formatted(title)))
												.orElse(text(""))))))
				.with(createLabelValueTr("Reason", text(reason)))
				.condWith(StringTool.notEmpty(specifics), createLabelValueTr("Specifics", text(specifics))))
				.withStyle("border-spacing: 0; white-space: pre-wrap;");
		var content = div(table, p(a("Edit user profile").withHref(primaryHref)));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userHistoryService.getPermissionRequestEmailSubject(user))
				.withTitle(EMAIL_TITLE)
				.withTitleHref(primaryHref)
				.withContent(content)
				.from(userEmail)
				.to(userEmail)
				.to(additionalRecipients, serverTypeDetector.mightBeProduction())
				.to(permissionRequestEmailType, serverTypeDetector.mightBeProduction())
				.toAdmin(serverTypeDetector.mightBeDevelopment());
		if(subscribersSettings.includeSubscribers.get()){
			emailBuilder.toSubscribers();
		}

		htmlEmailService.trySendJ2Html(emailBuilder);

		var doc = Rml.doc(
						Rml.heading(1, Rml.text(EMAIL_TITLE).link(primaryHref)),
						Rml.table(
										Rml.tableRow(
												Rml.tableCell(Rml.text("Service").strong()),
												Rml.tableCell(Rml.text(serviceName.get()))))
								.with(userInfo.stream()
										.map(info -> Rml.tableRow(
												Rml.tableCell(Rml.text(info.relation()).strong()),
												Rml.tableCell(Rml.text(info.name()))
														.condWith(
																info.title().isPresent(),
																() -> Rml.text(" (%s)"
																		.formatted(info.title().get()))))))
								.with(Rml.tableRow(
										Rml.tableCell(Rml.text("Reason").strong()),
										Rml.tableCell(Rml.text(reason))))
								.with(Rml.tableRow(
										Rml.tableCell(Rml.text("Specifics").strong()),
										Rml.tableCell(specifics.lines()
												.map(Rml::text)
												.collect(RmlCollectors.joining(Rml.hardBreak()))))))
				.withPadding(RmlStyle.padding(1));

		var request = new RelayStartThreadRequestDto(
				relayTopics.permissionRequest(),
				FROM_NAME,
				emailBuilder.getSubject(),
				doc);
		relaySender.startThread(request);
	}

	public static TrTag createLabelValueTr(String label, DomContent...values){
		return tr(td(b(label + ' ')).withStyle("text-align: right"), td().with(values).withStyle("padding-left: 8px"))
				.withStyle("vertical-align: top");
	}
}
