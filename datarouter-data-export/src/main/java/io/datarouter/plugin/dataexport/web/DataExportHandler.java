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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportDirectorySupplier;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportPrefetchExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportWriteParallelExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportFiles;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportPaths;
import io.datarouter.plugin.dataexport.service.DatabeanExport;
import io.datarouter.plugin.dataexport.service.DatabeanExportToDirectory;
import io.datarouter.plugin.dataexport.service.DatabeanImportService;
import io.datarouter.plugin.dataexport.storage.DataExportDao;
import io.datarouter.plugin.dataexport.storage.DataExportItem;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.op.raw.SortedStorage.SortedStorageNode;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import j2html.TagCreator;

public class DataExportHandler<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseHandler{

	private static final String
			PARAM_nodeName = "datarouterNodeName",
			PARAM_maxRows = "maxRows",
			PARAM_startAfterKey = "startAfterKeyString",
			PARAM_endBeforeKey = "endBeforeKeyString",
			PARAM_email = "email",
			PARAM_exportId = "exportId",
			PARAM_S3Key = "s3Key";

	private static final int PUT_BATCH_SIZE = 100;
	private static final int NUM_UPLOAD_THREADS = 4;

	@Inject
	private DataExportDao dataExportDao;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterDataExportFiles files;
	@Inject
	private DatarouterDataExportPaths paths;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatabeanImportService databeanImportService;
	@Inject
	private DatabeanExportPrefetchExecutor databeanExportPrefetchExec;
	@Inject
	private DatabeanExportWriteParallelExecutor databeanExportWriteParallelExec;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private DatarouterDataExportDirectorySupplier directorySupplier;

	@Handler
	public Mav showForm(){
		return new Mav(files.jsp.datarouter.plugin.export.dataExportJsp);
	}

	@Handler
	public Mav exportData(){
		Mav mav = new Mav(files.jsp.datarouter.plugin.export.showImportFormJsp);
		long exportId = makeExportId();
		Optional<String> toEmail = params.optionalNotEmpty(PARAM_email).map(String::trim);
		List<DataExportItem> nodeExportList = fetchParameters(exportId);
		String s3Key = null;
		var nodeListString = new StringBuilder();
		List<String> exports = new ArrayList<>();
		boolean appendedAnyNode = false;

		// exporting each node
		for(DataExportItem exportItem : nodeExportList){
			SortedStorageNode<?,?,?> node = (SortedStorageNode<?,?,?>)datarouterNodes.getNode(exportItem.getNodeName());
			if(node == null){
				return new Mav("Cannot find node " + exportItem.getNodeName());
			}

			var backup = new DatabeanExportToDirectory<>(
					directorySupplier.getDirectory(),
					Long.toString(exportId),
					node,
					DatabeanExport.DATABEAN_CONFIG,
					exportItem.getRange(),
					null,
					exportItem.getMaxRows(),
					databeanExportPrefetchExec,
					new Threads(databeanExportWriteParallelExec, NUM_UPLOAD_THREADS),
					new PhaseTimer());
			backup.execute();
			exports.add(exportItem.getNodeName() + " - " + NumberFormatter.addCommas(backup.getNumRecords())
					+ " records");
			s3Key = backup.getFullKey();
			if(appendedAnyNode){
				nodeListString.append(",");
			}
			appendedAnyNode = true;
			nodeListString.append(exportItem.getNodeName());
		}

		String localhostAndContext = "https://localhost:8443" + request.getContextPath();
		String localImportPath = localhostAndContext + "/datarouter/dataMigration?submitAction=localImport&exportId="
				+ exportId + "&s3Key=" + s3Key + "&nodeListString=" + nodeListString;

		// send email once export is complete
		var exportsDiv = div();
		Scanner.of(exports)
				.map(TagCreator::p)
				.forEach(exportsDiv::with);
		var resultHtml = div(
				standardDatarouterEmailHeaderService.makeStandardHeader(),
				p("Exported:"),
				exportsDiv,
				text("Please click "),
				a("here").withHref(localImportPath),
				text(" to import locally."));
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.dataMigration)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle("Export Complete")
				.withTitleHref(primaryHref)
				.withContent(resultHtml)
				.fromAdmin()
				.to(getSessionInfo().getRequiredSession().getUsername());
		toEmail.ifPresent(email -> Scanner.of(email.split(",")).forEach(emailBuilder::to));
		htmlEmailService.trySendJ2Html(emailBuilder);
		mav.put("localImportPath", localImportPath);
		mav.put("nodeExportList", nodeExportList);
		mav.put("exportResultHtml", resultHtml.renderFormatted());
		var dto = new DatarouterChangelogDtoBuilder(
				"DataMigration",
				nodeListString.toString(),
				"export",
				getSessionInfo().getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
		return mav;
	}

