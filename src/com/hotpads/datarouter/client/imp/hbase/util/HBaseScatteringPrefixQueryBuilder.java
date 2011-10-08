package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Scan;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.SimpleFieldSet;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Pair;

public class HBaseScatteringPrefixQueryBuilder {

	public static List<Scan> getRangeScanners(DatabeanFieldInfo<?,?,?> fieldInfo,
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive, Config config){
//		return getPrefixedRangeScanner(fieldInfo, null, false, startKey, startInclusive, endKey, endInclusive, config);
		List<Scan> outs = ListTool.createArrayList();
		ArrayList<FieldSet<?>> prefixedStartKeys = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, startKey);
		ArrayList<FieldSet<?>> prefixedEndKeys = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, endKey);
		for(int i=0; i < prefixedStartKeys.size(); ++i){
			Pair<byte[],byte[]> byteRange = HBaseQueryBuilder.getStartEndBytesForRange(
					prefixedStartKeys.get(i), startInclusive, prefixedEndKeys.get(i), endInclusive);
			Scan scan = HBaseQueryBuilder.getScanForRange(byteRange.getLeft(), byteRange.getRight(), config);
			outs.add(scan);
		}
		return outs;
	}

	public static List<Scan> getPrefixScanners(DatabeanFieldInfo<?,?,?> fieldInfo, FieldSet<?> prefix, 
			boolean wildcardLastField, Config config){
//		return getPrefixedRangeScanner(fieldInfo, prefix, wildcardLastField, null, true, null, true, config);
		ArrayList<FieldSet<?>> scatteringPrefixesPlusPrefix = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, prefix);
		if(CollectionTool.isEmpty(scatteringPrefixesPlusPrefix)){ return ListTool.wrap(HBaseQueryBuilder.getScanForRange(null, null, config)); }
		List<Scan> outs = ListTool.createArrayList();
		for(FieldSet<?> fieldSet : scatteringPrefixesPlusPrefix){
			Pair<byte[],byte[]> byteRange = HBaseQueryBuilder.getStartEndBytesForPrefix(fieldSet, wildcardLastField);
			Scan scan = HBaseQueryBuilder.getScanForRange(byteRange.getLeft(), byteRange.getRight(), config);
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

	@Deprecated//getting rid of prefixedRange searches
	public static List<Scan> getPrefixedRangeScanners(DatabeanFieldInfo<?,?,?> fieldInfo, 
			FieldSet<?> prefix, boolean wildcardLastField,
			FieldSet<?> startKey, boolean startInclusive, 
			FieldSet<?> endKey, boolean endInclusive,
			Config config){
		ArrayList<FieldSet<?>> scatteringPrefixesPlusPrefix = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, prefix);
		ArrayList<FieldSet<?>> scatteringPrefixesPlusStartKey = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, startKey);
		ArrayList<FieldSet<?>> scatteringPrefixesPlusEndKey = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, endKey);
		List<Scan> outs = ListTool.createArrayList();
		for(int i=0; i < CollectionTool.size(scatteringPrefixesPlusPrefix); ++i){
			FieldSet<?> scatteringPrefixPlusPrefix = scatteringPrefixesPlusPrefix.get(i);
			FieldSet<?> scatteringPrefixPlusStartKey = scatteringPrefixesPlusStartKey.get(i);
			FieldSet<?> scatteringPrefixPlusEndKey = scatteringPrefixesPlusEndKey.get(i);
			Pair<byte[],byte[]> prefixBounds = HBaseQueryBuilder.getStartEndBytesForPrefix(scatteringPrefixPlusPrefix, wildcardLastField);
			Pair<byte[],byte[]> rangeBounds = HBaseQueryBuilder.getStartEndBytesForRange(
					scatteringPrefixPlusStartKey, startInclusive, scatteringPrefixPlusEndKey, endInclusive);
			Pair<byte[],byte[]> intersection = HBaseQueryBuilder.getRangeIntersection(prefixBounds, rangeBounds);
			Scan scan = HBaseQueryBuilder.getScanForRange(intersection.getLeft(), intersection.getRight(), config);
			outs.add(scan);
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
}
