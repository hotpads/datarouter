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
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessor;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorRegistry;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorService;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorService.TableProcessorSpanResult;
import io.datarouter.storage.config.Config;
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

public class SingleThreadTableProcessorHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_lastKeyString = "lastKeyString",
			P_scanBatchSize = "scanBatchSize",
			P_processorName = "processorName",
			P_toEmail = "toEmail",
			P_submitAction = "submitAction";

	private static final int DEFAULT_SCAN_BATCH_SIZE = Config.DEFAULT_RESPONSE_BATCH_SIZE;

	@Inject
	private TableProcessorService service;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private TableProcessorRegistry processorRegistry;
	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CopyTableChangelogService changelogService;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private TableSamplerService tableSamplerService;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceNodeName) Optional<String> sourceName,
			@Param(P_lastKeyString) Optional<String> lastKeyString,
			@Param(P_scanBatchSize) Optional<String> scanBatchSize,
			@Param(P_processorName) Optional<String> processorName,
			@Param(P_toEmail) Optional<String> toEmail,
			@Param(P_submitAction) Optional<String> submitAction){
		boolean shouldValidate = submitAction.isPresent();
		List<String> possibleNodes = tableSamplerService.scanAllSortedMapStorageNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		List<String> possibleProcessors = processorRegistry.scan()
				.map(TableProcessor::getClass)
				.map(Class::getSimpleName)
				.append("")
				.sort()
				.list();
		var form = new HtmlForm(HtmlFormMethod.POST);
		form.addSelectField()
				.withLabel("Processor Name")
				.withName(P_processorName)
				.withValues(possibleProcessors);
		form.addSelectField()
				.withLabel("Node Name")
				.withName(P_sourceNodeName)
				.withValues(possibleNodes);
		form.addNumberField()
				.withLabel("Scan Batch Size")
				.withName(P_scanBatchSize)
				.withPlaceholder(DEFAULT_SCAN_BATCH_SIZE)
				.withValue(
						scanBatchSize.orElse(null),
						shouldValidate && scanBatchSize.isPresent(),
						HtmlFormValidator::positiveInteger);
		form.addTextField()
				.withLabel("From Key String")
				//add validation
				.withName(P_lastKeyString)
				.withValue(lastKeyString.orElse(null));
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
					.withTitle("Table Processor - Single Thread")
					.withContent(TableProcessorHtml.makeContent(
							paths.datarouter.tableProcessor.singleThread,
							form))
					.buildMav();
		}

		int actualScanBatchSize = scanBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_SCAN_BATCH_SIZE);
		TableProcessor<?> processor = processorRegistry.find(processorName.get()).get();
		TableProcessorSpanResult result = service.runTableProcessor(
				sourceName.get(),
				lastKeyString.map(StringTool::nullIfEmpty).orElse(null),
				null,
				actualScanBatchSize,
				processor,
				1,
				1);
		if(!result.success()){
			String message = String.format("The table processor was interrupted unexpectedly with %s."
					+ "  Please resume the processor with lastKey %s",
					result.exception().getMessage(),
					result.resumeFromKeyString());
			return pageFactory.message(request, message);
		}
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		String message = String.format("Successfully processed %s records for %s - %s",
				NumberFormatter.addCommas(result.numScanned()),
				sourceName.get(),
				processorName.get());
		var body = body(header, p(message));
		if(toEmail.filter(str -> !str.isEmpty()).isPresent()){
			String primaryHref = htmlEmailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.copyTable.singleThread)
					.build();
			var emailBuilder = htmlEmailService.startEmailBuilder()
					.withTitle("Table Processor")
					.withTitleHref(primaryHref)
					.withContent(body)
					.fromAdmin()
					.to(toEmail.get());
			htmlEmailService.trySendJ2Html(emailBuilder);
		}
		changelogService.recordChangelogForTableProcessor(
				getSessionInfo(),
				"Single Thread",
				sourceName.get(),
				processorName.get());
		return pageFactory.message(request, message);
	}

}
