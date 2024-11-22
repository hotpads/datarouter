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
package io.datarouter.plugin.copytable.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.FormTag;
import jakarta.inject.Inject;

public class SystemTablesCopyHandler extends BaseHandler{

	private static final String
			P_sourceClientName = "sourceClientName",
			P_targetClientName = "targetClientName",
			P_submitAction = "submitAction";

	@Inject
	private TableSamplerService service;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterCopyTablePaths paths;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceClientName) Optional<String> sourceClientName,
			@Param(P_targetClientName) Optional<String> targetClientName,
			@Param(P_submitAction) Optional<String> submitAction){
		Map<String,ClientId> clientIdMap = getClients().stream()
				.collect(Collectors.toMap(ClientId::getName, Function.identity()));
		List<String> clientNames = clientIdMap.keySet().stream().toList();
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Source Client Name")
				.withName(P_sourceClientName)
				.withValues(clientNames)
				.withSelected(sourceClientName.orElse(null));
		form.addSelectField()
				.withLabel("Target Client Name")
				.withName(P_targetClientName)
				.withValues(clientNames)
				.withSelected(targetClientName.orElse(null));
		form.addButton()
				.withLabel("System Tables")
				.withValue("anything");
		form.withAction(servletContext.getContextPath() + paths.datarouter.systemTableCopier.viewTables
				.toSlashedString());
		FormTag formTag = Bootstrap4FormHtml.render(form, true);
		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Copy Table - System Table Copier")
					.withContent(SystemTableCopyHtml.makeHeader(formTag))
					.buildMav();
		}

		List<PhysicalNode<?,?,?>> systemTablesToCopy = getSystemTables(clientIdMap.get(sourceClientName.get()));
		List<String> nodeNames = systemTablesToCopy.stream()
				.map(node -> node.getFieldInfo().getNodeName())
				.toList();
		return pageFactory.startBuilder(request)
				.withTitle("Copy Table - System Table Copier")
				.withContent(SystemTableCopyHtml.makeContent(formTag, nodeNames))
				.buildMav();
	}

	private List<ClientId> getClients(){
		return service.listClientIdsWithCountableNodes();
	}

	private List<PhysicalNode<?,?,?>> getCoutableNodes(ClientId clientId){
		return Scanner.of(datarouterNodes.getWritableNodes(List.of(clientId)))
				.include(service::isCountableNode)
				.list();
	}

	private List<PhysicalNode<?,?,?>> getSystemTables(ClientId clientId){
		return Scanner.of(getCoutableNodes(clientId))
				.include(this::isSystemTable)
				.list();
	}

	public boolean isSystemTable(PhysicalNode<?,?,?> node){
		String tag = node.getFieldInfo().findTag().map(Tag::displayLowerCase).orElse("unknown");
		return !tag.equals(Tag.APP.displayLowerCase());
	}

}