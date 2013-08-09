package com.hotpads.handler.datarouter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import junit.framework.Assert;

import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.NumberFormatter;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.java.ReflectionTool;

public class ViewNodeDataHandler<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseHandler{	
	
	private DataRouterContext drContext;
	private Node<?,?> node;
	

	@Inject
	public ViewNodeDataHandler(DataRouterContext drContext){
		this.drContext = drContext;
	}
	

	public static final String
		ACTION_summary = "summary",
		ACTION_countRows = "countRows",
		ACTION_countKeys = "countKeys",
		ACTION_countCells = "countKeys",
		ACTION_browseData = "browseData",
		
		PARAM_routerName = "routerName",
		PARAM_nodeName = "nodeName",
		
		PARAM_backKey = "backKey",
		PARAM_startAfterKey = "startAfterKey",
		PARAM_nextKey = "nextKey",
		PARAM_limit = "limit",
		PARAM_offset = "offset";
	
	private Mav preHandle(){
		Mav mav = new Mav("/admin/datarouter/data/viewNodeData");
		String nodeName = RequestTool.get(request, PARAM_nodeName);
		node = drContext.getNodes().getNode(nodeName);
		if(node==null){
			return new MessageMav("Cannot find node "+nodeName);
		}
		mav.put("node", node);
		return mav;
	}
	
	@Override
	protected Mav handleDefault(){
		return preHandle();
	}
	
	@Handler
	public Mav countRows(){
		Mav mav = preHandle();
		if( ! (node instanceof SortedStorageWriter<?,?>)){
			return new MessageMav("Cannot browse unsorted node");
		}
		SortedStorageReaderNode<PK,D> sortedNode = (SortedStorageReaderNode<PK,D>)node;
		int iterateBatchSize = 5000;
		Iterable<D> iterable = sortedNode.scan(null, true, null, true, 
				new Config().setIterateBatchSize(iterateBatchSize)
						.setScannerCaching(false)
						.setTimeout(10, TimeUnit.SECONDS)
						.setNumAttempts(5));
		int printBatchSize = 10000;
		long count = 0;
		D last = null;
		long startMs = System.currentTimeMillis() - 1;
		long batchStartMs = System.currentTimeMillis() - 1;
		for(D d : iterable){
			if(ComparableTool.lt(d, last)){
				throw new RuntimeException(count+":"+d+" <= "+last);
			}
			++count;
			if(count > 0 && count % printBatchSize == 0){
				long batchMs = System.currentTimeMillis() - batchStartMs;
				double batchAvgRps = printBatchSize * 1000 / batchMs;
				logger.warn(NumberFormatter.addCommas(count)+" "+d.toString()+" @"+batchAvgRps+"rps");
				batchStartMs = System.currentTimeMillis();
			}
			last = d;
		}
		if(count<1){ return new MessageMav("no rows found"); }
		long ms = System.currentTimeMillis() - startMs;
		double avgRps = count * 1000 / ms;
		String message = "finished at "+NumberFormatter.addCommas(count)+" "+last.toString()+" @"+avgRps+"rps";
		logger.warn(message);
		return new MessageMav(message);
	}
		
	@Handler
	public Mav countKeys(){
		Mav mav = preHandle();
		if( ! (node instanceof SortedStorageWriter<?,?>)){
			return new MessageMav("Cannot browse unsorted node");
		}
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		int iterateBatchSize = 10000;
		Iterable<PK> iterable = sortedNode.scanKeys(null, true, null, true, 
				new Config().setIterateBatchSize(iterateBatchSize)
						.setScannerCaching(false)
						.setTimeout(10, TimeUnit.SECONDS)
						.setNumAttempts(5));
		int printBatchSize = 10000;
		long count = 0;
		PK last = null;
		long startMs = System.currentTimeMillis() - 1;
		long batchStartMs = System.currentTimeMillis() - 1;
		for(PK pk : iterable){
			Assert.assertTrue(ComparableTool.gt(pk, last));
			++count;
			if(count > 0 && count % printBatchSize == 0){
				long batchMs = System.currentTimeMillis() - batchStartMs;
				double batchAvgRps = printBatchSize * 1000 / batchMs;
				logger.warn(NumberFormatter.addCommas(count)+" "+pk.toString()+" @"+batchAvgRps+"rps");
				batchStartMs = System.currentTimeMillis();
			}
			last = pk;
		}
		if(count<1){ return new MessageMav("no rows found"); }
		long ms = System.currentTimeMillis() - startMs;
		double avgRps = count * 1000 / ms;
		String message = "finished at "+NumberFormatter.addCommas(count)+" "+last.toString()+" @"+avgRps+"rps";
		logger.warn(message);
		return new MessageMav(message);
	}
		
