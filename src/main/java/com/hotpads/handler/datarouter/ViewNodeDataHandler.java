package com.hotpads.handler.datarouter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.hibernate.node.HibernateReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.PrimaryKeyStringConverter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.PrimaryKeyFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.datarouter.query.CountWhereTxn;
import com.hotpads.handler.datarouter.query.GetWhereTxn;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.java.ReflectionTool;

public class ViewNodeDataHandler<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends HibernateReaderNode<PK,D,F>>
		extends BaseHandler{

	private DatarouterContext drContext;
	private Node<?,?> node;
	private Integer limit;

	@Inject
	public ViewNodeDataHandler(DatarouterContext drContext){
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
			PARAM_where = "where",
			PARAM_backKey = "backKey", 
			PARAM_startAfterKey = "startAfterKey", 
			PARAM_nextKey = "nextKey",
			PARAM_limit = "limit", 
			PARAM_offset = "offset";

	private Mav preHandle(){
		Mav mav = new Mav("/jsp/admin/viewNodeData.jsp");
		String nodeName = RequestTool.get(request, PARAM_nodeName);
		node = drContext.getNodes().getNode(nodeName);
		if(node == null){ return new MessageMav("Cannot find node " + nodeName); }
		mav.put("node", node);

		limit = RequestTool.getIntegerAndPut(request, PARAM_limit, 100, mav);
		return mav;
	}

	@Override
	protected Mav handleDefault(){
		return preHandle();
	}

	@Handler
	public Mav countKeys(){
		Mav mav = preHandle();
		if(!(node instanceof SortedStorageWriter<?,?>)){ return new MessageMav("Cannot browse unsorted node"); }
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		int iterateBatchSize = 10000;
		Iterable<PK> iterable = sortedNode.scanKeys(null, new Config().setIterateBatchSize(
				iterateBatchSize).setScannerCaching(false).setTimeout(10, TimeUnit.SECONDS).setNumAttempts(5));
		int printBatchSize = 10000;
		long count = 0;
		PK last = null;
		long startMs = System.currentTimeMillis() - 1;
		long batchStartMs = System.currentTimeMillis() - 1;
		for(PK pk : iterable){
			if(DrComparableTool.lt(pk, last)){
				logger.warn(pk+" was < "+last);//shouldn't happen, but seems to once in 10mm times
			}
			++count;
			if(count > 0 && count % printBatchSize == 0){
				long batchMs = System.currentTimeMillis() - batchStartMs;
				double batchAvgRps = printBatchSize * 1000 / batchMs;
				logger.warn(DrNumberFormatter.addCommas(count) + " " + pk.toString() + " @" + batchAvgRps + "rps");
				batchStartMs = System.currentTimeMillis();
			}
			last = pk;
		}
		if(count < 1){ return new MessageMav("no rows found"); }
		long ms = System.currentTimeMillis() - startMs;
		double avgRps = count * 1000 / ms;
		String message = "finished at " + DrNumberFormatter.addCommas(count) + " " + last.toString() + " @" + avgRps
				+ "rps";
		logger.warn(message);
		return new MessageMav(message);
	}

	@Handler
	public Mav countWhere(){
		preHandle();
		// assume all table names are the same (they are at the time of writing this)
		String tableName = DrCollectionTool.getFirst(node.getPhysicalNodes()).getTableName();
		String where = params.optional(PARAM_where, null);
		List<String> clientNames = node.getClientNames();
		Long count = node.getRouter().run(new CountWhereTxn(drContext, clientNames, tableName, where));
		Mav mav = new MessageMav("found "+DrNumberFormatter.addCommas(count)+" rows in "+tableName+" ("+node.getName()+")");
		return mav;
	}

	@Handler
	public Mav browseData(){
		Mav mav = preHandle();
		if(!(node instanceof SortedStorageReader<?,?>)){ return new MessageMav("Cannot browse "
				+ node.getClass().getSimpleName()); }
		List<Field<?>> fields = node.getFields();
		mav.put("nonFieldAware", "field aware");

		if(fields == null){
			fields = DrListTool.create();
			fields.addAll(node.getFieldInfo().getPrimaryKeyFields());
			mav.put("nonFieldAware", " non field aware");
		}

		mav.put("fields", fields);
		SortedStorageReader<PK,D> sortedNode = (SortedStorageReader<PK,D>)node;
		String backKeyString = RequestTool.get(request, PARAM_backKey, null);// allows for 1 "back" action
		mav.put(PARAM_backKey, backKeyString);
		String startAfterKeyString = RequestTool.get(request, PARAM_startAfterKey, null);

		Config config = new Config().setLimit(limit);
		PK startAfterKey = null;
		if(DrStringTool.notEmpty(startAfterKeyString)){
//			startAfterKey = (PK)ReflectionTool.create(node.getPrimaryKeyType());
			startAfterKey = PrimaryKeyStringConverter.primaryKeyFromString(
					(Class<PK>)node.getFieldInfo().getPrimaryKeyClass(), //need to use the fielder in the jsp
					(PrimaryKeyFielder<PK>)node.getFieldInfo().getSamplePrimaryKey(), startAfterKeyString);
			startAfterKey.fromPersistentString(startAfterKeyString);
			mav.put(PARAM_startAfterKey, startAfterKey.getPersistentString());
		}

		boolean startInclusive = true;
		List<D> databeans = DrListTool.createArrayList(sortedNode.scan(new Range<>((PK)startAfterKey, startInclusive, 
				null, true), config));

		addDatabeansToMav(mav, databeans);
		return mav;
	}

	@Handler
	public Mav getWhere(){
		Mav mav = preHandle();
		if(!(node instanceof HibernateReaderNode<?,?,?>)){ return new MessageMav("Cannot getWhere "
				+ node.getClass().getSimpleName()); }
		mav.put("fields", node.getFields());
		String backKeyString = RequestTool.get(request, PARAM_backKey, null);// allows for 1 "back" action
		mav.put(PARAM_backKey, backKeyString);
		String startAfterKeyString = RequestTool.get(request, PARAM_startAfterKey, null);

		Config config = new Config().setLimit(limit);
		PK startAfterKey = null;
		if(DrStringTool.notEmpty(startAfterKeyString)){
			startAfterKey = (PK)ReflectionTool.create(node.getPrimaryKeyType());
			startAfterKey.fromPersistentString(startAfterKeyString);
			mav.put(PARAM_startAfterKey, startAfterKey.getPersistentString());
		}

		// assume all table names are the same (they are at the time of writing this)
		String tableName = DrCollectionTool.getFirst(node.getPhysicalNodes()).getTableName();
		String where = RequestTool.getAndPut(request, PARAM_where, null, mav);
		List<D> databeans = node.getRouter().run(new GetWhereTxn<PK,D,F,N>((N)node, tableName, startAfterKey, where, 
				config));
		addDatabeansToMav(mav, databeans);
		return mav;
	}

	private void addDatabeansToMav(Mav mav, List<D> databeans){
		mav.put("databeans", databeans);

		List<List<Field<?>>> rowsOfFields = DrListTool.create();
		DatabeanFielder fielder = node.getFieldInfo().getSampleFielder();
		if(fielder != null){
			for(Databean<?,?> databean : DrIterableTool.nullSafe(databeans)){
//				FieldSet<?> fieldSet = (FieldSet<?>)databean;
				List<Field<?>> rowOfFields = fielder.getFields(databean);
				rowsOfFields.add(rowOfFields);
			}
			mav.put("rowsOfFields", rowsOfFields);
		}

		mav.put("abbreviatedFieldNameByFieldName", getFieldAbbreviationByFieldName(fielder, databeans));
		if(DrCollectionTool.size(databeans) >= limit){
			mav.put(PARAM_nextKey, DrCollectionTool.getLast(databeans).getKey().getPersistentString());
		}
	}

	public static final Integer MIN_FIELD_ABBREVIATION_LENGTH = 2;

	private Map<String,String> getFieldAbbreviationByFieldName(DatabeanFielder fielder, 
			Collection<? extends Databean<?,?>> databeans){
		if(DrCollectionTool.isEmpty(databeans)){ return DrMapTool.create(); }
		Databean<?,?> first = DrIterableTool.first(databeans);
		List<String> fieldNames = FieldTool.getFieldNames(fielder.getFields(first));
		List<Integer> maxLengths = DrListTool.createArrayListAndInitialize(fieldNames.size());
		Collections.fill(maxLengths, 0);
		
		for(Databean<?,?> d : DrIterableTool.nullSafe(databeans)){
			List<?> values = FieldTool.getFieldValues(fielder.getFields(d));
			for(int i = 0; i < DrCollectionTool.size(values); ++i){
				int length = values.get(i) == null ? 0 : DrStringTool.length(values.get(i).toString());
				if(length > maxLengths.get(i)){
					maxLengths.set(i, length);
				}
			}
		}
		
		Map<String,String> abbreviatedNames = DrMapTool.create();
		for(int i = 0; i < maxLengths.size(); ++i){
			int length = maxLengths.get(i);
			if(length < MIN_FIELD_ABBREVIATION_LENGTH){
				length = MIN_FIELD_ABBREVIATION_LENGTH;
			}
			String abbreviated = fieldNames.get(i);
			if(length < DrStringTool.length(fieldNames.get(i))){
				abbreviated = fieldNames.get(i).substring(0, length);
			}
			abbreviatedNames.put(fieldNames.get(i), abbreviated);
		}
		return abbreviatedNames;
	}

}
