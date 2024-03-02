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

import static j2html.TagCreator.body;
import static j2html.TagCreator.p;

import java.util.List;
import java.util.Optional;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.plugin.copytable.CopyTableService;
import io.datarouter.plugin.copytable.CopyTableService.CopyTableSpanResult;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class SingleThreadCopyTableHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_targetNodeName = "targetNodeName",
			P_lastKeyString = "lastKeyString",
			P_numThreads = "numThreads",
			P_scanBatchSize = "scanBatchSize",
			P_putBatchSize = "putBatchSize",
			P_skipInvalidDatabeans = "skipInvalidDatabeans",
			P_toEmail = "toEmail",
			P_submitAction = "submitAction";

	private static final int DEFAULT_NUM_THREADS = 4;
	private static final int DEFAULT_SCAN_BATCH_SIZE = 500;
	private static final int DEFAULT_PUT_BATCH_SIZE = 500;
	private static final boolean DEFAULT_SKIP_INVALID_DATABEANS = false;

	@Inject
	private CopyTableService copyTableService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CopyTableChangelogService changelogRecorderService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private TableSamplerService tableSamplerService;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceNodeName) Optional<String> sourceNodeName,
			@Param(P_targetNodeName) Optional<String> targetNodeName,
			@Param(P_lastKeyString) Optional<String> lastKeyString,
			@Param(P_toEmail) Optional<String> toEmail,
			@Param(P_numThreads) Optional<String> optNumThreads,
			@Param(P_scanBatchSize) Optional<String> optScanBatchSize,
			@Param(P_putBatchSize) Optional<String> optPutBatchSize,
			@Param(P_skipInvalidDatabeans) Optional<Boolean> skipInvalidDatabeans,
			@Param(P_submitAction) Optional<String> submitAction){
		boolean shouldValidate = submitAction.isPresent();
		List<String> possibleSourceNodes = tableSamplerService.scanAllSortedMapStorageNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		List<String> possibleTargetNodes = tableSamplerService.scanCountableNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Source Node Name")
				.withName(P_sourceNodeName)
				.withValues(possibleSourceNodes)
				.withSelected(sourceNodeName.orElse(null));
		form.addSelectField()
				.withLabel("Target Node Name")
				.withName(P_targetNodeName)
				.withValues(possibleTargetNodes)
				.withSelected(targetNodeName.orElse(null));
		form.addNumberField()
				.withLabel("Scan Batch Size")
				.withName(P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE)
				.withValue(
						optScanBatchSize.orElse(null),
						shouldValidate && optScanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addNumberField()
				.withLabel("Put Batch Size")
				.withName(P_putBatchSize)
				.withPlaceholder(DEFAULT_PUT_BATCH_SIZE)
				.withValue(
						optPutBatchSize.orElse(null),
						shouldValidate && optPutBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addTextField()
				.withLabel("Last Key String")
				//add validation
				.withName(P_lastKeyString)
				.withValue(lastKeyString.orElse(null));
		form.addNumberField()
				.withLabel("Num Threads")
				.withName(P_numThreads)
				.withPlaceholder(DEFAULT_NUM_THREADS)
				.withValue(
						optNumThreads.orElse(null),
						shouldValidate && optNumThreads.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addCheckboxField()
				.withLabel("Skip Invalid Databeans")
				.withName(P_skipInvalidDatabeans)
				.withChecked(DEFAULT_SKIP_INVALID_DATABEANS);
		form.addTextField()
				.withLabel("Email on Completion")
				//add validation
				.withName(P_toEmail)
				.withPlaceholder("you@email.com")
				.withValue(toEmail.orElse(getSessionInfo().getRequiredSession().getUsername()));
		form.addButton()
				.withLabel("Execute")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Copy Table - Single Thread")
					.withContent(CopyTableHtml.makeContent(
							paths.datarouter.copyTable.singleThread,
							form))
					.buildMav();
		}

		int numThreads = optNumThreads
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM_THREADS);
		int scanBatchSize = optScanBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		int putBatchSize = optPutBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_PUT_BATCH_SIZE);

		CopyTableSpanResult result = copyTableService.copyTableSpan(
				sourceNodeName.get(),
				targetNodeName.get(),
				lastKeyString.map(StringTool::nullIfEmpty).orElse(null),
				null,
				numThreads,
				scanBatchSize,
				putBatchSize,
				1,
				1,
				skipInvalidDatabeans.orElse(DEFAULT_SKIP_INVALID_DATABEANS));
		if(!result.success()){
			String message = String.format("The migration was interrupted unexpectedly with %s."
					+ "  Please resume the migration with lastKey %s",
					result.exception().getMessage(),
					result.resumeFromKeyString());
			return pageFactory.message(request, message);
		}
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		String message = String.format("Successfully migrated %s records from %s to %s",
				NumberFormatter.addCommas(result.numCopied()),
				sourceNodeName.get(),
				targetNodeName.get());
		var body = body(header, p(message));
		if(toEmail.filter(str -> !str.isEmpty()).isPresent()){
			String primaryHref = htmlEmailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.copyTable.singleThread)
					.build();
			var emailBuilder = htmlEmailService.startEmailBuilder()
					.withTitle("Copy Table")
					.withTitleHref(primaryHref)
					.withContent(body)
					.fromAdmin()
					.to(toEmail.get());
			htmlEmailService.trySendJ2Html(emailBuilder);
		}
		changelogRecorderService.recordChangelog(
				getSessionInfo(),
				"SingleThread",
				sourceNodeName.get(),
				targetNodeName.get());
		return pageFactory.message(request, message);
	}

}
