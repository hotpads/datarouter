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
package io.datarouter.web.browse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.email.DatarouterEmailService;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalInteger;
import io.datarouter.web.user.session.CurrentUserSessionInfo;
import io.datarouter.web.util.http.RequestTool;

public class ViewNodeDataHandler extends InspectNodeDataHandler{
	private static final Logger logger = LoggerFactory.getLogger(ViewNodeDataHandler.class);

	private static final String PARAM_outputBatchSize = "outputBatchSize";

	@Inject
	private DatarouterAdministratorEmailService administratorEmailService;
	@Inject
	private DatarouterEmailService emailService;
	@Inject
	private CurrentUserSessionInfo sessionInfo;
	@Inject
	private DatarouterProperties properties;

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
		int outputBatchSize = mav.put(PARAM_outputBatchSize, params.optionalInteger(PARAM_outputBatchSize).orElse(10));

		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		String startKeyString = RequestTool.get(request, PARAM_startKey, null);

		PK startKey = null;
		if(StringTool.notEmpty(startKeyString)){
			startKey = (PK)PrimaryKeyPercentCodec.decode(node.getFieldInfo().getPrimaryKeyClass(),
					startKeyString);
			mav.put(PARAM_startKey, PrimaryKeyPercentCodec.encode(startKey));
		}

		boolean startInclusive = true;
		Config config = new Config().setOutputBatchSize(outputBatchSize).setLimit(limit);
		Range<PK> range = new Range<>(startKey, startInclusive, null, true);
		List<D> databeans = sortedNode.scan(range, config)
				.list();

		addDatabeansToMav(mav, databeans);
		return mav;
	}

	@Handler
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Mav countKeys(
			OptionalInteger batchSize,
			OptionalInteger logBatchSize,
			OptionalInteger limit){
		showForm();
		if(!(node instanceof SortedStorageWriter<?,?>)){
			return new MessageMav("Cannot browse unsorted node");
		}
		@SuppressWarnings("unchecked")
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		Config config = new Config()
				.setOutputBatchSize(batchSize.orElse(1000))
				.setScannerCaching(false) //disabled due to BigTable bug?
				.setTimeout(1, TimeUnit.MINUTES)
				.setSlaveOk(true)
				.setNumAttempts(1);
		limit.ifPresent(config::setLimit);
		int printBatchSize = logBatchSize.orElse(100_000);
		long count = 0;
		PK last = null;
		long startMs = System.currentTimeMillis() - 1;
		long batchStartMs = System.currentTimeMillis() - 1;
		for(PK pk : sortedNode.scanKeys(config).iterable()){
			if(ComparableTool.lt(pk, last)){
				logger.warn("{} was < {}", pk, last);// shouldn't happen, but seems to once in 10mm times
			}
			++count;
			if(count % printBatchSize == 0){
				long batchMs = System.currentTimeMillis() - batchStartMs;
				double batchAvgRps = printBatchSize * 1000 / Math.max(1, batchMs);
				logger.warn("{} {} {} @{}rps",
						NumberFormatter.addCommas(count),
						node.getName(),
						pk.toString(),
						NumberFormatter.addCommas(batchAvgRps));
				batchStartMs = System.currentTimeMillis();
			}
			last = pk;
		}
		if(count < 1){
			return new MessageMav("no rows found");
		}
		long ms = System.currentTimeMillis() - startMs;
		DatarouterDuration duration = new DatarouterDuration(System.currentTimeMillis() - startMs,
				TimeUnit.MILLISECONDS);
		double avgRps = count * 1000 / ms;
		String message = String.format("finished counting %s at %s %s @%srps totalDuration=%s",
				node.getName(),
				NumberFormatter.addCommas(count),
				last.toString(),
				NumberFormatter.addCommas(avgRps),
				duration);
		logger.warn(message);
		String currentUser = sessionInfo.getUser(request).get().getUsername();
		String to = administratorEmailService.getAdministratorEmailAddressesCsv(currentUser);
		String emailMessage = buildMessage(node.getName(), NumberFormatter.addCommas(count), last.toString(),
				NumberFormatter.addCommas(avgRps), duration, currentUser);
		String subject = "Finished counting " + node.getName();
		try{
			emailService.sendEmail(currentUser, to, subject, emailMessage, true);
		}catch(MessagingException e){
			logger.warn(e.getMessage());
		}
		return new MessageMav(message);
	}


	private String buildMessage(String nodeName, String totalCount, String lastKey, String averageRps,
			DatarouterDuration duration, String userName){
		String bullet = " - ";
		String newLine = "<br>";
		StringBuilder builder = new StringBuilder();
		builder.append(bullet).append("node=").append(nodeName).append(newLine);
		builder.append(bullet).append("totalCount=").append(totalCount).append(newLine);
		builder.append(bullet).append("lastKey=").append(lastKey).append(newLine);
		builder.append(bullet).append("averageRps=").append(averageRps).append(newLine);
		builder.append(bullet).append("duration=").append(duration).append(newLine);
		builder.append(bullet).append("server=").append(properties.getServerName()).append(newLine);
		builder.append(bullet).append("triggeredBy=").append(userName).append(newLine);
		return builder.toString();
	}
}