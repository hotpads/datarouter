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
import io.datarouter.plugin.copytable.link.SingleThreadCopyTableLink;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class SingleThreadCopyTableHandler extends BaseHandler{

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

	@Handler
	private <PK extends PrimaryKey<PK>, D extends Databean<PK,D>>
	Mav singleThread(SingleThreadCopyTableLink link){
		Optional<String> sourceNodeName = link.sourceNodeName;
		Optional<String> targetNodeName = link.targetNodeName;
		Optional<Integer> optScanBatchSize = link.scanBatchSize;
		Optional<Integer> optPutBatchSize = link.putBatchSize;
		Optional<String> lastKeyString = link.lastKeyString;
		Optional<Integer> optNumThreads = link.numThreads;
		Optional<Boolean> skipInvalidDatabeans = link.skipInvalidDatabeans;
		Optional<String> submitAction = link.submitAction;
		Optional<String> toEmail = link.toEmail;

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
				.withName(SingleThreadCopyTableLink.P_sourceNodeName)
				.withValues(possibleSourceNodes)
				.withSelected(sourceNodeName.orElse(null));
		form.addSelectField()
				.withLabel("Target Node Name")
				.withName(SingleThreadCopyTableLink.P_targetNodeName)
				.withValues(possibleTargetNodes)
				.withSelected(targetNodeName.orElse(null));
		form.addNumberField()
				.withLabel("Scan Batch Size")
				.withName(SingleThreadCopyTableLink.P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE)
				.withValue(
						optScanBatchSize.map(String::valueOf).orElse(null),
						shouldValidate && optScanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addNumberField()
				.withLabel("Put Batch Size")
				.withName(SingleThreadCopyTableLink.P_putBatchSize)
				.withPlaceholder(DEFAULT_PUT_BATCH_SIZE)
				.withValue(
						optPutBatchSize.map(String::valueOf).orElse(null),
						shouldValidate && optPutBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addTextField()
				.withLabel("Last Key String")
				//add validation
				.withName(SingleThreadCopyTableLink.P_lastKeyString)
				.withValue(lastKeyString.orElse(null));
		form.addNumberField()
				.withLabel("Num Threads")
				.withName(SingleThreadCopyTableLink.P_numThreads)
				.withPlaceholder(DEFAULT_NUM_THREADS)
				.withValue(
						optNumThreads.map(String::valueOf).orElse(null),
						shouldValidate && optNumThreads.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addCheckboxField()
				.withLabel("Skip Invalid Databeans")
				.withName(SingleThreadCopyTableLink.P_skipInvalidDatabeans)
				.withChecked(DEFAULT_SKIP_INVALID_DATABEANS);
		form.addTextField()
				.withLabel("Email on Completion")
				//add validation
				.withName(SingleThreadCopyTableLink.P_toEmail)
				.withPlaceholder("you@email.com")
				.withValue(toEmail.orElse(getSessionInfo().getRequiredSession().getUsername()));
		form.addButton()
				.withLabel("Execute")
				.withValue("singleThread");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Copy Table - Single Thread")
					.withContent(CopyTableHtml.makeContent(
							paths.datarouter.copyTable.singleThread,
							form))
					.buildMav();
		}

		int numThreads = optNumThreads
				.orElse(DEFAULT_NUM_THREADS);
		int scanBatchSize = optScanBatchSize
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		int putBatchSize = optPutBatchSize
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
