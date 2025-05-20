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
package io.datarouter.nodewatch.web.handler;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.ByteLength;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.link.NodewatchTableStorageLink;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDao;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto.ColumnStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.NodewatchDatabaseType;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.nodewatch.util.TableStorageSizeTool;
import io.datarouter.nodewatch.util.TableStorageSizeTool.IndexSize;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.ThTag;
import jakarta.inject.Inject;

public class NodewatchTableStorageHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private ManagedNodesHolder managedNodesHolder;
	@Inject
	private TableStorageStatsBinaryDao statsDao;

	@Handler
	private Mav storage(NodewatchTableStorageLink link){
		String clientName = link.clientName;
		String tableName = link.tableName;
		var nodeWrapper = new PhysicalSortedNodeWrapper<>(datarouterNodes, clientName, tableName);
		Optional<TableStorageStatsBinaryDto> optStats = statsDao.find(nodeWrapper.node);
		var content = div(
				NodewatchHtml.makeHeader(
						"Storage Estimate",
						"Storage sizes for primary table, columns, and indexes."),
				navService.makeNavTabs(paths.datarouter.nodewatch.table)
						.addTableStorageTab(clientName, tableName)
						.render(),
				br(),
				NodewatchHtml.makeTableInfoDiv(clientName, tableName))
				.withClass("container");
		if(optStats.isEmpty()){
			var notFoundDiv = div("Stats not found.  They are computed daily in the background.");
			content.with(notFoundDiv);
		}else{
			TableStorageStatsBinaryDto stats = optStats.orElseThrow();
			TableCount tableCount = tableSamplerService.getCurrentTableCountFromSamples(clientName, tableName);
			ClientType<?,?> clientType = nodeWrapper.node.getClientType();
			Optional<NodewatchDatabaseType> optDatabaseType = NodewatchDatabaseType.findPrice(clientType);
			boolean includeColumnNames = optDatabaseType
					.map(databaseType -> databaseType.storesColumnNames)
					.orElse(false);
			long totalRows = tableCount.getNumRows();
			var statsDiv = div(
					br(),
					makeTableSummaryDiv(
							nodeWrapper,
							stats,
							tableCount,
							includeColumnNames,
							totalRows,
							clientType),
					br(),
					makeIndexSummaryDiv(nodeWrapper, stats, totalRows, includeColumnNames),
					br(),
					makePrimaryTableDetailsDiv(stats, includeColumnNames, totalRows),
					makeIndexesDiv(nodeWrapper, stats, includeColumnNames, totalRows),
					br());
			content.with(statsDiv);
		}
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Storage Estimate")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	/*-------- table summary ----------*/

	private DivTag makeTableSummaryDiv(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableStorageStatsBinaryDto stats,
			TableCount tableCount,
			boolean includeColumnNames,
			long totalRows,
			ClientType<?,?> clientType){
		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(nodeWrapper.node);
		Optional<NodewatchDatabaseType> optDatabaseType = NodewatchDatabaseType.findPrice(clientType);
		double storageMultiplier = optDatabaseType.map(type -> type.storageMultiplier).orElse(1.0);

		// byte calculations
		long avgNameBytesPerRow = stats.columns.stream()
				.mapToLong(col -> col.avgNameBytes)
				.sum();
		long totalNameBytes = avgNameBytesPerRow * totalRows;
		long avgValueBytesPerRow = stats.columns.stream()
				.mapToLong(col -> col.avgValueBytes)
				.sum();
		long totalValueBytes = avgValueBytesPerRow * totalRows;
		long avgRowBytes = includeColumnNames ? avgNameBytesPerRow + avgValueBytesPerRow : avgValueBytesPerRow;
		long totalPrimaryTableBytes = avgRowBytes * totalRows;
		long totalIndexBytes = TableStorageSizeTool.calcTotalIndexSize(stats, managedNodes, totalRows).toBytes();
		long totalBytes = totalPrimaryTableBytes + totalIndexBytes;
		long adjustedTotalBytes = (long)(storageMultiplier * totalBytes);

		// dollar calculations
		String yearlyStorageCostString = "?";
		boolean shouldDisplayNodeCost = false;
		String yearlyNodeCostString = "?";
		String yearlyTotalCostString = "?";
		if(optDatabaseType.isPresent()){
			NodewatchDatabaseType databaseType = optDatabaseType.get();
			double yearlyTotalCost = 0;

			// storage
			double yearlyStorageCost = databaseType.dollarsPerTiBPerYear()
					* ByteLength.ofBytes(adjustedTotalBytes).toTiBDouble();
			yearlyStorageCostString = "$" + NumberFormatter.format(yearlyStorageCost, 2);
			yearlyTotalCost += yearlyStorageCost;

			// nodes
			Optional<Double> optYearlyNodeCost = databaseType.findYearlyNodeCost(
					ByteLength.ofBytes(adjustedTotalBytes));
			if(optYearlyNodeCost.isPresent()){
				shouldDisplayNodeCost = true;
				double yearlyNodeCost = optYearlyNodeCost.orElseThrow();
				yearlyNodeCostString = "$" + NumberFormatter.format(yearlyNodeCost, 2);
				yearlyTotalCost += yearlyNodeCost;
			}
			yearlyTotalCostString = "$" + NumberFormatter.format(yearlyTotalCost, 2);
		}

		// table
		record Row(
				String name,
				String value){
		}
		List<Row> rows = new ArrayList<>();
		rows.add(new Row("Nodewatch Rows", NumberFormatter.addCommas(tableCount.getNumRows())));
		rows.add(new Row("Rows Sampled", NumberFormatter.addCommas(stats.numRowsIncluded)));
		if(includeColumnNames){
			rows.add(new Row("Avg Name Bytes", ByteLength.ofBytes(avgNameBytesPerRow).toDisplay()));
			rows.add(new Row("Avg Value Bytes", ByteLength.ofBytes(avgValueBytesPerRow).toDisplay()));
		}
		rows.add(new Row("Avg Row Bytes", ByteLength.ofBytes(avgRowBytes).toDisplay()));
		if(includeColumnNames){
			rows.add(new Row("Est Name Bytes", ByteLength.ofBytes(totalNameBytes).toDisplay()));
			rows.add(new Row("Est Value Bytes", ByteLength.ofBytes(totalValueBytes).toDisplay()));
		}
		rows.add(new Row("Est Primary Data Size", ByteLength.ofBytes(totalPrimaryTableBytes).toDisplay()));
		rows.add(new Row("Est Index Data Size", ByteLength.ofBytes(totalIndexBytes).toDisplay()));
		rows.add(new Row("Est Total Data Size", ByteLength.ofBytes(totalBytes).toDisplay()));
		rows.add(new Row("Database Multiplier", Double.toString(storageMultiplier)));
		rows.add(new Row("Est Storage Size", ByteLength.ofBytes(adjustedTotalBytes).toDisplay()));
		rows.add(new Row("Est Yearly Storage Cost", yearlyStorageCostString));
		if(shouldDisplayNodeCost){
			rows.add(new Row("Min Yearly Node Cost", yearlyNodeCostString));
			rows.add(new Row("Min Yearly Total Cost", yearlyTotalCostString));
		}
		var table = new J2HtmlTable<Row>()
				.withClasses("table table-sm table-striped my-2 border")
				.withColumn("Name", Row::name)
				.withColumn("Value", Row::value)
				.build(rows);

		var wrapper = div(table)
				.withStyle("width:400px;");
		return div(
				h5("Table Summary"),
				wrapper);
	}

	private List<IndexSize> getAllIndexSizes(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableStorageStatsBinaryDto stats,
			long totalRows,
			boolean includeNameBytes){
		long primaryBytes = totalRows * stats.avgValueBytesPerRow();
		if(includeNameBytes){
			primaryBytes += totalRows * stats.avgNameBytesPerRow();
		}
		IndexSize primaryIndexSize = new IndexSize(
				"Primary",
				ByteLength.ofBytes(primaryBytes),
				ByteLength.ofBytes(stats.avgValueBytesPerRow()));
		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(nodeWrapper.node);
		List<IndexSize> secondaryIndexSizes = Scanner.of(managedNodes)
				.map(managedNode -> TableStorageSizeTool.calcIndexSize(stats, managedNode, totalRows))
				.sort(Comparator.comparing(IndexSize::indexName))
				.list();
		return ObjectScanner.of(primaryIndexSize)
				.append(secondaryIndexSizes)
				.list();
	}

	/*--------- index summary html ----------*/

	private DivTag makeIndexSummaryDiv(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableStorageStatsBinaryDto stats,
			long totalRows,
			boolean includeColumnNames){
		List<IndexSize> indexSizes = getAllIndexSizes(nodeWrapper, stats, totalRows, includeColumnNames);
		var indexTable = new J2HtmlTable<IndexSize>()
				.withClasses("sortable table table-sm table-striped border")
				.withHtmlColumn(
						makeThFixedWidth("Name", 200),
						indexSize -> td(indexSize.indexName()))
				.withHtmlColumn(
						makeThFixedWidth("Total Bytes", 100),
						indexSize -> td(NumberFormatter.addCommas(indexSize.totalSize().toBytes())))
				.withHtmlColumn(
						makeThFixedWidth("Avg Row Bytes", 100),
						indexSize -> td(NumberFormatter.addCommas(indexSize.avgRowSize().toBytes())))
				.build(indexSizes);
		return div(
				h5(String.format("Indexes (%s)", indexSizes.size())),
				indexTable);
	}

	/*-------- primary table details ----------*/

	private DivTag makePrimaryTableDetailsDiv(
			TableStorageStatsBinaryDto stats,
			boolean includeColumnNames,
			long totalRows){
		List<ColumnStorageStatsBinaryDto> rows = Scanner.of(stats.columns)
				.sort(Comparator.comparing(columnStats -> columnStats.name))
				.list();
		var table = makeColumnStatsTableBuilder(includeColumnNames, totalRows).build(rows);
		return div(
				h5(String.format("Primary Table Columns (%s)", rows.size())),
				table);
	}

	/*-------- individual index details --------*/

	private DivTag makeIndexesDiv(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableStorageStatsBinaryDto stats,
			boolean includeColumnNames,
			long totalRows){
		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(nodeWrapper.node);
		var result = div();
		Scanner.of(managedNodes)
				.sort(Comparator.comparing(ManagedNode::getName))
				.map(managedNode -> makeIndexDetailsDiv(
						managedNode,
						stats,
						includeColumnNames,
						totalRows))
				.forEach(indexDiv -> result.with(div(br(), indexDiv)));
		return result;
	}

	private DivTag makeIndexDetailsDiv(
			ManagedNode<?,?,?,?,?> managedNode,
			TableStorageStatsBinaryDto stats,
			boolean includeColumnNames,
			long totalRows){
		Map<String,ColumnStorageStatsBinaryDto> columnSummaryByName = Scanner.of(stats.columns)
				.toMap(columnStats -> columnStats.name);
		List<String> columnNames = managedNode.getIndexEntryFieldInfo().getFieldColumnNames();
		List<ColumnStorageStatsBinaryDto> rows = Scanner.of(columnNames)
				.map(columnSummaryByName::get)
				.sort(Comparator.comparing(columnStats -> columnStats.name))
				.list();
		var table = makeColumnStatsTableBuilder(includeColumnNames, totalRows).build(rows);
		return div(
				h5(String.format("Index: %s (%s)", managedNode.getName(), rows.size())),
				table);
	}

	/*---------- per-column table builder ----------*/

	private J2HtmlTable<ColumnStorageStatsBinaryDto> makeColumnStatsTableBuilder(
			boolean includeColumnNames,
			long totalRows){
		var tableBuilder = new J2HtmlTable<ColumnStorageStatsBinaryDto>()
			.withClasses("sortable table table-sm table-striped border")
			.withHtmlColumn(
					makeThFixedWidth("Column", 200),
					columnStats -> td(columnStats.name))
			.withHtmlColumn(
					makeThFixedWidth(
							includeColumnNames ? "Total Value Bytes" : "Total Bytes",
							100),
					columnStats -> {
						long totalValueBytes = columnStats.avgValueBytes * totalRows;
						return td(NumberFormatter.addCommas(totalValueBytes));
					})
			.withHtmlColumn(
					makeThFixedWidth(
							includeColumnNames ? "Avg Value Bytes" : "Avg Bytes",
							100),
					columnStats -> td(NumberFormatter.addCommas(columnStats.avgValueBytes)));
		if(includeColumnNames){
			tableBuilder
				.withHtmlColumn(
						makeThFixedWidth("Total Name Bytes", 100),
						columnStats -> {
							long totalNameBytes = columnStats.avgNameBytes * totalRows;
							return td(NumberFormatter.addCommas(totalNameBytes));
						})
				.withHtmlColumn(
						makeThFixedWidth("Avg Name Bytes", 100),
						columnStats -> td(NumberFormatter.addCommas(columnStats.avgNameBytes)));
		}
		return tableBuilder;
	}

	private ThTag makeThFixedWidth(String name, int width){
		String style = String.format("width:%spx;", width);
		return th(name).withStyle(style);
	}

}
