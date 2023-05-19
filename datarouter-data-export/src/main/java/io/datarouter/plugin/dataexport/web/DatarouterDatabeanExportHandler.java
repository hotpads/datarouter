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
package io.datarouter.plugin.dataexport.web;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.plugin.dataexport.config.DatarouterDataExportPaths;
import io.datarouter.plugin.dataexport.service.DatabeanExportEmailService;
import io.datarouter.plugin.dataexport.service.DatabeanExportService;
import io.datarouter.plugin.dataexport.service.DatarouterDataExportChangelogService;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.types.Ulid;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlFormText;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class DatarouterDatabeanExportHandler extends BaseHandler{

	private static final String
			P_nodeName = "nodeName",
			P_nodeNames = "nodeNames",
			P_startKeyInclusive = "startKeyInclusive",
			P_endKeyExclusive = "endKeyExclusive",
			P_maxRows = "maxRows",
			P_scanBatchSize = "scanBatchSize";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterDataExportPaths paths;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatabeanExportService databeanExportService;
	@Inject
	private DatarouterDataExportChangelogService changelogService;
	@Inject
	private DatabeanExportEmailService emailService;

	@Handler
	private Mav singleTable(
			@Param(P_nodeName) Optional<String> nodeName,
			@Param(P_startKeyInclusive) Optional<String> startKeyInclusive,
			@Param(P_endKeyExclusive) Optional<String> endKeyExclusive,
			@Param(P_maxRows) Optional<String> maxRows,
			@Param(P_scanBatchSize) Optional<String> scanBatchSize){
		boolean submitted = StringTool.notEmpty(nodeName.orElse(""));

		// show form
		HtmlForm form = makeFormSingleTable(
				nodeName,
				startKeyInclusive,
				endKeyExclusive,
				maxRows,
				scanBatchSize,
				submitted);
		if(!submitted || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Databean Export - Single Table")
					.withContent(DatabeanExportHtml.makeExportContent(
							paths.datarouter.dataExport.exportDatabeans.singleTable,
							form))
					.buildMav();
		}

		// execute
		String exportId = Ulid.newValue();
		long numDatabeans = databeanExportService.exportNodeFromHandler(
				exportId,
				nodeName.orElseThrow(),
				startKeyInclusive,
				endKeyExclusive,
				maxRows,
				scanBatchSize);

		// complete
		return complete(exportId, List.of(nodeName.orElseThrow()), numDatabeans);
	}

	@Handler
	private Mav multiTable(
			@Param(P_nodeNames) Optional<String> nodeNames,
			@Param(P_maxRows) Optional<String> maxRows,
			@Param(P_scanBatchSize) Optional<String> scanBatchSize){
		boolean submitted = StringTool.notEmpty(nodeNames.orElse(""));

		// parse
		List<String> nodeNameList = nodeNamesToList(nodeNames.orElse(""));

		// show form
		HtmlForm form = makeFormMultiTable(
				nodeNameList,
				maxRows,
				scanBatchSize,
				submitted);
		if(!submitted || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Databean Export - Multi Table")
					.withContent(DatabeanExportHtml.makeExportContent(
							paths.datarouter.dataExport.exportDatabeans.multiTable,
							form))
					.buildMav();
		}

		// execute
		String exportId = Ulid.newValue();
		long numDatabeans = databeanExportService.exportNodesFromHandler(
				exportId,
				nodeNameList,
				maxRows,
				scanBatchSize);

		// complete
		return complete(exportId, nodeNameList, numDatabeans);
	}

	private List<String> nodeNamesToList(String nodeNamesString){
		String withoutNewLines = nodeNamesString.replace('\n', ' ');
		String[] tokens = withoutNewLines.split(" ");
		return Scanner.of(tokens)
				.map(String::trim)
				.exclude(String::isBlank)
				.list();
	}

	/*------------ form -------------*/

	private HtmlForm makeFormSingleTable(
			Optional<String> nodeName,
			Optional<String> startKeyInclusive,
			Optional<String> endKeyExclusive,
			Optional<String> maxRows,
			Optional<String> scanBatchSize,
			boolean shouldValidate){
		List<String> possibleNodes = databeanExportService.scanPossibleNodeNames()
				.append("")
				.sort()
				.list();

		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Node Name")
				.withName(P_nodeName)
				.withValues(possibleNodes)
				.withSelected(nodeName.orElse(null));
		form.addField(makeScanBatchSizeField(scanBatchSize, shouldValidate));
		form.addField(makeMaxRowsField("Max Rows", maxRows, shouldValidate));
		form.addTextField()
				.withDisplay("Start Key Inclusive")
				.withName(P_startKeyInclusive)
				.withPlaceholder("optional")
				.withValue(
						startKeyInclusive.orElse(null),
						shouldValidate && startKeyInclusive.isPresent(),
						stringKey -> validatePkString(nodeName.orElseThrow(), stringKey));
		form.addTextField()
				.withDisplay("End Key Exclusive")
				.withName(P_endKeyExclusive)
				.withPlaceholder("optional")
				.withValue(
						endKeyExclusive.orElse(null),
						shouldValidate && endKeyExclusive.isPresent(),
						stringKey -> validatePkString(nodeName.orElseThrow(), stringKey));
		form.addButtonWithoutSubmitAction()
				.withDisplay("Export");
		return form;
	}

	private HtmlForm makeFormMultiTable(
			List<String> nodeNames,
			Optional<String> maxRows,
			Optional<String> scanBatchSize,
			boolean shouldValidate){
		var form = new HtmlForm()
				.withMethod("post");
		form.addTextAreaField()
				.withDisplay("Node Names (whitespace separated)")
				.withName(P_nodeNames)
				.withValue(
						String.join("\n", nodeNames),
						shouldValidate,
						this::validateNodeNames);
		form.addField(makeScanBatchSizeField(scanBatchSize, shouldValidate));
		form.addField(makeMaxRowsField("Max Rows Per Table", maxRows, shouldValidate));
		form.addButtonWithoutSubmitAction()
				.withDisplay("Export");
		return form;
	}

	/*--------- form fields -----------*/

	private HtmlFormText makeScanBatchSizeField(
			Optional<String> scanBatchSize,
			boolean shouldValidate){
		return new HtmlFormText()
				.withDisplay("Scan Batch Size")
				.withName(P_scanBatchSize)
				.withPlaceholder(DatabeanExportService.DEFAULT_SCAN_BATCH_SIZE)
				.withValue(
						scanBatchSize.orElse(""),
						shouldValidate && scanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
	}

	private HtmlFormText makeMaxRowsField(
			String display,
			Optional<String> maxRows,
			boolean shouldValidate){
		return new HtmlFormText()
				.withDisplay(display)
				.withName(P_maxRows)
				.withPlaceholder("optional")
				.withValue(
						maxRows.orElse(""),
						shouldValidate && maxRows.isPresent(),
						HtmlFormValidator::positiveLong);
	}

	/*--------- form validate -----------*/

	private Optional<String> validateNodeNames(String nodeNames){
		return Scanner.of(nodeNamesToList(nodeNames))
				.map(this::validateNodeName)
				.concat(OptionalScanner::of)
				.findFirst();
	}

	private Optional<String> validateNodeName(String nodeName){
		boolean exists = databeanExportService.scanPossibleNodeNames()
				.anyMatch(nodeName::equals);
		return exists ? Optional.empty() : Optional.of("Can't find node named " + nodeName);
	}

	private Optional<String> validatePkString(String nodeName, String pkString){
		return new TypedNodeWrapper<>(datarouterNodes, nodeName).validatePk(pkString);
	}

	/*--------- complete -----------*/

	private Mav complete(
			String exportId,
			List<String> nodeNames,
			long numDatabeans){
		changelogService.record(
				exportId,
				"export",
				getSessionInfo().getRequiredSession().getUsername());
		DivTag emailAndBrowserContent = DatabeanExportHtml.makeExportCompleteContent(
				request.getContextPath() + paths.datarouter.dataExport.importDatabeans.toSlashedString(),
				exportId,
				nodeNames,
				numDatabeans);
		emailService.trySendExportCompleteEmail(
				getSessionInfo().getRequiredSession().getUsername(),
				emailAndBrowserContent);
		return pageFactory.startBuilder(request)
				.withTitle("Databean Export Complete")
				.withContent(emailAndBrowserContent)
				.buildMav();
	}

}
