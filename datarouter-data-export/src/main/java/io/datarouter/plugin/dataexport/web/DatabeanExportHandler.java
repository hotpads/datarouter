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

import io.datarouter.nodewatch.util.PhysicalSortedNodeWrapper;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportPaths;
import io.datarouter.plugin.dataexport.service.DatabeanExportChangelogService;
import io.datarouter.plugin.dataexport.service.DatabeanExportEmailService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.types.Ulid;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormText;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatabeanExportHandler extends BaseHandler{

	private static final String
			P_nodeName = "nodeName",
			P_nodeNames = "nodeNames",
			P_startKeyInclusive = "startKeyInclusive",
			P_endKeyExclusive = "endKeyExclusive",
			P_maxRows = "maxRows",
			P_scanBatchSize = "scanBatchSize",
			P_numThreads = "numThreads";

	public static final int DEFAULT_SCAN_BATCH_SIZE = Config.DEFAULT_REQUEST_BATCH_SIZE;
	public static final int DEFAULT_NUM_THREADS = 2;

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterDataExportPaths paths;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatabeanExportHandlerService databeanExportHandlerService;
	@Inject
	private DatabeanExportChangelogService changelogService;
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
		Ulid exportId = new Ulid();
		long numDatabeans = databeanExportHandlerService.exportNode(
				exportId,
				nodeName.orElseThrow(),
				startKeyInclusive,
				endKeyExclusive,
				maxRows,
				scanBatchSize,
				Optional.empty(),
				false);

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
		Ulid exportId = new Ulid();
		long numDatabeans = databeanExportHandlerService.exportNodes(
				exportId,
				nodeNameList,
				maxRows,
				scanBatchSize);

		// complete
		return complete(exportId, nodeNameList, numDatabeans);
	}

	@Handler
	private Mav parallel(
			@Param(P_nodeName) Optional<String> nodeName,
			@Param(P_startKeyInclusive) Optional<String> startKeyInclusive,
			@Param(P_endKeyExclusive) Optional<String> endKeyExclusive,
			@Param(P_scanBatchSize) Optional<String> scanBatchSize,
			@Param(P_numThreads) Optional<String> numThreads){
		boolean submitted = StringTool.notEmpty(nodeName.orElse(""));

		// show form
		HtmlForm form = makeFormParallel(
				nodeName,
				startKeyInclusive,
				endKeyExclusive,
				scanBatchSize,
				numThreads,
				submitted);
		if(!submitted || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Databean Export - Parallel")
					.withContent(DatabeanExportHtml.makeExportContent(
							paths.datarouter.dataExport.exportDatabeans.parallel,
							form))
					.buildMav();
		}

		// execute
		Ulid exportId = new Ulid();
		long numDatabeans = databeanExportHandlerService.exportNode(
				exportId,
				nodeName.orElseThrow(),
				startKeyInclusive,
				endKeyExclusive,
				Optional.empty(),
				scanBatchSize,
				numThreads,
				true);

		// complete
		return complete(exportId, List.of(nodeName.orElseThrow()), numDatabeans);
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
		List<String> possibleNodes = databeanExportHandlerService.scanPossibleNodeNames()
				.append("")
				.sort()
				.list();

		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Node Name")
				.withName(P_nodeName)
				.withValues(possibleNodes)
				.withSelected(nodeName.orElse(null));
		form.addField(makeScanBatchSizeField(scanBatchSize, shouldValidate));
		form.addField(makeMaxRowsField("Max Rows", maxRows, shouldValidate));
		form.addField(makeStartKeyInclusiveField(nodeName, startKeyInclusive, shouldValidate));
		form.addField(makeEndKeyExclusiveField(nodeName, endKeyExclusive, shouldValidate));
		form.addButtonWithoutSubmitAction()
				.withLabel("Export");
		return form;
	}

	private HtmlForm makeFormMultiTable(
			List<String> nodeNames,
			Optional<String> maxRows,
			Optional<String> scanBatchSize,
			boolean shouldValidate){
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addTextAreaField()
				.withLabel("Node Names (whitespace separated)")
				.withName(P_nodeNames)
				.withValue(
						String.join("\n", nodeNames),
						shouldValidate,
						this::validateNodeNames);
		form.addField(makeScanBatchSizeField(scanBatchSize, shouldValidate));
		form.addField(makeMaxRowsField("Max Rows Per Table", maxRows, shouldValidate));
		form.addButtonWithoutSubmitAction()
				.withLabel("Export");
		return form;
	}

	private HtmlForm makeFormParallel(
			Optional<String> nodeName,
			Optional<String> startKeyInclusive,
			Optional<String> endKeyExclusive,
			Optional<String> scanBatchSize,
			Optional<String> numThreads,
			boolean shouldValidate){
		List<String> possibleNodes = databeanExportHandlerService.scanPossibleNodeNames()
				.append("")
				.sort()
				.list();

		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Node Name")
				.withName(P_nodeName)
				.withValues(possibleNodes)
				.withSelected(nodeName.orElse(null));
		form.addField(makeScanBatchSizeField(scanBatchSize, shouldValidate));
		form.addField(makeNumThreadsField(numThreads, shouldValidate));
		form.addField(makeStartKeyInclusiveField(nodeName, startKeyInclusive, shouldValidate));
		form.addField(makeEndKeyExclusiveField(nodeName, endKeyExclusive, shouldValidate));
		form.addButtonWithoutSubmitAction()
				.withLabel("Export");
		return form;
	}

	/*--------- form fields -----------*/

	private HtmlFormText makeStartKeyInclusiveField(
			Optional<String> nodeName,
			Optional<String> startKeyInclusive,
			boolean shouldValidate){
		return new HtmlFormText()
				.withLabel("Start Key Inclusive")
				.withName(P_startKeyInclusive)
				.withPlaceholder("optional")
				.withValue(
						startKeyInclusive.orElse(null),
						shouldValidate && startKeyInclusive.isPresent(),
						stringKey -> validatePkString(nodeName.orElseThrow(), stringKey));
	}

	private HtmlFormText makeEndKeyExclusiveField(
			Optional<String> nodeName,
			Optional<String> endKeyExclusive,
			boolean shouldValidate){
		return new HtmlFormText()
				.withLabel("End Key Exclusive")
				.withName(P_endKeyExclusive)
				.withPlaceholder("optional")
				.withValue(
						endKeyExclusive.orElse(null),
						shouldValidate && endKeyExclusive.isPresent(),
						stringKey -> validatePkString(nodeName.orElseThrow(), stringKey));
	}

	private HtmlFormText makeScanBatchSizeField(
			Optional<String> scanBatchSize,
			boolean shouldValidate){
		return new HtmlFormText()
				.withLabel("Scan Batch Size")
				.withName(P_scanBatchSize)
				.withPlaceholder(Integer.toString(DEFAULT_SCAN_BATCH_SIZE))
				.withValue(
						scanBatchSize.orElse(""),
						shouldValidate && scanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
	}

	private HtmlFormText makeNumThreadsField(
			Optional<String> numThreads,
			boolean shouldValidate){
		return new HtmlFormText()
				.withLabel("Threads")
				.withName(P_numThreads)
				.withPlaceholder(Integer.toString(DEFAULT_NUM_THREADS))
				.withValue(
						numThreads.orElse(""),
						shouldValidate && numThreads.isPresent(),
						HtmlFormValidator::positiveInteger);
	}

	private HtmlFormText makeMaxRowsField(
			String display,
			Optional<String> maxRows,
			boolean shouldValidate){
		return new HtmlFormText()
				.withLabel(display)
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
				.concatOpt(this::validateNodeName)
				.findFirst();
	}

	private Optional<String> validateNodeName(String nodeName){
		boolean exists = databeanExportHandlerService.scanPossibleNodeNames()
				.anyMatch(nodeName::equals);
		return exists ? Optional.empty() : Optional.of("Can't find node named " + nodeName);
	}

	private Optional<String> validatePkString(String nodeName, String pkString){
		return new PhysicalSortedNodeWrapper<>(datarouterNodes, nodeName).validatePk(pkString);
	}

	/*--------- complete -----------*/

	private Mav complete(
			Ulid exportId,
			List<String> nodeNames,
			long numDatabeans){
		changelogService.record(
				exportId.toString(),
				"export",
				getSessionInfo().getRequiredSession().getUsername());
		DivTag emailAndBrowserContent = DatabeanExportHtml.makeExportCompleteContent(
				request.getContextPath() + paths.datarouter.dataExport.importDatabeans.toSlashedString(),
				exportId.toString(),
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
