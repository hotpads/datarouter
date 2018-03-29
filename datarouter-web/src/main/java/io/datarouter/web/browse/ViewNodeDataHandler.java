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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.node.op.raw.write.SortedStorageWriter;
import io.datarouter.storage.util.PrimaryKeyPercentCodec;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Range;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.util.http.RequestTool;

public class ViewNodeDataHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(ViewNodeDataHandler.class);

	private static final Integer MIN_FIELD_ABBREVIATION_LENGTH = 2;

	private static final String
			PARAM_backKey = "backKey",
			PARAM_startAfterKey = "startAfterKey",
			PARAM_nextKey = "nextKey",
			PARAM_limit = "limit";

	@Inject
	private DatarouterNodes nodes;
	@Inject
	private DatarouterWebFiles files;


	private Node<?,?,?> node;
	private Integer limit;

	@Handler(defaultHandler = true)
	protected Mav showDashboard(){
		Mav mav = new Mav(files.jsp.admin.viewNodeDataJsp);
		String nodeName = RequestTool.get(request, "nodeName");
		node = nodes.getNode(nodeName);
		if(node == null){
			return new MessageMav("Cannot find node " + nodeName);
		}
		mav.put("node", node);
		List<Field<?>> fields = node.getFields();
		mav.put("fields", fields);
		return mav;
	}

	@SuppressWarnings("unchecked")
	@Handler
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Mav browseData(){
		Mav mav = showDashboard();
		if(!(node instanceof SortedStorageReader<?,?>)){
			return mav;
		}
		mav.put("browseSortedData", true);
		limit = RequestTool.getIntegerAndPut(request, PARAM_limit, 100, mav);
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		String backKeyString = RequestTool.get(request, PARAM_backKey, null);// allows for 1 "back" action
		mav.put(PARAM_backKey, backKeyString);
		String startAfterKeyString = RequestTool.get(request, PARAM_startAfterKey, null);

		PK startAfterKey = null;
		if(StringTool.notEmpty(startAfterKeyString)){
			startAfterKey = (PK)PrimaryKeyPercentCodec.decode(node.getPrimaryKeyType(), startAfterKeyString);
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
		showDashboard();
		if(!(node instanceof SortedStorageWriter<?,?>)){
			return new MessageMav("Cannot browse unsorted node");
		}
		@SuppressWarnings("unchecked")
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		int iterateBatchSize = params.optionalInteger("iterateBatchSize", 1000);
		Iterable<PK> iterable = sortedNode.scanKeys(null, new Config()
				.setIterateBatchSize(iterateBatchSize));
//				.setScannerCaching(false) //disabled due to BigTable bug?
//				.setTimeout(1, TimeUnit.MINUTES)
//				.setNumAttempts(1));
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

	private <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> void addDatabeansToMav(Mav mav, List<D> databeans){
		mav.put("databeans", databeans);
		List<List<Field<?>>> rowsOfFields = new ArrayList<>();
		List<String> fieldKeysAndValues = new ArrayList<>();
		@SuppressWarnings("unchecked")
		DatabeanFielder<PK,D> fielder = (DatabeanFielder<PK,D>)node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			for(D databean : IterableTool.nullSafe(databeans)){
				rowsOfFields.add(fielder.getFields(databean));
				List<Field<?>> databeanFieldKeys = databean.getKeyFields();
				String databeanFieldKey = "";
				for(Field<?> field : databeanFieldKeys){
					databeanFieldKey += "&" + field.getKey().getName() + "=" + field.getValueString();
				}
				fieldKeysAndValues.add(databeanFieldKey);
			}
			mav.put("rowsOfFields", rowsOfFields);
			mav.put("fieldKeys", fieldKeysAndValues);
		}
		mav.put("abbreviatedFieldNameByFieldName", getFieldAbbreviationByFieldName(fielder, databeans));
		if(CollectionTool.size(databeans) >= limit){
			mav.put(PARAM_nextKey, PrimaryKeyPercentCodec.encode(CollectionTool.getLast(databeans).getKey()));
		}
	}

	private <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Map<String,String> getFieldAbbreviationByFieldName(
			DatabeanFielder<PK,D> fielder, Collection<? extends D> databeans){
		if(CollectionTool.isEmpty(databeans)){
			return new HashMap<>();
		}
		D first = IterableTool.first(databeans);
		List<String> fieldNames = FieldTool.getFieldNames(fielder.getFields(first));
		List<Integer> maxLengths = ListTool.createArrayListAndInitialize(fieldNames.size());
		Collections.fill(maxLengths, 0);

		for(D d : IterableTool.nullSafe(databeans)){
			List<?> values = FieldTool.getFieldValues(fielder.getFields(d));
			for(int i = 0; i < CollectionTool.size(values); ++i){
				int length = values.get(i) == null ? 0 : StringTool.length(values.get(i).toString());
				if(length > maxLengths.get(i)){
					maxLengths.set(i, length);
				}
			}
		}

		Map<String,String> abbreviatedNames = new HashMap<>();
		for(int i = 0; i < maxLengths.size(); ++i){
			int length = maxLengths.get(i);
			if(length < MIN_FIELD_ABBREVIATION_LENGTH){
				length = MIN_FIELD_ABBREVIATION_LENGTH;
			}
			String abbreviated = fieldNames.get(i);
			if(length < StringTool.length(fieldNames.get(i))){
				abbreviated = fieldNames.get(i).substring(0, length);
			}
			abbreviatedNames.put(fieldNames.get(i), abbreviated);
		}
		return abbreviatedNames;
	}

}
