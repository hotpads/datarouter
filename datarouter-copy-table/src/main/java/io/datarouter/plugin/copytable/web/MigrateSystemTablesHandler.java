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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.plugin.copytable.CopyTableService;
import io.datarouter.plugin.copytable.CopyTableService.CopyTableSpanResult;
import io.datarouter.plugin.copytable.SystemTableCopyService;
import io.datarouter.plugin.copytable.link.MigrateSystemTablesLink;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.SortedStorage.PhysicalSortedStorageNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.FormTag;
import jakarta.inject.Inject;

public class MigrateSystemTablesHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(MigrateSystemTablesHandler.class);

	private static final int SCAN_BATCH_SIZE = 100;
	private static final int PUT_BATCH_SIZE = 100;
	private static final int NUM_THREADS = 4;

	@Inject
	private TableSamplerService service;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private CopyTableService copyTableService;
	@Inject
	private SystemTableCopyService systemTableCopyService;

	@Handler
	@SuppressWarnings("unused")
	private <PK extends PrimaryKey<PK>, D extends Databean<PK,D>> Mav migrateSystemTables(MigrateSystemTablesLink link){

		Optional<String> sourceClientName = link.sourceClientName;
		Optional<String> targetClientName = link.targetClientName;
		boolean shouldValidate = sourceClientName.isPresent() && targetClientName.isPresent();
		Map<String,ClientId> clientIdMap = getClients().stream()
				.collect(Collectors.toMap(ClientId::getName, Function.identity()));
		List<String> clientNames = clientIdMap.keySet().stream().toList();
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Source Client Name")
				.withName(MigrateSystemTablesLink.P_sourceClientName)
				.withValues(clientNames)
				.withSelected(sourceClientName.orElse(null));
		form.addSelectField()
				.withLabel("Target Client Name")
				.withName(MigrateSystemTablesLink.P_targetClientName)
				.withValues(clientNames)
				.withSelected(targetClientName.orElse(null));
		form.addButtonWithoutSubmitAction()
				.withLabel("Migrate System Tables")
				.withValue("migrateSystemTables");

		FormTag formTag = Bootstrap4FormHtml.render(form, true);
		if(!shouldValidate || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Copy Table - System Table Copier")
					.withContent(SystemTableCopyHtml.makeHeader(formTag))
					.buildMav();
		}

		List<PhysicalNode<?,?,?>> systemTables = systemTableCopyService.getSystemTables(
				clientIdMap.get(sourceClientName.get()));
		logger.warn("Received {} system tables for client : {}", systemTables.size(), sourceClientName.get());
		long joblets = 0;
		for(PhysicalNode<?,?,?> node : systemTables){
			@SuppressWarnings("unchecked")
			PhysicalSortedStorageNode<PK,D,?> sourceNode = (PhysicalSortedStorageNode<PK,D,?>)node;
			String tableName = sourceNode.getFieldInfo().getTableName();

			String sourceNodeName = datarouterNodes.getPhysicalNodeForClientAndTable(sourceClientName.get(), tableName)
					.getName();
			String targetNodeName = datarouterNodes.getPhysicalNodeForClientAndTable(targetClientName.get(), tableName)
					.getName();
			logger.warn("Copying system tables - SystemTable: {}", tableName);

			CopyTableSpanResult result = copyTableService.copyTableSpan(
					sourceNodeName,
					targetNodeName,
					null,
					null,
					NUM_THREADS,
					SCAN_BATCH_SIZE,
					PUT_BATCH_SIZE,
					1,
					1,
					false);
			logger.warn("Finished Copying SystemTable: {} with result: {}", tableName, result);
			if(!result.success()){
				String message = String.format("Failure while migrating tables from %s to %s: %s",
						sourceClientName.get(),
						targetClientName.get(),
						result);
				return pageFactory.message(request, message);
			}
		}

		return pageFactory.message(request, "Succesfully migrated tables from " + sourceClientName.get() + " to "
				+ targetClientName.get());
	}

	private List<ClientId> getClients(){
		return service.listClientIdsWithCountableNodes();
	}

}
