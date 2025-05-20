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
package io.datarouter.nodewatch.shadowtable.service;

import java.util.List;
import java.util.Objects;

import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.storage.tag.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ShadowTableNodeSelectionService{

	private static final boolean EXCLUDE_DATAROUTER_TABLES = false;

	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private TableConfigurationService tableConfigurationService;
	@Inject
	private TableSamplerService tableSamplerService;

	public boolean hasNodesForExport(ShadowTableExport export){
		return scanNodesForExport(export).hasAny();
	}

	public List<PhysicalSortedStorageReaderNode<?,?,?>> listNodesForExport(ShadowTableExport export){
		return scanNodesForExport(export).list();
	}

	private Scanner<PhysicalSortedStorageReaderNode<?,?,?>> scanNodesForExport(ShadowTableExport export){
		return datarouterClients.findClientId(export.clientName())
				.map(clientId -> tableSamplerService.scanCountableNodes()
						.include(node -> Objects.equals(node.getClientId().getName(), clientId.getName()))
						.exclude(node -> isDatarouterTable(node) && EXCLUDE_DATAROUTER_TABLES)
						.include(this::isEnabledInNodewatchConfig))
				.orElse(Scanner.empty());
	}

	private boolean isDatarouterTable(PhysicalSortedStorageReaderNode<?,?,?> node){
		return node.getFieldInfo().findTag().orElse(null) == Tag.DATAROUTER;
	}

	private boolean isEnabledInNodewatchConfig(PhysicalSortedStorageReaderNode<?,?,?> node){
		return tableConfigurationService.findConfig(node)
				.map(config -> config.enableShadowTableExport)
				.orElse(NodewatchConfigurationBuilder.DEFAULT_ENABLE_SHADOW_TABLE_EXPORT);
	}

	public boolean enableCompression(PhysicalSortedStorageReaderNode<?,?,?> node){
		return tableConfigurationService.findConfig(node)
				.map(config -> config.enableShadowTableCompression)
				.orElse(NodewatchConfigurationBuilder.DEFAULT_ENABLE_SHADOW_TABLE_COMPRESSION);
	}

	public int scanBatchSizeForNode(PhysicalSortedStorageReaderNode<?,?,?> node){
		return tableConfigurationService.findConfig(node)
				.map(config -> config.shadowTableScanBatchSize)
				.orElse(NodewatchConfigurationBuilder.DEFAULT_SHADOW_TABLE_SCAN_BATCH_SIZE);
	}

}
