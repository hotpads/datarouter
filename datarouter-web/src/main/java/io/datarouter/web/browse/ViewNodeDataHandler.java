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
package io.datarouter.web.browse;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.CountKeysEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.pathnode.PathNode;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.op.scan.stride.StrideScanner.StrideScannerBuilder;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.types.MilliTime;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.DatarouterWebExecutors.CountKeysExecutor;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService.EmailHeaderRow;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.util.ExceptionTool;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;

public class ViewNodeDataHandler extends InspectNodeDataHandler{
	private static final Logger logger = LoggerFactory.getLogger(ViewNodeDataHandler.class);

	private static final String PARAM_responseBatchSize = "responseBatchSize";

	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private CountKeysEmailType countKeysEmailType;
	@Inject
	private CountKeysExecutor countKeysExec;

	@Override
	protected PathNode getFormPath(){
		return files.jsp.admin.viewNodeDataJsp;
	}

	@Override
	protected List<Field<?>> getFields(){
		return node.getFieldInfo().getFields();
	}

	@Override
	protected List<Field<?>> getKeyFields(){
		return node.getFieldInfo().getSampleDatabean().getKeyFields();
	}

	@SuppressWarnings("unchecked")
	@Handler
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Mav browseData(){
		Mav mav = showForm();
		if(!(node instanceof SortedStorageReader<?,?>)){
			return mav;
		}
		mav.put("browseSortedData", true);

		limit = mav.put(PARAM_limit, params.optionalInteger(PARAM_limit).orElse(100));
		int responseBatchSize = mav.put(
				PARAM_responseBatchSize,
				params.optionalInteger(PARAM_responseBatchSize).orElse(10));

		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		String startKeyString = RequestTool.get(request, PARAM_startKey, null);

		PK startKey = null;
		if(StringTool.notEmpty(startKeyString)){
			try{
				startKey = (PK)PrimaryKeyPercentCodecTool.decode(node.getFieldInfo().getPrimaryKeySupplier(),
						startKeyString);
				mav.put(PARAM_startKey, PrimaryKeyPercentCodecTool.encode(startKey));
			}catch(RuntimeException e){
				return new MessageMav(ExceptionTool.getStackTraceAsString(e));
			}
		}

		boolean startInclusive = true;
		Config config = new Config().setResponseBatchSize(responseBatchSize).setLimit(limit);
		Range<PK> range = new Range<>(startKey, startInclusive, null, true);
		sortedNode.scan(range, config)
				.flush(databeans -> addDatabeansToMav(mav, databeans));
		return mav;
	}

	@Handler
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Mav countKeys(
			Optional<Integer> batchSize,
			Optional<Integer> logBatchSize,
			Optional<Integer> limit,
			Optional<Boolean> useOffsetting,
			Optional<Integer> stride){
		showForm();
		if(!(node instanceof SortedStorageWriter<?,?>)){
			return pageFactory.message(request, "Cannot browse unsorted node");
		}
		PhysicalNode<?,?,?> physicalNode = NodeTool.extractSinglePhysicalNode(node);
		//TODO replace strings with more formal client detection
		boolean actualUseOffsetting = useOffsetting.orElse(physicalNode.getClientType().supportsOffsetSampling());
		@SuppressWarnings("unchecked")
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		var count = new AtomicLong();
		MilliTime startMs = MilliTime.now().minus(Duration.ofMillis(1));
		var lastKeyRef = new AtomicReference<PK>();
		if(actualUseOffsetting){
			var countingToolBuilder = new StrideScannerBuilder<>(sortedNode)
					.withLog(true);
			stride.ifPresent(countingToolBuilder::withStride);
			batchSize.ifPresent(countingToolBuilder::withBatchSize);
			long offsettingCount = countingToolBuilder.build()
					.findLast()
					.map(sample -> sample.totalCount)
					.orElse(0L);
			count.set(offsettingCount);
		}else{
			int responseBatchSize = batchSize.orElse(10_000);
			var config = new Config()
					.setResponseBatchSize(responseBatchSize);
			limit.ifPresent(config::setLimit);
			int printBatchSize = logBatchSize.orElse(100_000);
			var batchStartMs = new AtomicLong(System.currentTimeMillis() - 1);
			sortedNode.scanKeys(config)
					.prefetch(countKeysExec, responseBatchSize)
					.each($ -> count.incrementAndGet())
					.periodic(printBatchSize, pk -> {
						long batchMs = System.currentTimeMillis() - batchStartMs.get();
						double batchAvgRps = printBatchSize * 1000 / Math.max(1, batchMs);
						logger.warn(
								"{} {} {} @{}rps",
								NumberFormatter.addCommas(count),
								node.getName(),
								pk.toString(),
								NumberFormatter.addCommas(batchAvgRps));
						batchStartMs.set(System.currentTimeMillis());
					})
					.forEach(lastKeyRef::set);
		}
		if(count.get() < 1){
			return pageFactory.message(request, "no rows found");
		}
		MilliTime endMs = MilliTime.now();
		Duration duration = Duration.ofMillis(endMs.minus(startMs).toEpochMilli());
		var datarouterDuration = new DatarouterDuration(duration);
		double avgRps = count.get() * 1_000 / duration.toMillis();
		String message = String.format("finished counting %s at %s %s @%srps totalDuration=%s",
				node.getName(),
				NumberFormatter.addCommas(count),
				lastKeyRef.get() == null ? "?" : lastKeyRef.get().toString(),
				NumberFormatter.addCommas(avgRps),
				datarouterDuration);
		logger.warn(message);
		List<EmailHeaderRow> emailKvs = List.of(
				new EmailHeaderRow("node", node.getName()),
				new EmailHeaderRow("useOffsetting", actualUseOffsetting + ""),
				new EmailHeaderRow("stride", stride.map(Object::toString).orElse("default")),
				new EmailHeaderRow("totalCount", NumberFormatter.addCommas(count)),
				new EmailHeaderRow("lastKey", lastKeyRef.get() == null ? "?" : lastKeyRef.get().toString()),
				new EmailHeaderRow("averageRps", NumberFormatter.addCommas(avgRps)),
				new EmailHeaderRow("start", startMs.format(ZoneId.systemDefault())),
				new EmailHeaderRow("end", endMs.format(ZoneId.systemDefault())),
				new EmailHeaderRow("duration", datarouterDuration + ""),
				new EmailHeaderRow("triggeredBy", getSessionInfo().getRequiredSession().getUsername()));
		sendEmail(node.getName(), emailKvs);
		var dto = new DatarouterChangelogDtoBuilder(
				"Inspect Node Data",
				node.getName(),
				"countKeys",
				getSessionInfo().getRequiredSession().getUsername()).build();
		changelogRecorder.record(dto);
		return pageFactory.message(request, message);
	}

	private void sendEmail(String nodeName, List<EmailHeaderRow> kvs){
		String title = "Count Keys Result";
		var table = standardDatarouterEmailHeaderService.makeStandardHeaderWithSupplements(kvs);
		String primaryHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.nodes.browseData)
				.withParam("nodeName", nodeName)
				.build();
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withTitle(title)
				.withTitleHref(primaryHref)
				.withContent(table)
				.from(getSessionInfo().getRequiredSession().getUsername())
				.to(countKeysEmailType)
				.to(getSessionInfo().getRequiredSession().getUsername());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

}
