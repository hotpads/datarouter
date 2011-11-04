package com.hotpads.datarouter.client.imp.hbase.util;

import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.collections.Pair;


public class HBaseQueryBuilder{
		
	/*********************** primary methods **************************************/

	public static Scan getRangeScanner(
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive, Config config){
		Pair<byte[],byte[]> byteRange = getStartEndBytesForRange(startKey, startInclusive, endKey, endInclusive);
		Scan scan = getScanForRange(byteRange.getLeft(), byteRange.getRight(), config);
		return scan;
	}

	public static Scan getPrefixScanner(FieldSet<?> prefix, 
			boolean wildcardLastField, Config config){
		Pair<byte[],byte[]> byteRange = getStartEndBytesForPrefix(prefix, wildcardLastField);
		Scan scan = getScanForRange(byteRange.getLeft(), byteRange.getRight(), config);
		return scan;
	}

	public static Scan getPrefixedRangeScanner(
			FieldSet<?> prefix, boolean wildcardLastField,
			FieldSet<?> startKey, boolean startInclusive, 
			FieldSet<?> endKey, boolean endInclusive,
			Config config){
		Pair<byte[],byte[]> prefixBounds = getStartEndBytesForPrefix(prefix, wildcardLastField);
		Pair<byte[],byte[]> rangeBounds = getStartEndBytesForRange(startKey, startInclusive, endKey, endInclusive);
		Pair<byte[],byte[]> intersection = getRangeIntersection(prefixBounds, rangeBounds);
		Scan scan = getScanForRange(intersection.getLeft(), intersection.getRight(), config);
		return scan;
	}
	
	/****************************** scan helpers ************************************/

	public static Scan getScanForRange(byte[] startInclusive, byte[] endExclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig);
		Scan scan;
		if(startInclusive!=null && endExclusive!=null){
			scan = new Scan(startInclusive, endExclusive);
		}else if(startInclusive!=null){
			scan = new Scan(startInclusive);
		}else if(endExclusive!=null){
			scan = new Scan(new byte[0], endExclusive);
		}else{
			scan = new Scan();//whole table
		}
		scan.setCaching(getIterateBatchSize(config));
		scan.setCacheBlocks(BooleanTool.isTrue(config.getScannerCaching()));
		return scan;
	}
	
	/****************************** primary helpers **********************************/

	protected static Pair<byte[],byte[]> getStartEndBytesForRange(
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive){
		byte[] startBytes = null;
		if(startKey!=null){
			startBytes = getBytesForNonNullFieldsWithNoTrailingSeparator(startKey);
			if( ! startInclusive){
				startBytes = ByteTool.unsignedIncrement(startBytes); 
			}
		}
		byte[] endBytes = null;
		if(endKey!=null){
			endBytes = getBytesForNonNullFieldsWithNoTrailingSeparator(endKey);
			if(endInclusive){ endBytes = ByteTool.unsignedIncrement(endBytes); }
		}
		return new Pair<byte[],byte[]>(startBytes, endBytes);
	}
	
	protected static Pair<byte[],byte[]> getStartEndBytesForPrefix(FieldSet<?> prefix, boolean wildcardLastField){
		int numNonNullFields = FieldSetTool.getNumNonNullFields(prefix);
		byte[][] fieldBytes = new byte[numNonNullFields][];
		int numFullFieldsFinished = 0;
		for(Field<?> field : CollectionTool.nullSafe(prefix.getFields())){
			if(numFullFieldsFinished >= numNonNullFields) break;
			if(field.getValue()==null) {
				throw new DataAccessException("Prefix query on "+
						prefix.getClass()+" cannot contain intermediate nulls.");
			}
			boolean lastNonNullField = (numFullFieldsFinished == numNonNullFields-1);
			boolean doPrefixMatchOnField = wildcardLastField && lastNonNullField;
			if(doPrefixMatchOnField){//TODO not sure you can actually do wildcard matches
				fieldBytes[numFullFieldsFinished] = field.getBytes();//wildcard
			}else{
				fieldBytes[numFullFieldsFinished] = field.getBytesWithSeparator();//no wildcard
			}
			++numFullFieldsFinished;
			
		}
		byte[] startBytes = ByteTool.concatenate(fieldBytes);
		byte[] endBytes = ByteTool.unsignedIncrementOverflowToNull(startBytes);
		return new Pair<byte[],byte[]>(startBytes, endBytes);
	}
	
	/************************** field to byte helpers *****************************************/
	
	protected static byte[] getBytesForNonNullFieldsWithNoTrailingSeparator(FieldSet<?> fields){
		int numNonNullFields = FieldSetTool.getNumNonNullFields(fields);
		byte[][] fieldArraysWithSeparators = new byte[numNonNullFields][];
		int fieldIdx=-1;
		for(Field<?> field : IterableTool.nullSafe(fields.getFields())){
			++fieldIdx;
			if(fieldIdx == numNonNullFields - 1){//last field
				fieldArraysWithSeparators[fieldIdx] = field.getBytes();
				break;
			}
			fieldArraysWithSeparators[fieldIdx] = field.getBytesWithSeparator();
		}
		return ByteTool.concatenate(fieldArraysWithSeparators);
	}
	
	/************************** pure byte helpers *****************************************/
	
	protected static Pair<byte[],byte[]> getRangeIntersection(Pair<byte[],byte[]> a, Pair<byte[],byte[]> b){
		return new Pair<byte[],byte[]>(
				getGreaterOrNull(a.getLeft(), b.getLeft()),
				getLesserOrNull(a.getRight(), b.getRight()));
	}
	
	protected static byte[] getGreaterOrNull(byte[] a, byte[] b){
		int numNulls = ObjectTool.numNulls(a, b);
		if(numNulls==2){ return null; }
		if(numNulls==1){ return a==null?b:a; }
		return ByteTool.bitwiseCompare(a, b)>0?a:b;
	}
	
	protected static byte[] getLesserOrNull(byte[] a, byte[] b){
		int numNulls = ObjectTool.numNulls(a, b);
		if(numNulls==2){ return null; }
		if(numNulls==1){ return a==null?b:a; }
		return ByteTool.bitwiseCompare(a, b)<0?a:b;
	}
	
	
	/***************************** helpers **************************************/
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	public static int getIterateBatchSize(Config config){
		if(config==null){ return DEFAULT_ITERATE_BATCH_SIZE; }
		if(config.getIterateBatchSize()==null){ return DEFAULT_ITERATE_BATCH_SIZE; }
		return config.getIterateBatchSize();
	}
}