	@Handler
	public Mav browseData(){
		Mav mav = preHandle();
		if( ! (node instanceof SortedStorageWriter<?,?>)){
			return new MessageMav("Cannot browse unsorted node");
		}
		mav.put("fields", node.getFields());
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		String backKeyString = RequestTool.get(request, PARAM_backKey, null);//allows for 1 "back" action
		mav.put(PARAM_backKey, backKeyString);
		String startAfterKeyString = RequestTool.get(request, PARAM_startAfterKey, null);
		
		Integer limit = RequestTool.getInteger(request, PARAM_limit, 100);
		mav.put(PARAM_limit, limit);
		
		Integer offset = RequestTool.getInteger(request, PARAM_offset, 0);//offset is for display purposes only
		mav.put(PARAM_offset, offset);

		Config config = new Config();
		PK startAfterKey = null;
		if(StringTool.notEmpty(startAfterKeyString)){
			startAfterKey = (PK)ReflectionTool.create(node.getPrimaryKeyType());
			startAfterKey.fromPersistentString(startAfterKeyString);
			config.setStartId(startAfterKey);
			mav.put(PARAM_startAfterKey, startAfterKey.getPersistentString());
		}
		config.setLimit(limit);
		boolean startInclusive = startAfterKey==null;
		List<? extends Databean<?,?>> databeans = sortedNode.getRange(
				(PK)startAfterKey, startInclusive, null, true, config);
		mav.put("databeans", databeans);
		
		List<List<Field<?>>> rowsOfFields = ListTool.create();
		DatabeanFielder fielder = node.getFieldInfo().getSampleFielder();
		if(fielder!=null){
			for(Databean<?,?> databean : IterableTool.nullSafe(databeans)){
				FieldSet<?> fieldSet = (FieldSet<?>)databean;
				List<Field<?>> rowOfFields = fielder.getFields(fieldSet);//assumes there are no missing fields.  should use a Map instead
				rowsOfFields.add(rowOfFields);
			}
			mav.put("rowsOfFields", rowsOfFields);
		}
		
		mav.put("abbreviatedFieldNameByFieldName", getFieldAbbreviationByFieldName(databeans));
		if(CollectionTool.size(databeans)>=limit){
			mav.put(PARAM_nextKey, CollectionTool.getLast(databeans).getPersistentString());
		}
		return mav;
	}
	
	public static final Integer MIN_FIELD_ABBREVIATION_LENGTH = 2;
	
	protected Map<String,String> getFieldAbbreviationByFieldName(Collection<? extends Databean<?,?>> databeans){
		if(CollectionTool.isEmpty(databeans)){ return MapTool.create(); }
		Databean<?,?> first = IterableTool.first(databeans);
		List<Integer> maxLengths = ListTool.createArrayListAndInitialize(first.getFieldNames().size());
		Collections.fill(maxLengths, 0);
		for(Databean<?,?> d : IterableTool.nullSafe(databeans)){
			List<?> values = d.getFieldValues();
			for(int i=0; i < CollectionTool.size(values); ++i){
				int length = values.get(i)==null?0:StringTool.length(values.get(i).toString());
				if(length > maxLengths.get(i)){
					maxLengths.set(i, length);
				}
			}
		}
		List<String> fieldNames = first.getFieldNames();
		Map<String,String> abbreviatedNames = MapTool.create();
		for(int i=0; i < maxLengths.size(); ++i){
			int length = maxLengths.get(i);
			if(length < MIN_FIELD_ABBREVIATION_LENGTH){ length = MIN_FIELD_ABBREVIATION_LENGTH; }
			String abbreviated = fieldNames.get(i);
			if(length < StringTool.length(fieldNames.get(i))){
				abbreviated = fieldNames.get(i).substring(0, length);
			}
			abbreviatedNames.put(fieldNames.get(i), abbreviated);
		}
		return abbreviatedNames;
	}
	
}
