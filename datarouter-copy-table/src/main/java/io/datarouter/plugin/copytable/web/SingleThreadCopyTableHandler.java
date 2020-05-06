/**
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

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.p;

import java.util.Objects;

import javax.inject.Inject;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.plugin.copytable.CopyTableConfiguration;
import io.datarouter.plugin.copytable.CopyTableService;
import io.datarouter.plugin.copytable.CopyTableService.CopyTableSpanResult;
import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.ContainerTag;

public class SingleThreadCopyTableHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_targetNodeName = "targetNodeName",
			P_filterName = "filterName",
			P_autoResume = "autoResume",
			P_lastKeyString = "lastKeyString",
			P_numThreads = "numThreads",
			P_putBatchSize = "putBatchSize",
			P_toEmail = "toEmail",
			P_submitAction = "submitAction";

	private static final int DEFAULT_NUM_THREADS = 4;
	private static final int DEFAULT_BATCH_SIZE = 1_000;
	private static final boolean PERSISTENT_PUT = false;

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private CopyTableService copyTableService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private CopyTableConfiguration copyTableConfiguration;
	@Inject
	private DatarouterCopyTablePaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private CopyTableChangelogRecorderService changelogRecorderService;

	@Handler(defaultHandler = true)
	private <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	Mav defaultHandler(
			@Param(P_sourceNodeName) OptionalString sourceNodeName,
			@Param(P_targetNodeName) OptionalString targetNodeName,
			@Param(P_filterName) OptionalString filterName,
			@Param(P_autoResume) OptionalBoolean autoResume,
			@Param(P_lastKeyString) OptionalString lastKeyString,
			@Param(P_toEmail) OptionalString toEmail,
			@Param(P_numThreads) OptionalString numThreads,
			@Param(P_putBatchSize) OptionalString putBatchSize,
			@Param(P_submitAction) OptionalString submitAction){
		String errorSourceNode = null;
		String errorTargetNode = null;
		String errorFilterName = null;
		String errorNumThreads = null;
		String errorPutBatchSize = null;

		if(submitAction.isPresent()){
			try{
				Objects.requireNonNull(nodes.getNode(sourceNodeName.get()));
			}catch(Exception e){
				errorSourceNode = StringTool.isEmpty(sourceNodeName.get())
						? "Please specify source node"
						: "Unknown sourceNode: " + sourceNodeName.get();
			}
			try{
				Objects.requireNonNull(nodes.getNode(targetNodeName.get()));
			}catch(Exception e){
				errorTargetNode = StringTool.isEmpty(targetNodeName.get())
						? "Please specify target node"
						: "Unknown targetNode: " + targetNodeName.get();
			}
			try{
				copyTableConfiguration.getValidFilter(filterName);
			}catch(Exception e){
				errorFilterName = "Unknown filter: " + filterName.get();
			}
			try{
				if(numThreads.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(numThreads.get());
				}
			}catch(Exception e){
				errorNumThreads = "Please specify an integer";
			}
			try{
				if(putBatchSize.map(StringTool::nullIfEmpty).isPresent()){
					Integer.valueOf(putBatchSize.get());
				}
			}catch(Exception e){
				errorPutBatchSize = "Please specify an integer";
			}
		}

		var form = new HtmlForm()
				.withMethod("post");
		form.addTextField()
				.withDisplay("Source Node Name")
				.withError(errorSourceNode)
				.withName(P_sourceNodeName)
				.withPlaceholder("client.TableName")
				.withValue(sourceNodeName.orElse(null));
		form.addTextField()
				.withDisplay("Target Node Name")
				.withError(errorTargetNode)
				.withName(P_targetNodeName)
				.withPlaceholder("client.TableName")
				.withValue(targetNodeName.orElse(null));
		form.addTextField()
				.withDisplay("Filter Name")
				.withError(errorFilterName)
				.withName(P_filterName)
				.withPlaceholder("filterName")
				.withValue(filterName.orElse(null));
		form.addCheckboxField()
				.withDisplay("Auto-resume")
				.withName(P_autoResume)
				.withChecked(autoResume.orElse(false));
		form.addTextField()
				.withDisplay("Last Key String")
				//add validation
				.withName(P_lastKeyString)
				.withValue(lastKeyString.orElse(null));
		form.addTextField()
				.withDisplay("Num Threads")
				.withError(errorNumThreads)
				.withName(P_numThreads)
				.withPlaceholder(DEFAULT_NUM_THREADS + "")
				.withValue(numThreads.orElse(null));
		form.addTextField()
				.withDisplay("Batch Size")
				.withError(errorPutBatchSize)
				.withName(P_putBatchSize)
				.withPlaceholder(DEFAULT_BATCH_SIZE + "")
				.withValue(putBatchSize.orElse(null));
		form.addTextField()
				.withDisplay("Email on Completion")
				//add validation
				.withName(P_toEmail)
				.withPlaceholder("you@email.com")
				.withValue(toEmail.orElse(null));
		form.addButton()
				.withDisplay("Copy")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Copy Table - Single Thread")
					.withContent(Html.makeContent(form))
					.buildMav();
		}

		int actualNumThreads = numThreads
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM_THREADS);
		int actualPutBatchSize = putBatchSize
				.map(StringTool::nullIfEmpty)
				.map(Integer::valueOf)
				.orElse(DEFAULT_BATCH_SIZE);

		CopyTableSpanResult result = copyTableService.copyTableSpan(
				sourceNodeName.get(),
				targetNodeName.get(),
				lastKeyString.map(StringTool::nullIfEmpty).orElse(null),
				null,
				copyTableConfiguration.getValidFilter(filterName),
				copyTableConfiguration.findProcessor(null).orElse(null),
				autoResume.orElse(false),
				actualNumThreads,
				actualPutBatchSize,
				PERSISTENT_PUT,
				1,
				1);
		if(!result.success){
			String message = String.format("The migration was interrupted unexpectedly with %s."
					+ "  Please resume the migration with lastKey %s",
					result.exception.getMessage(),
					result.resumeFromKeyString);
			return pageFactory.message(request, message);
		}
		String message = String.format("Successfully migrated %s records from %s to %s",
				NumberFormatter.addCommas(result.numCopied),
				sourceNodeName.get(),
				targetNodeName.get());
		if(!toEmail.get().isEmpty()){
			String fromEmail = datarouterProperties.getAdministratorEmail();
			String primaryHref = htmlEmailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.copyTableSingleThread)
					.build();
			var emailBuilder = htmlEmailService.startEmailBuilder()
					.withTitle("Copy Table")
					.withTitleHref(primaryHref)
					.withContent(p(message));
			htmlEmailService.trySendJ2Html(fromEmail, toEmail.get(), emailBuilder);
		}
		changelogRecorderService.recordChangelog(getSessionInfo(), "Single Thread", sourceNodeName.get(), targetNodeName
				.get());
		return pageFactory.message(request, message);
	}

	private static class Html{

		public static ContainerTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Copy Table - Single Thread"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
