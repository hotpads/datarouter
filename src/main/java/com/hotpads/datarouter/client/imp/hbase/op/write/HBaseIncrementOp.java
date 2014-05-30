package com.hotpads.datarouter.client.imp.hbase.op.write;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Row;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hbase.task.HBaseTask;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.positive.UInt63Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.ClassTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.StringByteTool;


public class HBaseIncrementOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseTask<Void>{
	
	public static final String OP_increment = "increment";
	
	private HBaseNode<PK,D,F> node;
	private Map<PK,Map<String,Long>> countByColumnByKey;
	private Config config;
	
	public HBaseIncrementOp(HBaseNode<PK,D,F> node, Map<PK,Map<String,Long>> countByColumnByKey, Config pConfig){
		super(node.getDataRouterContext(), "HBaseTask."+OP_increment, node, pConfig);
		this.node = node;
		this.countByColumnByKey = countByColumnByKey;
		this.config = Config.nullSafe(pConfig);
	}
	
	public Void hbaseCall() throws Exception{
		if(countByColumnByKey==null){ return null; }
		List<Row> actions = ListTool.createArrayList();
		int numCellsIncremented = 0, numRowsIncremented = 0;
		for(Map.Entry<PK,Map<String,Long>> row : countByColumnByKey.entrySet()){//TODO obey Config.commitBatchSize
			byte[] keyBytes = node.getKeyBytesWithScatteringPrefix(null, row.getKey(), false);
			Increment increment = new Increment(keyBytes);
			for(Map.Entry<String,Long> columnCount : row.getValue().entrySet()){
				String columnName = columnCount.getKey();
				assertColumnIsUInt63Field(columnName);
				byte[] columnNameBytes = StringByteTool.getUtf8Bytes(columnName);
				increment.addColumn(node.FAM, columnNameBytes, columnCount.getValue());
				++numCellsIncremented;
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
	
	//try to prevent making a mistake with columnName and incrementing a non-counter column
	private void assertColumnIsUInt63Field(String columnName){
		Class<? extends Field> columnType = node.getFieldInfo().getFieldTypeForColumn(columnName);
		if(ClassTool.differentClass(columnType, UInt63Field.class)){
			throw new IllegalArgumentException(columnName+" is a "+columnType.getClass()
					+", but you can only increment a LongField");
		}
	}

}