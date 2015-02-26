package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.List;

import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.FieldSetTool;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.collections.Twin;


public class HBaseQueryBuilder{
		
	/*********************** primary methods **************************************/

	public static Scan getRangeScanner(
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive, Config config){
		Twin<ByteRange> byteRange = getStartEndBytesForRange(startKey, startInclusive, endKey, endInclusive);
		Scan scan = getScanForRange(byteRange.getLeft(), true, byteRange.getRight(), false, config);
		return scan;
	}

	public static Scan getPrefixScanner(FieldSet<?> prefix, 
			boolean wildcardLastField, Config config){
		Twin<ByteRange> byteRange = getStartEndBytesForPrefix(prefix.getFields(), wildcardLastField);
		Scan scan = getScanForRange(byteRange.getLeft(), true, byteRange.getRight(), false, config);
		return scan;
	}

	public static Scan getPrefixedRangeScanner(
			FieldSet<?> prefix, boolean wildcardLastField,
			FieldSet<?> startKey, boolean startInclusive, 
			FieldSet<?> endKey, boolean endInclusive,
			Config config){
		Twin<ByteRange> prefixBounds = getStartEndBytesForPrefix(prefix.getFields(), wildcardLastField);
		Twin<ByteRange> rangeBounds = getStartEndBytesForRange(startKey, startInclusive, endKey, endInclusive);
		Pair<byte[],byte[]> intersection = getRangeIntersection(
				new Pair<byte[],byte[]>(prefixBounds.getLeft().toArray(), 
						prefixBounds.getRight().toArray()), 
				new Pair<byte[],byte[]>(rangeBounds.getLeft().toArray(), 
						rangeBounds.getRight().toArray()));
		Range<ByteRange> range = Range.create(new ByteRange(intersection.getLeft()), true, 
				new ByteRange(intersection.getRight()), false);
		Scan scan = getScanForRange(range, config);
		return scan;
	}
	
	/****************************** scan helpers ************************************/
	
	public static Scan getScanForRange(Range<ByteRange> range, Config pConfig){
		Config config = Config.nullSafe(pConfig);
		byte[] start = null;
		if(range.hasStart()){
			start = range.getStart().toArray();
			if( ! range.getStartInclusive()){
				start = DrByteTool.unsignedIncrement(start); 
			}
		}
		byte[] end = null;
		if(range.hasEnd()){
			end = range.getEnd().toArray();
			if(range.getEndInclusive()){
				end = DrByteTool.unsignedIncrement(end);
			}
		}
		
		Scan scan;
		if(range.hasStart() && range.hasEnd()){
			scan = new Scan(start, end);
		}else if(range.hasStart()){
			scan = new Scan(start);
		}else if(range.hasEnd()){
			scan = new Scan(new byte[]{}, end);
		}else{
			scan = new Scan();//whole table
		}
		scan.setCaching(getIterateBatchSize(config));
		scan.setCacheBlocks(DrBooleanTool.isTrue(config.getScannerCaching()));
		return scan;
	}

	@Deprecated//pass in a Range
	public static Scan getScanForRange(ByteRange pStart, boolean startInclusive, ByteRange pEnd, boolean endInclusive, 
			Config pConfig){
		return getScanForRange(Range.create(pStart, startInclusive, pEnd, endInclusive), pConfig);
	}
	
	/****************************** primary helpers **********************************/

	protected static Twin<ByteRange> getStartEndBytesForRange(
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive){
		ByteRange startBytes = null;
		if(startKey!=null){
			startBytes = new ByteRange(FieldSetTool.getBytesForNonNullFieldsWithNoTrailingSeparator(startKey));
			if( ! startInclusive){
				startBytes = startBytes.cloneAndIncrement(); 
			}
		}
		ByteRange endBytes = null;
		if(endKey!=null){
			endBytes = new ByteRange(FieldSetTool.getBytesForNonNullFieldsWithNoTrailingSeparator(endKey));
			if(endInclusive){ endBytes = endBytes.cloneAndIncrement(); }
		}
		return new Twin<ByteRange>(startBytes, endBytes);
	}
	
	public static Twin<ByteRange> getStartEndBytesForPrefix(List<Field<?>> prefix, boolean wildcardLastField){
		int numNonNullFields = FieldTool.countNonNullLeadingFields(prefix);
		byte[][] fieldBytes = new byte[numNonNullFields][];
		int numFullFieldsFinished = 0;
		for(Field<?> field : DrCollectionTool.nullSafe(prefix)){
			if(numFullFieldsFinished >= numNonNullFields){ break; }
			if(field.getValue()==null) {
				throw new DataAccessException("Prefix query cannot contain intermediate nulls.");
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
		byte[] startBytes = DrByteTool.concatenate(fieldBytes);
		byte[] endBytes = DrByteTool.unsignedIncrementOverflowToNull(startBytes);
		ByteRange startByteRange = startBytes==null ? null : new ByteRange(startBytes);
		ByteRange endByteRange = endBytes==null ? null : new ByteRange(endBytes);
		return new Twin<ByteRange>(startByteRange, endByteRange);
	}
	
	/************************** pure byte helpers *****************************************/
	
	protected static Pair<byte[],byte[]> getRangeIntersection(Pair<byte[],byte[]> a, Pair<byte[],byte[]> b){
		return new Pair<byte[],byte[]>(
				getGreaterOrNull(a.getLeft(), b.getLeft()),
				getLesserOrNull(a.getRight(), b.getRight()));
	}
	
	protected static byte[] getGreaterOrNull(byte[] a, byte[] b){
		int numNulls = DrObjectTool.numNulls(a, b);
		if(numNulls==2){ return null; }
		if(numNulls==1){ return a==null?b:a; }
		return DrByteTool.bitwiseCompare(a, b)>0?a:b;
	}
	
	protected static byte[] getLesserOrNull(byte[] a, byte[] b){
		int numNulls = DrObjectTool.numNulls(a, b);
		if(numNulls==2){ return null; }
		if(numNulls==1){ return a==null?b:a; }
		return DrByteTool.bitwiseCompare(a, b)<0?a:b;
	}
	
	
	/***************************** helpers **************************************/
	
	public static final int DEFAULT_ITERATE_BATCH_SIZE = 1000;
	
	public static int getIterateBatchSize(Config config){
		if(config==null){ return DEFAULT_ITERATE_BATCH_SIZE; }
		if(config.getIterateBatchSize()==null){ return DEFAULT_ITERATE_BATCH_SIZE; }
		return config.getIterateBatchSize();
	}
}
