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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.service.TableStorageSummarizer;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.ColumnSize;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.ColumnSummary;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.TableSummary;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.NodewatchDatabaseType;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientAndTableNames;
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

	public static final String
			P_clientName = "clientName",
			P_tableName = "tableName";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchLinks links;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private ManagedNodesHolder managedNodesHolder;

	@Handler
	private Mav storage(String clientName, String tableName){
		var nodeWrapper = new PhysicalSortedNodeWrapper<>(datarouterNodes, clientName, tableName);
		TableSummary tableSummary = new TableStorageSummarizer<>(
				() -> false,
				tableSamplerService,
				datarouterNodes,
				new ClientAndTableNames(clientName, tableName),
				200_000)
				.summarizeTable();
		TableCount tableCount = tableSamplerService.getCurrentTableCountFromSamples(clientName, tableName);
		ClientType<?,?> clientType = nodeWrapper.node.getClientType();
		Optional<NodewatchDatabaseType> optDatabaseType = NodewatchDatabaseType.findPrice(clientType);
		boolean includeColumnNames = optDatabaseType.map(databaseType -> databaseType.storesColumnNames).orElse(false);
		long extrapolatedRowCount = Math.max(tableCount.getNumRows(), tableSummary.numRowsIncluded());
		var content = div(
				NodewatchHtml.makeHeader(
						"Storage Estimate",
						"Storage sizes for primary table, columns, and indexes."),
				navService.makeNavTabs(paths.datarouter.nodewatch.table)
						.addTableStorageTab(clientName, tableName)
						.render(),
				br(),
				NodewatchHtml.makeTableInfoDiv(clientName, tableName),
				br(),
				makeTableSummaryDiv(
						nodeWrapper,
						tableSummary,
						tableCount,
						includeColumnNames,
						extrapolatedRowCount,
						clientType),
				br(),
				makeIndexSummaryDiv(nodeWrapper, tableSummary, extrapolatedRowCount),
				br(),
				makePrimaryTableDetailsDiv(tableSummary, includeColumnNames, extrapolatedRowCount),
				makeIndexesDiv(nodeWrapper, tableSummary, includeColumnNames, extrapolatedRowCount),
				br())
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Storage Estimate")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	/*-------- table summary ----------*/

	private DivTag makeTableSummaryDiv(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableSummary tableSummary,
			TableCount tableCount,
			boolean includeColumnNames,
			long extrapolatedRowCount,
			ClientType<?,?> clientType){
		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(nodeWrapper.node);
		Optional<NodewatchDatabaseType> optDatabaseType = NodewatchDatabaseType.findPrice(clientType);
		double storageMultiplier = optDatabaseType.map(type -> type.storageMultiplier).orElse(1.0);

		// bytes
		ByteLength extrapolatedNameBytes = tableSummary.extrapolateNameSize(extrapolatedRowCount);
		ByteLength extrapolatedValueBytes = tableSummary.extrapolateValueSize(extrapolatedRowCount);
		ByteLength extrapolatedPrimaryTableBytes = includeColumnNames
				? ByteLength.sum(extrapolatedNameBytes, extrapolatedValueBytes)
				: extrapolatedValueBytes;
		ByteLength extrapolatedIndexBytes = calcTotalIndexSize(tableSummary, managedNodes, extrapolatedRowCount);
		ByteLength extrapolatedTotalBytes = ByteLength.sum(extrapolatedPrimaryTableBytes, extrapolatedIndexBytes);
		ByteLength adjustedBytes = ByteLength.ofBytes(
				(long)(extrapolatedTotalBytes.toBytes() * storageMultiplier));

		// dollars
		Optional<Double> optYearlyStorageCost = optDatabaseType
				.map(price -> price.dollarsPerTiBPerYear() * adjustedBytes.toTiBDouble());
		Optional<Double> optYearlyNodeCost = optDatabaseType
				.flatMap(price -> price.findYearlyNodeCost(adjustedBytes));
		Function<Optional<Double>,String> toDollarString = optDouble -> optDouble
				.map(yearlyCost -> NumberFormatter.format(yearlyCost, 2))
				.map(formattedDollars -> "$" + formattedDollars)
				.orElse("?");
		double yearlyTotalCost = optYearlyStorageCost.orElse(0d) + optYearlyNodeCost.orElse(0d);

		// table
		record Row(
				String name,
				String value){
		}
		List<Row> rows = new ArrayList<>();
		rows.add(new Row("Nodewatch Rows", NumberFormatter.addCommas(tableCount.getNumRows())));
		rows.add(new Row("Rows Sampled", NumberFormatter.addCommas(tableSummary.numRowsIncluded())));
		rows.add(new Row("Bytes Sampled", tableSummary.totalValueBytes().toDisplay()));
		if(includeColumnNames){
			rows.add(new Row("Avg Name Bytes", tableSummary.avgNameBytes().toDisplay()));
			rows.add(new Row("Avg Value Bytes", tableSummary.avgValueBytes().toDisplay()));
			rows.add(new Row("Avg Row Bytes", tableSummary.avgTotalBytes().toDisplay()));
		}else{
			rows.add(new Row("Avg Row Bytes", tableSummary.avgValueBytes().toDisplay()));
		}
		if(includeColumnNames){
			rows.add(new Row("Est Name Bytes", extrapolatedNameBytes.toDisplay()));
			rows.add(new Row("Est Value Bytes", extrapolatedValueBytes.toDisplay()));
		}
		rows.add(new Row("Est Primary Data Size", extrapolatedPrimaryTableBytes.toDisplay()));
		rows.add(new Row("Est Index Data Size", extrapolatedIndexBytes.toDisplay()));
		rows.add(new Row("Est Total Data Size", extrapolatedTotalBytes.toDisplay()));
		rows.add(new Row("Database Multiplier", Double.toString(storageMultiplier)));
		rows.add(new Row("Est Storage Size", adjustedBytes.toDisplay()));
		rows.add(new Row("Est Yearly Storage Cost", toDollarString.apply(optYearlyStorageCost)));
		if(optYearlyNodeCost.isPresent()){
			rows.add(new Row("Min Yearly Node Cost", toDollarString.apply(optYearlyNodeCost)));
			rows.add(new Row("Min Yearly Total Cost", toDollarString.apply(Optional.of(yearlyTotalCost))));
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

	private ByteLength calcTotalIndexSize(
			TableSummary tableSummary,
			List<? extends ManagedNode<?,?,?,?,?>> managedNodes,
			long extrapolatedRowCount){
		return Scanner.of(managedNodes)
				.map(managedNode -> calcIndexSize(tableSummary, managedNode, extrapolatedRowCount))
				.map(IndexSize::totalSize)
				.listTo(ByteLength::sum);
	}

	/*--------- index summary html ----------*/

	private record IndexSize(
			String indexName,
			ByteLength totalSize,
			ByteLength avgRowSize){
	}

	private DivTag makeIndexSummaryDiv(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableSummary tableSummary,
			long extrapolatedRowCount){
		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(nodeWrapper.node);
		List<IndexSize> indexSizes = Scanner.of(managedNodes)
				.map(managedNode -> calcIndexSize(tableSummary, managedNode, extrapolatedRowCount))
				.sort(Comparator.comparing(IndexSize::indexName))
				.list();
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
				h5(String.format("Indexes (%s)", managedNodes.size())),
				indexTable);
	}

	private IndexSize calcIndexSize(
			TableSummary tableSummary,
			ManagedNode<?,?,?,?,?> managedNode,
			long extrapolatedRowCount){
		Set<String> columnNames = new HashSet<>(managedNode.getIndexEntryFieldInfo().getFieldColumnNames());
		ColumnSize combinedColumnSize = Scanner.of(tableSummary.subset(columnNames))
				.map(ColumnSummary::size)
				.reduce(ColumnSize.EMPTY, (a, b) -> ColumnSize.combine(tableSummary.numRowsIncluded(), a, b));
		return new IndexSize(
				managedNode.getName(),
				combinedColumnSize.extrapolateTotalValueBytes(extrapolatedRowCount),
				combinedColumnSize.avgValueBytes());
	}

	/*-------- primary table details ----------*/

	private DivTag makePrimaryTableDetailsDiv(
			TableSummary tableSummary,
			boolean includeColumnNames,
			long extrapolatedRowCount){
		List<ColumnSummary> rows = Scanner.of(tableSummary.columnSummaries())
				.sort(Comparator.comparing(ColumnSummary::name))
				.list();
		var table = makeColumnSummaryTableBuilder(includeColumnNames, extrapolatedRowCount).build(rows);
		return div(
				h5(String.format("Primary Table Columns (%s)", rows.size())),
				table);
	}

	/*-------- individual index details --------*/

	private DivTag makeIndexesDiv(
			PhysicalSortedNodeWrapper<?,?,?> nodeWrapper,
			TableSummary tableSummary,
			boolean includeColumnNames,
			long extrapolatedRowCount){
		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(nodeWrapper.node);
		var result = div();
		Scanner.of(managedNodes)
				.sort(Comparator.comparing(ManagedNode::getName))
				.map(managedNode -> makeIndexDetailsDiv(
						managedNode,
						tableSummary,
						includeColumnNames,
						extrapolatedRowCount))
				.forEach(indexDiv -> result.with(div(br(), indexDiv)));
		return result;
	}

	private DivTag makeIndexDetailsDiv(
			ManagedNode<?,?,?,?,?> managedNode,
			TableSummary tableSummary,
			boolean includeColumnNames,
			long extrapolatedRowCount){
		Map<String,ColumnSummary> columnSummaryByName = Scanner.of(tableSummary.columnSummaries())
				.toMap(ColumnSummary::name);
		List<String> columnNames = managedNode.getIndexEntryFieldInfo().getFieldColumnNames();
		List<ColumnSummary> rows = Scanner.of(columnNames)
				.map(columnSummaryByName::get)
				.sort(Comparator.comparing(ColumnSummary::name))
				.list();
		var table = makeColumnSummaryTableBuilder(includeColumnNames, extrapolatedRowCount).build(rows);
		return div(
				h5(String.format("Index: %s (%s)", managedNode.getName(), rows.size())),
				table);
	}

	/*---------- per-column table builder ----------*/

	private J2HtmlTable<ColumnSummary> makeColumnSummaryTableBuilder(
			boolean includeColumnNames,
			long extrapolatedRowCount){
		var tableBuilder = new J2HtmlTable<ColumnSummary>()
			.withClasses("sortable table table-sm table-striped border")
			.withHtmlColumn(
					makeThFixedWidth("Column", 200),
					col -> td(col.name()))
			.withHtmlColumn(
					makeThFixedWidth(
							includeColumnNames ? "Total Value Bytes" : "Total Bytes",
							100),
					columnSummary -> {
						long totalValueBytes = columnSummary.size()
								.extrapolateTotalValueBytes(extrapolatedRowCount)
								.toBytes();
						return td(NumberFormatter.addCommas(totalValueBytes));
					})
			.withHtmlColumn(
					makeThFixedWidth(
							includeColumnNames ? "Avg Value Bytes" : "Avg Bytes",
							100),
					columnSummary -> {
						long avgValueBytes = columnSummary.size().avgValueBytes().toBytes();
						return td(NumberFormatter.addCommas(avgValueBytes));
					});
		if(includeColumnNames){
			tableBuilder
				.withHtmlColumn(
						makeThFixedWidth("Total Name Bytes", 100),
						columnSummary -> {
							long totalNameBytes = columnSummary.size()
									.extrapolateTotalNameBytes(extrapolatedRowCount)
									.toBytes();
							return td(NumberFormatter.addCommas(totalNameBytes));
						})
				.withHtmlColumn(
						makeThFixedWidth("Avg Name Bytes", 100),
						columnSummary -> {
							long avgNameBytes = columnSummary.size().avgNameBytes().toBytes();
							return td(NumberFormatter.addCommas(avgNameBytes));
						});
		}
		return tableBuilder;
	}

	private ThTag makeThFixedWidth(String name, int width){
		String style = String.format("width:%spx;", width);
		return th(name).withStyle(style);
	}

}
