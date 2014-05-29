package com.hotpads.datarouter.client.imp.hbase.op.write;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Row;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;


public class HBaseIncrementOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseTask<Void>{
	
	private HBaseNode<PK,D,F> node;
	private Multimap<PK,LongField> incrementFieldByKey;
	private Config config;
	
	public HBaseIncrementOp(HBaseNode<PK,D,F> node, String taskName, Multimap<PK,LongField> incrementFieldByKey, Config pConfig){
		super(node.getDataRouterContext(), "HBaseTask."+taskName, node, pConfig);
		this.incrementFieldByKey = incrementByKey;
		this.config = Config.nullSafe(pConfig);
	}
	
	public Void hbaseCall() throws Exception{
		if(MapTool.isEmpty(incrementByKey)){ return null; }
		List<Row> actions = ListTool.createArrayList();
		int numCellsIncremented = 0, numRowsIncremented = 0;
		long batchStartTime = System.currentTimeMillis();
		for(Map.Entry<PK,Long> entry : incrementByKey.entrySet()){//TODO obey Config.commitBatchSize
			byte[] keyBytes = node.getKeyBytesWithScatteringPrefix(null, entry.getKey(), false);
			Increment increment = new Increment(keyBytes);
			List<Field<?>> fields = ;
			for(Field<?> field : fields){
				if(!(field instanceof LongField)){
					throw new IllegalArgumentException("you can only increment a LongField");
				}
				//TODO also verify that fieldInfo uses a LongField for this column
				LongField longField = (LongField)field;
				increment.addColumn(node.FAM, field.getColumnNameBytes(), longField.getValue());
				put.add(FAM, field.getColumnNameBytes(), field.getBytes());
				++numCellsPut;
			}
			increment.setWriteToWAL(config.getPersistentPut());
			actions.add(increment);
			++numRowsIncremented;
		}
		DRCounters.incSuffixClientNode(client.getType(), "cells incremented", node.getClientName(), node.getName(), numCellsIncremented);
		DRCounters.incSuffixClientNode(client.getType(), "rows incremented", node.getClientName(), node.getName(), numRowsIncremented);
		if(CollectionTool.notEmpty(actions)){
			hTable.batch(actions);
			hTable.flushCommits();
		}
		return null;
	}
}).call();

}
