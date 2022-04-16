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
package io.datarouter.aws.rds.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.i;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.aws.rds.config.DatarouterAwsPaths;
import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.service.AuroraClientIdProvider;
import io.datarouter.aws.rds.service.AuroraClientIdProvider.AuroraClientDto;
import io.datarouter.aws.rds.service.AuroraDnsService;
import io.datarouter.aws.rds.service.AuroraDnsService.DnsHostEntryDto;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.aws.rds.service.RdsService;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.util.Require;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class AuroraInstancesHandler extends BaseHandler{

	private static final String P_clientName = "clientName";

	@Inject
	private AuroraDnsService dnsService;
	@Inject
	private DatarouterAwsPaths paths;
	@Inject
	private RdsService rdsService;
	@Inject
	private AuroraClientIdProvider auroraClientIdProvider;
	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatabaseAdministrationConfiguration config;
	@Inject
	private ChangelogRecorder changelogRecorder;

	@Handler
	public Mav inspectClientUrl(){
		List<DnsHostEntryDto> otherReaderInstances = new ArrayList<>();
		List<AuroraClientDto> clientsMissingOtherInstances = new ArrayList<>();
		for(AuroraClientDto clientDto : auroraClientIdProvider.getAuroraClientDtos()){
			DnsHostEntryDto otherEntry = dnsService.getOtherReader(clientDto.getWriterClientId().getName(),
					clientDto.getClusterName());
			if(otherEntry == null){
				clientsMissingOtherInstances.add(clientDto);
			}else{
				otherReaderInstances.add(otherEntry);
			}
		}

		List<DomContent> fragments = new ArrayList<>();
		fragments.add(makeAuroraClientsTable("Aurora Clients", dnsService.getDnsEntryForClients().values(), false));
		if(otherReaderInstances.size() != 0){
			//temporarily disabling the trash icon
			fragments.add(makeAuroraClientsTable("Aurora Other Instances", otherReaderInstances, true));
		}
		if(clientsMissingOtherInstances.size() != 0){
			fragments.add(makeCreateOtherSection(clientsMissingOtherInstances));
		}
		DivTag content = div(each(fragments.stream()))
				.withClass("container my-4");
		return pageFactory.startBuilder(request)
				.withTitle("Aurora Clients")
				.withContent(content)
				.buildMav();
	}

	@Handler
	public Mav createOtherInstance(@Param(P_clientName) String clientName){
		String clusterName = rdsSettings.dbPrefix.get() + clientName;
		rdsService.createOtherInstance(clusterName);
		config.addOtherDatabaseDns(clusterName);
		var dto = new DatarouterChangelogDtoBuilder(
				"AuroraClients",
				clientName,
				"created other instance for " + clusterName,
				getSessionInfo().getNonEmptyUsernameOrElse(""))
				.build();
		changelogRecorder.record(dto);
		return new InContextRedirectMav(request, paths.datarouter.auroraInstances.inspectClientUrl.toSlashedString());
	}

	private DivTag makeAuroraClientsTable(String header, Collection<DnsHostEntryDto> rows,
			boolean showDeleteOption){
		String contextPath = request.getContextPath();
		var h2 = h2(header);
		var table = new J2HtmlTable<DnsHostEntryDto>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Client name", row -> {
					if(row.isReaderPointedToWriter() || row.isReaderPointedToOther()){
						return td(row.getClientName()).withClass("table-danger");
					}
					return td(row.getClientName());
				})
				.withColumn("Hostname", DnsHostEntryDto::getHostname)
				.withColumn("Cluster hostname", DnsHostEntryDto::getClusterHostname)
				.withColumn("Cluster name", DnsHostEntryDto::getClusterName)
				.withColumn("Replcation role", DnsHostEntryDto::getReplicationRole)
				.withColumn("Instance hostname", DnsHostEntryDto::getInstanceHostname)
				.withColumn("IP", DnsHostEntryDto::getIp)
				.withHtmlColumn("X", row -> {
					if(showDeleteOption){
						var trashIcon = i().withClass("fas fa-trash");
						return td(a(trashIcon).withHref(getDeleteOtherClientUri(contextPath, row.getClientName())));
					}
					return td("");

				})
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

	private DivTag makeCreateOtherSection(Collection<AuroraClientDto> rows){
		var h2 = h2("Create a read-only Other Instance");
		var table = table(tbody(each(rows, this::makeCreateOtherRow)));
		return div(h2, table);
	}

	private TrTag makeCreateOtherRow(AuroraClientDto row){
		String href = new URIBuilder().setPath(servletContext.getContextPath()
				+ paths.datarouter.auroraInstances.createOtherInstance.toSlashedString())
				.addParameter(P_clientName, row.getWriterClientId().getName())
				.toString();
		return tr(
				td(row.getWriterClientId().getName()),
				td(a("Create Other Instance").withHref(href))
				.withClass("text-center"));
	}

	@Handler
	public Mav deleteOtherInstance(@Param(P_clientName) String clientName){
		Require.isTrue(clientName.endsWith(rdsSettings.dbOtherInstanceSuffix.get()));
		rdsService.deleteOtherInstance(clientName);
		var dto = new DatarouterChangelogDtoBuilder(
				"AuroraClients",
				clientName,
				"deleted " + clientName + " instance",
				getSessionInfo().getNonEmptyUsernameOrElse(""))
				.build();
		changelogRecorder.record(dto);
		return new InContextRedirectMav(request, paths.datarouter.auroraInstances.inspectClientUrl.toSlashedString());
	}

	public String getDeleteOtherClientUri(String contextPath, String clientName){
		String href = new URIBuilder().setPath(contextPath
				+ paths.datarouter.auroraInstances.deleteOtherInstance.toSlashedString())
				.addParameter(P_clientName, clientName)
				.toString();
		return href;
	}

}
