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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.service.TableStorageSummarizer;
import io.datarouter.nodewatch.service.TableStorageSummarizer.ColumnSummary;
import io.datarouter.nodewatch.service.TableStorageSummarizer.TableSummary;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.NodewatchDatabaseType;
import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
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

	@Handler
	private Mav storage(String clientName, String tableName){
		var nodeWrapper = new PhysicalSortedNodeWrapper<>(datarouterNodes, clientName, tableName);
		TableSummary tableSummary = new TableStorageSummarizer<>(
				() -> false,
				tableSamplerService,
				datarouterNodes,
				clientName,
				tableName,
				200_000)
				.summarize();
		TableCount tableCount = tableSamplerService.getCurrentTableCountFromSamples(clientName, tableName);
		long extrapolatedRowCount = Math.max(tableCount.getNumRows(), tableSummary.numRowsIncluded());
		ClientType<?,?> clientType = nodeWrapper.node.getClientType();
		var content = div(
				NodewatchHtml.makeHeader(
						"Storage Estimate",
						"Summary of storage size by column and overall table, not including indexes."),
				navService.makeNavTabs(paths.datarouter.nodewatch.table)
						.addTableStorageTab(clientName, tableName)
						.render(),
				br(),
				NodewatchHtml.makeTableInfoDiv(clientName, tableName),
				br(),
				makeTableSummaryDiv(tableSummary, tableCount, extrapolatedRowCount, clientType),
				br(),
				makeColumnSummaryDiv(tableSummary, extrapolatedRowCount))
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Storage Estimate")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	/*-------- table summary html ----------*/

	private DivTag makeTableSummaryDiv(
			TableSummary tableSummary,
			TableCount tableCount,
			long extrapolatedRowCount,
			ClientType<?,?> clientType){
		Optional<NodewatchDatabaseType> optDatabaseType = NodewatchDatabaseType.findPrice(clientType);
		boolean includeNameBytes = optDatabaseType.map(type -> type.storesColumnNames).orElse(true);
		ByteLength extrapolatedNameBytes = tableSummary.extrapolateNameSize(extrapolatedRowCount);
		ByteLength extrapolatedValueBytes = tableSummary.extrapolateValueSize(extrapolatedRowCount);
		ByteLength extrapolatedBytes = includeNameBytes
				? ByteLength.sum(extrapolatedNameBytes, extrapolatedValueBytes)
				: extrapolatedValueBytes;
		tableSummary.extrapolateValueSize(extrapolatedRowCount);
		Optional<Double> optYearlyStorageCost = optDatabaseType
				.map(price -> price.dollarsPerTiBPerYear() * extrapolatedBytes.toTiBDouble());
		Optional<Double> optYearlyNodeCost = optDatabaseType
				.flatMap(price -> price.findYearlyNodeCost(extrapolatedBytes));
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
		if(includeNameBytes){
				rows.add(new Row("Avg Name Bytes", tableSummary.avgNameBytes().toDisplay()));
				rows.add(new Row("Avg Value Bytes", tableSummary.avgValueBytes().toDisplay()));
		}
		rows.add(new Row("Avg Row Size", tableSummary.avgTotalBytes().toDisplay()));
		if(optDatabaseType.map(type -> type.storesColumnNames).orElse(true)){
			rows.add(new Row("Est Name Bytes", extrapolatedNameBytes.toDisplay()));
			rows.add(new Row("Est Value Bytes", extrapolatedValueBytes.toDisplay()));
		}
		rows.add(new Row("Est Table Size", extrapolatedBytes.toDisplay()));
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

	/*-------- column summary html ----------*/

	private DivTag makeColumnSummaryDiv(TableSummary tableSummary, long extrapolatedRowCount){
		List<ColumnSummary> rows = Scanner.of(tableSummary.columnSummaries())
				.sort(Comparator.comparing(ColumnSummary::name))
				.list();
		var table = makeColumnSummaryTableBuilder(extrapolatedRowCount).build(rows);
		return div(
				h5("Column Summary"),
				table);
	}

	private J2HtmlTable<ColumnSummary> makeColumnSummaryTableBuilder(long extrapolatedRowCount){
		return new J2HtmlTable<ColumnSummary>()
			.withClasses("sortable table table-sm table-striped border")
			.withColumn(
					"Column",
					ColumnSummary::name)
			.withColumn(
					"Name Bytes",
					columnSummary -> columnSummary.size().extrapolateTotalNameBytes(extrapolatedRowCount),
					byteLength -> NumberFormatter.addCommas(byteLength.toBytes()))
			.withColumn(
					"Avg Name Bytes",
					ColumnSummary::size,
					size -> NumberFormatter.addCommas(size.avgNameBytes().toBytes()))
			.withColumn(
					"Value Bytes",
					columnSummary -> columnSummary.size().extrapolateTotalValueBytes(extrapolatedRowCount),
					byteLength -> NumberFormatter.addCommas(byteLength.toBytes()))
			.withColumn(
					"Avg Value Bytes",
					ColumnSummary::size,
					size -> NumberFormatter.addCommas(size.avgValueBytes().toBytes()));
	}

}
