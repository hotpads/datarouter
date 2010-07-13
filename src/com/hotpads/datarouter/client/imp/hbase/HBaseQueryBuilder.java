package com.hotpads.datarouter.client.imp.hbase;

import java.util.Collection;

import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;


public class HBaseQueryBuilder{

	public static Scan getRangeScanner(
			final FieldSet start, final boolean startInclusive, 
			final FieldSet end, final boolean endInclusive, Config config){
		byte[] startBytes = null;
		if(start!=null){
			getBytesForNonNullFields(start.getFields());
			if(!startInclusive){ ByteTool.unsignedIncrement(startBytes); }
		}
		byte[] endBytes = null;
		if(end!=null){
			getBytesForNonNullFields(end.getFields());
			if(!endInclusive){ ByteTool.unsignedIncrement(endBytes); }
		}
//		Scan scan = new Scan(startBytes, endBytes);//scans will time out strangely with null start and stop values, i guess
		Scan scan;
		if(start!=null && end!=null){
			scan = new Scan(startBytes, endBytes);
		}else if(start!=null){
			scan = new Scan(startBytes);
		}else if(end!=null){
			scan = new Scan(new byte[0], endBytes);
		}else{
			scan = new Scan();//whole table
		}
		scan.setCaching(getIterateBatchSize(config));
		return scan;
	}
	
	protected static byte[] getBytesForNonNullFields(Collection<Field<?>> fields){
		if(CollectionTool.size(fields)==1){ return CollectionTool.getFirst(fields).getBytes(); }
		if(CollectionTool.isEmpty(fields)){ return new byte[0]; }
		byte[][] fieldArraysWithSeparators = new byte[CollectionTool.size(fields)][];
		int fieldIdx=-1;
		for(Field<?> field : IterableTool.nullSafe(fields)){
			++fieldIdx;
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}

	public static Scan getPrefixScanner(FieldSet prefix, boolean wildcardLastField, Config config){
		int numNonNullFields = FieldSetTool.getNumNonNullFields(prefix);
		byte[][] fieldBytes = new byte[numNonNullFields][];
		int numFullFieldsFinished = 0;
		for(Field<?> field : CollectionTool.nullSafe(prefix.getFields())){
			if(numFullFieldsFinished < numNonNullFields){
				boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
				boolean doPrefixMatchOnField = wildcardLastField && lastNonNullField;
				if(doPrefixMatchOnField){//TODO not sure you can actually do wildcard matches
					fieldBytes[numFullFieldsFinished] = field.getBytes();//wildcard
				}else{
					fieldBytes[numFullFieldsFinished] = field.getBytesWithSeparator();//no wildcard
				}
				++numFullFieldsFinished;
			}
		}
		byte[] startBytes = ByteTool.concatenate(fieldBytes);
		byte[] endBytes = ByteTool.unsignedIncrementOverflowToNull(startBytes);
		Scan scan = new Scan(startBytes, endBytes);
		scan.setCaching(getIterateBatchSize(config));
		return scan;
	}
	
	
	/***************************** helpers **************************************/
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	public static int getIterateBatchSize(Config config){
		if(config==null){ return DEFAULT_ITERATE_BATCH_SIZE; }
		if(config.getIterateBatchSize()==null){ return DEFAULT_ITERATE_BATCH_SIZE; }
		return config.getIterateBatchSize();
	}
}