	@Handler
	public Mav localImport(){
		Mav mav = new Mav(files.jsp.datarouter.plugin.export.dataImportJsp);
		Long exportId = params.requiredLong(PARAM_exportId);
		String s3Key = params.required(PARAM_S3Key);
		String nodeListString = params.required("nodeListString");
		ArrayList<String> nodeList = StringTool.splitOnCharNoRegex(nodeListString, ',');
		List<String> nodes = new ArrayList<>();
		nodes.addAll(nodeList);
		mav.put(PARAM_exportId, exportId);
		mav.put(PARAM_S3Key, s3Key);
		mav.put("nodes", nodes);
		return mav;
	}

	@Handler
	public Mav importFromS3(){
		Mav mav = new Mav(files.jsp.datarouter.plugin.export.dataImportJsp);
		String importResult = "Imported:\n";

		String[] nodeNames = request.getParameterValues(PARAM_nodeName);
		Long exportId = params.requiredLong(PARAM_exportId);
		for(String nodeName : nodeNames){
			@SuppressWarnings("unchecked")
			MapStorageNode<PK,D,F> node = (MapStorageNode<PK,D,F>)datarouterNodes.getNode(nodeName);
			long numRecords = databeanImportService.importFromDirectory(
					directorySupplier.getDirectory(),
					makePathbeanKey(exportId, nodeName),
					node,
					PUT_BATCH_SIZE);
			importResult += nodeName + " - " + NumberFormatter.addCommas(numRecords) + " records\n";
		}
		mav.put("importResult", importResult);
		mav.put("nodes", nodeNames);
		var dto = new DatarouterChangelogDtoBuilder(
				"DataMigration",
				String.join(",", nodeNames),
				"localImport",
				getSessionInfo().getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
		return mav;
	}

	@Handler
	public Mav importS3KeyToNode(String s3Key, String nodeName){
		long exportIdParam = parseExportIdFromS3Key(s3Key);
		String nodeNameParam = parseNodeNameFromS3Key(s3Key);
		@SuppressWarnings("unchecked")
		MapStorageNode<PK,D,F> node = (MapStorageNode<PK,D,F>)datarouterNodes.getNode(nodeName);
		long numRecords = databeanImportService.importFromDirectory(
				directorySupplier.getDirectory(),
				makePathbeanKey(exportIdParam, nodeNameParam),
				node,
				PUT_BATCH_SIZE);
		var dto = new DatarouterChangelogDtoBuilder(
				"DataMigration",
				nodeName,
				"importS3KeyToNode",
				getSessionInfo().getRequiredSession().getUsername())
				.build();
		changelogRecorder.record(dto);
		return new MessageMav("imported " + numRecords);
	}

	// Assuming legacy format: migration/exportId/nodeName
	private long parseExportIdFromS3Key(String s3Key){
		return Long.valueOf(s3Key.split("/")[1]);
	}

	// Assuming legacy format: migration/exportId/nodeName
	private String parseNodeNameFromS3Key(String s3Key){
		return s3Key.split("/")[2];
	}

	private PathbeanKey makePathbeanKey(long exportId, String nodeName){
		return PathbeanKey.of(new Subpath(Long.toString(exportId)), nodeName);
	}

	private long makeExportId(){
		return RandomTool.nextPositiveLong();
	}

	private List<DataExportItem> fetchParameters(long exportId){
		String[] nodes = request.getParameterValues(PARAM_nodeName);
		String[] startKeys = request.getParameterValues(PARAM_startAfterKey);
		String[] endKeys = request.getParameterValues(PARAM_endBeforeKey);
		String[] maxRows = request.getParameterValues(PARAM_maxRows);
		int listSize = nodes.length;
		List<DataExportItem> dataExportItemList = new ArrayList<>();
		for(int i = 0; i < listSize; i++){
			String startKey = startKeys[i] == null || startKeys[i].length() <= 0 ? null : startKeys[i];
			String endKey = endKeys[i] == null || endKeys[i].length() <= 0 ? null : endKeys[i];
			var dataExportItem = new DataExportItem(
					exportId,
					i,
					nodes[i],
					startKey,
					endKey,
					convertMaxRowsToLong(maxRows[i]));
			dataExportItemList.add(dataExportItem);
		}
		dataExportDao.putMulti(dataExportItemList);
		return dataExportItemList;
	}

	private long convertMaxRowsToLong(String maxRowString){
		if(StringTool.isEmpty(maxRowString)){
			return Long.MAX_VALUE;
		}
		return Long.parseLong(maxRowString);
	}

}
