package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.SimpleFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Pair;

public class HBaseScatteringPrefixQueryBuilder {

	public static List<Scan> getRangeScanners(DatabeanFieldInfo<?,?,?> fieldInfo,
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive, Config config){
		List<Scan> outs = ListTool.createArrayList();
		List<Pair<byte[],byte[]>> ranges = getRangeForEachScatteringPrefix(fieldInfo,
				startKey, startInclusive, endKey, endInclusive, config);
		for(Pair<byte[],byte[]> range : IterableTool.nullSafe(ranges)){
			Scan scan = HBaseQueryBuilder.getScanForRange(range.getLeft(), true, range.getRight(), config);
			outs.add(scan);
		}
		return outs;
	}

	public static List<Scan> getPrefixScanners(DatabeanFieldInfo<?,?,?> fieldInfo, FieldSet<?> prefix, 
			boolean wildcardLastField, Config config){
//		return getPrefixedRangeScanner(fieldInfo, prefix, wildcardLastField, null, true, null, true, config);
		ArrayList<FieldSet<?>> scatteringPrefixesPlusPrefix = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, prefix);
		if(CollectionTool.isEmpty(scatteringPrefixesPlusPrefix)){ 
			return ListTool.wrap(HBaseQueryBuilder.getScanForRange(null, true, null, config)); 
		}
		List<Scan> outs = ListTool.createArrayList();
		for(FieldSet<?> fieldSet : scatteringPrefixesPlusPrefix){
			Pair<byte[],byte[]> byteRange = HBaseQueryBuilder.getStartEndBytesForPrefix(fieldSet, wildcardLastField);
			Scan scan = HBaseQueryBuilder.getScanForRange(byteRange.getLeft(), true, byteRange.getRight(), config);
			outs.add(scan);
		}
		return outs;
	}
	
	public static List<Scan> getPrefixScanners(DatabeanFieldInfo<?,?,?> fieldInfo, Collection<? extends FieldSet<?>> prefixes, 
			boolean wildcardLastField, Config config){
		List<Scan> outs = ListTool.createArrayList();
		for(FieldSet<?> prefix : IterableTool.nullSafe(prefixes)){
//			List<Scan> scansForSinglePrefix = getPrefixedRangeScanner(fieldInfo, prefix, wildcardLastField, null, true, null, true, config);
//			outs.addAll(CollectionTool.nullSafe(scansForSinglePrefix));
			List<Scan> scansForSinglePrefix = getPrefixScanners(fieldInfo, prefix, wildcardLastField, config);
			outs.addAll(scansForSinglePrefix);
		}
		return outs;
	}

	public static List<Pair<byte[],byte[]>> getPrefixedRanges(DatabeanFieldInfo<?,?,?> fieldInfo, 
			FieldSet<?> prefix, boolean wildcardLastField,
			FieldSet<?> startKey, boolean startInclusive, 
			FieldSet<?> endKey, boolean endInclusive,
			Config config){
		ArrayList<FieldSet<?>> scatteringPrefixesPlusPrefix = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, prefix);
		ArrayList<FieldSet<?>> scatteringPrefixesPlusStartKey = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, startKey);
		ArrayList<FieldSet<?>> scatteringPrefixesPlusEndKey = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, endKey);
		List<Pair<byte[],byte[]>> outs = ListTool.createArrayList();
		for(int i=0; i < CollectionTool.size(scatteringPrefixesPlusPrefix); ++i){
			FieldSet<?> scatteringPrefixPlusPrefix = scatteringPrefixesPlusPrefix.get(i);
			FieldSet<?> scatteringPrefixPlusStartKey = scatteringPrefixesPlusStartKey.get(i);
			FieldSet<?> scatteringPrefixPlusEndKey = scatteringPrefixesPlusEndKey.get(i);
			Pair<byte[],byte[]> prefixBounds = HBaseQueryBuilder.getStartEndBytesForPrefix(scatteringPrefixPlusPrefix, wildcardLastField);
			Pair<byte[],byte[]> rangeBounds = HBaseQueryBuilder.getStartEndBytesForRange(
					scatteringPrefixPlusStartKey, startInclusive, scatteringPrefixPlusEndKey, endInclusive);
			Pair<byte[],byte[]> intersection = HBaseQueryBuilder.getRangeIntersection(prefixBounds, rangeBounds);
			outs.add(intersection);
		}
		return outs;
	}
	
	
	/**************************** helper ***************************************/
	
	public static ArrayList<FieldSet<?>> getInstancesForAllPossibleScatteringPrefixes(
			DatabeanFieldInfo<?,?,?> fieldInfo, Collection<? extends FieldSet<?>> pks){
		ArrayList<FieldSet<?>> outs = ListTool.createArrayList();
		List<List<Field<?>>> allScatteringPrefixFields = fieldInfo.getSampleScatteringPrefix().getAllPossibleScatteringPrefixes();
		//iterate through prefixes first so that everything stays sorted
		for(List<Field<?>> scatteringPrefixFields : allScatteringPrefixFields){
			for(FieldSet<?> pk : IterableTool.nullSafe(pks)){
				SimpleFieldSet<?> scatteringPrefixPlusPrefix = new SimpleFieldSet(scatteringPrefixFields);
				if(pk!=null){ scatteringPrefixPlusPrefix.add(pk.getFields()); }
				outs.add(scatteringPrefixPlusPrefix);
			}
		}
		return outs;
	}
	
	public static ArrayList<FieldSet<?>> getInstanceForAllPossibleScatteringPrefixes(
			DatabeanFieldInfo<?,?,?> fieldInfo, FieldSet<?> pk){
		ArrayList<FieldSet<?>> outs = ListTool.createArrayList();
		List<List<Field<?>>> allScatteringPrefixFields = fieldInfo.getSampleScatteringPrefix().getAllPossibleScatteringPrefixes();
		for(List<Field<?>> scatteringPrefixFields : allScatteringPrefixFields){
			if(CollectionTool.isEmpty(scatteringPrefixFields) && pk==null){ 
				outs.add(null); 
			}else{
				SimpleFieldSet<?> scatteringPrefixPlusPrefix = new SimpleFieldSet(scatteringPrefixFields);
				if(pk!=null){ scatteringPrefixPlusPrefix.add(pk.getFields()); }
				outs.add(scatteringPrefixPlusPrefix);
			}
		}
		return outs;
	}
	
	public static List<Pair<byte[],byte[]>> getRangeForEachScatteringPrefix(DatabeanFieldInfo<?,?,?> fieldInfo,
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive, Config config){
		
		List<Pair<byte[],byte[]>> ranges = ListTool.createArrayList();		
		ArrayList<FieldSet<?>> prefixedStartKeys = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, startKey);
		ArrayList<FieldSet<?>> prefixedEndKeys = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, endKey);
		
		//null endKey should not be exclusive because it would never return results
		boolean endInclusiveOverride = endInclusive;
		if(endKey==null){ endInclusiveOverride = true; }
		
		for(int i=0; i < prefixedStartKeys.size(); ++i){
			Pair<byte[],byte[]> byteRange = HBaseQueryBuilder.getStartEndBytesForRange(
					prefixedStartKeys.get(i), startInclusive, prefixedEndKeys.get(i), endInclusiveOverride);
			ranges.add(byteRange);
		}
		return ranges;
	}
	
		
