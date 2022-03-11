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

import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.input;
import static j2html.TagCreator.join;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.xbill.DNS.TextParseException;

import io.datarouter.aws.rds.config.DatarouterAwsPaths;
import io.datarouter.aws.rds.config.DatarouterAwsRdsConfigSettings;
import io.datarouter.aws.rds.job.DnsUpdater;
import io.datarouter.aws.rds.service.AuroraDnsService;
import io.datarouter.aws.rds.service.AuroraDnsService.DnsHostEntryDto;
import io.datarouter.aws.rds.service.DatabaseAdministrationConfiguration;
import io.datarouter.aws.rds.service.RdsService;
import io.datarouter.storage.client.ClientId;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

public class AuroraInstancesHandler extends BaseHandler{

	private static final String P_clientName = "clientName";

	@Inject
	private AuroraDnsService dnsService;
	@Inject
	private DatarouterAwsPaths paths;
	@Inject
	private RdsService rdsService;
	@Inject
	private DatarouterAwsRdsConfigSettings rdsSettings;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatabaseAdministrationConfiguration config;
	@Inject
	private DnsUpdater dnsUpdater;

	@Handler(defaultHandler = true)
	public Mav inspectClientUrl(){
		List<DnsHostEntryDto> otherReaderInstances = new ArrayList<>();
		List<ClientId> clientsMissingOtherInstances = new ArrayList<>();
		for(ClientId primaryClientId : dnsService.getPrimaryClientIds()){
			DnsHostEntryDto otherEntry = dnsService.getOtherReader(primaryClientId.getName());
			if(otherEntry == null){
				clientsMissingOtherInstances.add(primaryClientId);
			}else{
				otherReaderInstances.add(otherEntry);
			}
		}
		List<DomContent> fragments = new ArrayList<>();
		fragments.add(makeAuroraClientsTable("Aurora Clients", dnsService.checkClientEndpoint().getLeft()));
		if(otherReaderInstances.size() != 0){
			fragments.add(makeAuroraClientsTable("Aurora Other Instances", otherReaderInstances));
		}
		if(clientsMissingOtherInstances.size() != 0){
			fragments.add(makeForm(clientsMissingOtherInstances));
		}
		ContainerTag<?> content = div(each(fragments.stream()))
				.withClass("container my-4");
		return pageFactory.startBuilder(request)
				.withTitle("Aurora Clients")
				.withContent(content)
				.buildMav();
	}

	@Handler
	public String addCname(String subdomain, String target) throws TextParseException{
		return dnsUpdater.addCname(subdomain, target);
	}

	@Handler
	public String deleteCname(String subdomain) throws TextParseException{
		return dnsUpdater.deleteCname(subdomain);
	}

	@Handler
	public Mav createOtherInstance(@Param(P_clientName) String clientName){
		String clusterName = rdsSettings.dbPrefix.get() + clientName;
		rdsService.createOtherInstance(clusterName);
		config.addOtherDatabaseDns(clientName);
		return new InContextRedirectMav(request, paths.datarouter.auroraInstances.toSlashedString());
	}

	private static ContainerTag<?> makeAuroraClientsTable(String header, Collection<DnsHostEntryDto> rows){
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
				.withColumn("Replcation role", DnsHostEntryDto::getReplicationRole)
				.withColumn("Instance hostname", DnsHostEntryDto::getInstanceHostname)
				.withColumn("IP", DnsHostEntryDto::getIp)
				.withCaption("Total " + rows.size())
				.build(rows);
		return div(h2, table)
				.withClass("container my-4");
	}

	private ContainerTag<?> makeForm(Collection<ClientId> rows){
		var h2 = h2("Create a read only other Instance");
		var innerFormTable = table(tbody(each(rows, AuroraInstancesHandler::makeRow)));
		return form(join(h2, innerFormTable))
				.withMethod("get")
				.withAction(servletContext.getContextPath() + paths.datarouter.auroraInstances.toSlashedString());
	}

	private static ContainerTag<?> makeRow(ClientId row){
		return tr(
				td(input()
						.withType("text")
						.withName(P_clientName)
						.withClass("form-control-plaintext")
						.withValue(row.getName())),
				td(join(
					input()
						.withType("hidden")
						.withName("submitAction")
						.withClass("form-control-plaintext")
						.withValue("createOtherInstance")),
					button("Create")
						.withClass("btn btn-warning"))
				.withClass("text-center"));
	}

}
