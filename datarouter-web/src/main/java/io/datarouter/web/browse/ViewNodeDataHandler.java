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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.util.http.RequestTool;

public class ViewNodeDataHandler extends InspectNodeDataHandler{
	private static final Logger logger = LoggerFactory.getLogger(ViewNodeDataHandler.class);

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
		limit = params.optionalInteger(PARAM_limit).orElse(100);
		mav.put(PARAM_limit, 100);
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		String startAfterKeyString = RequestTool.get(request, PARAM_startAfterKey, null);

		PK startAfterKey = null;
		if(StringTool.notEmpty(startAfterKeyString)){
			startAfterKey = (PK)PrimaryKeyPercentCodec.decode(node.getFieldInfo().getPrimaryKeyClass(),
					startAfterKeyString);
			mav.put(PARAM_startAfterKey, PrimaryKeyPercentCodec.encode(startAfterKey));
		}

		boolean startInclusive = true;
		Config config = new Config().setIterateBatchSize(10).setLimit(limit);
		Iterable<D> databeanIterable = sortedNode.scan(new Range<>(startAfterKey, startInclusive, null, true),
				config);
		List<D> databeans = ListTool.createArrayList(databeanIterable, limit);

		addDatabeansToMav(mav, databeans);
		return mav;
	}

	@Handler
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Mav countKeys(){
		showForm();
		if(!(node instanceof SortedStorageWriter<?,?>)){
			return new MessageMav("Cannot browse unsorted node");
		}
		@SuppressWarnings("unchecked")
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		int iterateBatchSize = params.optionalInteger("iterateBatchSize", 1000);
		Iterable<PK> iterable = sortedNode.scanKeys(null, new Config()
				.setIterateBatchSize(iterateBatchSize)
				.setScannerCaching(false) //disabled due to BigTable bug?
				.setTimeout(1, TimeUnit.MINUTES)
				.setNumAttempts(1));
		int printBatchSize = 1000;
		long count = 0;
		PK last = null;
		long startMs = System.currentTimeMillis() - 1;
		long batchStartMs = System.currentTimeMillis() - 1;
		for(PK pk : iterable){
			if(ComparableTool.lt(pk, last)){
				logger.warn(pk + " was < " + last);// shouldn't happen, but seems to once in 10mm times
			}
			++count;
			if(count > 0 && count % printBatchSize == 0){
				long batchMs = System.currentTimeMillis() - batchStartMs;
				double batchAvgRps = printBatchSize * 1000 / Math.max(1, batchMs);
				logger.warn(NumberFormatter.addCommas(count) + " " + pk.toString() + " @" + batchAvgRps + "rps");
				batchStartMs = System.currentTimeMillis();
			}
			last = pk;
		}
		if(count < 1){
			return new MessageMav("no rows found");
		}
		long ms = System.currentTimeMillis() - startMs;
		double avgRps = count * 1000 / ms;
		String message = "finished at " + NumberFormatter.addCommas(count) + " " + last.toString() + " @" + avgRps
				+ "rps";
		logger.warn(message);
		return new MessageMav(message);
	}

}