//	@Deprecated
//	public static <PK extends PrimaryKey<PK>> ArrayList<HBasePrimaryKeyScanner<PK>> getScannerForEachPrefix(
//			DatabeanFieldInfo<PK,?,?> fieldInfo,
//			HTable hTable,
//			FieldSet<?> startKey, boolean startInclusive, 
//			FieldSet<?> endKey, boolean endInclusive,
//			final Config pConfig){
//			Config config = Config.nullSafe(pConfig);
//		List<Scan> scanForEachScatteringPartition = HBaseScatteringPrefixQueryBuilder
//				.getRangeScanners(fieldInfo, startKey, startInclusive, endKey, endInclusive, config);
//		ArrayList<HBasePrimaryKeyScanner<PK>> scanners = ListTool.createArrayList();
//		for(Scan scan : scanForEachScatteringPartition){
//			scanners.add(new HBasePrimaryKeyScanner(fieldInfo, hTable, scan));
//		}
//		return scanners;
//	}
	
	public static <PK extends PrimaryKey<PK>> 
	ArrayList<HBaseManualPrimaryKeyScanner<PK>> getManualPrimaryKeyScannerForEachPrefix(
			HBaseReaderNode<PK,?,?> node,
			DatabeanFieldInfo<PK,?,?> fieldInfo,
			HTable hTable,
			FieldSet<?> start, boolean startInclusive, 
			FieldSet<?> end, boolean endInclusive,
			final Config pConfig){
			Config config = Config.nullSafe(pConfig);
		List<Pair<byte[],byte[]>> ranges = getRangeForEachScatteringPrefix(
				fieldInfo, start, startInclusive, end, endInclusive, config);
		ArrayList<HBaseManualPrimaryKeyScanner<PK>> scanners = ListTool.createArrayList();
		for(Pair<byte[],byte[]> range : ranges){
			scanners.add(new HBaseManualPrimaryKeyScanner<PK>(node, fieldInfo, hTable, 
					range.getLeft(), range.getRight(), config));
		}
		return scanners;
	}
	
	//should probably be merged with getManualPrimaryKeyScannerForEachPrefix
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	ArrayList<HBaseManualDatabeanScanner<PK,D>> getManualDatabeanScannerForEachPrefix(
			HBaseReaderNode<PK,D,?> node,
			DatabeanFieldInfo<PK,D,?> fieldInfo,
			HTable hTable,
			FieldSet<?> start, boolean startInclusive, 
			FieldSet<?> end, boolean endInclusive,
			final Config pConfig){
			Config config = Config.nullSafe(pConfig);
		List<Pair<byte[],byte[]>> ranges = getRangeForEachScatteringPrefix(
				fieldInfo, start, startInclusive, end, endInclusive, config);
		ArrayList<HBaseManualDatabeanScanner<PK,D>> scanners = ListTool.createArrayList();
		for(Pair<byte[],byte[]> range : ranges){
			scanners.add(new HBaseManualDatabeanScanner<PK,D>(node, fieldInfo, hTable, 
					range.getLeft(), range.getRight(), config));
		}
		return scanners;
	}
	
	//should probably be merged with getManualPrimaryKeyScannerForEachPrefix
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> 
	ArrayList<HBaseManualDatabeanScanner<PK,D>> getManualDatabeanScannersForRanges(
			HBaseReaderNode<PK,D,?> node,
			DatabeanFieldInfo<PK,D,?> fieldInfo,
			HTable hTable,
			Collection<Pair<byte[],byte[]>> ranges,
			final Config pConfig){
			Config config = Config.nullSafe(pConfig);
		ArrayList<HBaseManualDatabeanScanner<PK,D>> scanners = ListTool.createArrayList();
		for(Pair<byte[],byte[]> range : ranges){
			scanners.add(new HBaseManualDatabeanScanner<PK,D>(node, fieldInfo, hTable, 
					range.getLeft(), range.getRight(), config));
		}
		return scanners;
	}
}
