package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.hbase.batching.HBaseDatabeanBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.batching.HBasePrimaryKeyBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseReaderNode;
import com.hotpads.datarouter.client.imp.hbase.scan.HBaseDatabeanScanner;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.datarouter.storage.field.SimpleFieldSet;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.collections.Twin;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;
import com.hotpads.util.core.iterable.scanner.batch.BatchingSortedScanner;

public class HBaseScatteringPrefixQueryBuilder {
	private static Logger logger = LoggerFactory.getLogger(HBaseScatteringPrefixQueryBuilder.class);

	public static List<Scan> getPrefixScanners(DatabeanFieldInfo<?,?,?> fieldInfo, FieldSet<?> prefix, 
			boolean wildcardLastField, Config config){
		ArrayList<FieldSet<?>> scatteringPrefixesPlusPrefix = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, prefix);
		if(DrCollectionTool.isEmpty(scatteringPrefixesPlusPrefix)){ 
			return DrListTool.wrap(HBaseQueryBuilder.getScanForRange(null, true, null, false, config)); 
		}
		List<Scan> outs = new ArrayList<>();
		for(FieldSet<?> fieldSet : scatteringPrefixesPlusPrefix){
			Twin<ByteRange> byteRange = HBaseQueryBuilder.getStartEndBytesForPrefix(fieldSet.getFields(), wildcardLastField);
			Range<ByteRange> scanRange = Range.create(byteRange.getLeft(), true, byteRange.getRight(), false);
			Scan scan = HBaseQueryBuilder.getScanForRange(scanRange, config);
			outs.add(scan);
		}
		return outs;
	}
	
	public static List<Scan> getPrefixScanners(DatabeanFieldInfo<?,?,?> fieldInfo, Collection<? extends FieldSet<?>> prefixes, 
			boolean wildcardLastField, Config config){
		List<Scan> outs = new ArrayList<>();
		for(FieldSet<?> prefix : DrIterableTool.nullSafe(prefixes)){
			List<Scan> scansForSinglePrefix = getPrefixScanners(fieldInfo, prefix, wildcardLastField, config);
			outs.addAll(scansForSinglePrefix);
		}
		return outs;
	}
	
	
	/**************************** helper ***************************************/
	
	public static ArrayList<FieldSet<?>> getInstanceForAllPossibleScatteringPrefixes(
			DatabeanFieldInfo<?,?,?> fieldInfo, FieldSet<?> pk){
		ArrayList<FieldSet<?>> outs = new ArrayList<>();
		List<List<Field<?>>> allScatteringPrefixFields = fieldInfo.getSampleScatteringPrefix().getAllPossibleScatteringPrefixes();
		for(List<Field<?>> scatteringPrefixFields : allScatteringPrefixFields){
			if(DrCollectionTool.isEmpty(scatteringPrefixFields) && pk==null){ 
				outs.add(null); 
			}else{
				SimpleFieldSet<?> scatteringPrefixPlusPk = new SimpleFieldSet(scatteringPrefixFields);
				if(pk!=null){ scatteringPrefixPlusPk.add(pk.getFields()); }
				outs.add(scatteringPrefixPlusPk);
			}
		}
		return outs;
	}
	
	public static List<Twin<ByteRange>> getRangeForEachScatteringPrefix(DatabeanFieldInfo<?,?,?> fieldInfo,
			final FieldSet<?> startKey, final boolean startInclusive, 
			final FieldSet<?> endKey, final boolean endInclusive, Config config){
		
		List<Twin<ByteRange>> ranges = new ArrayList<>();		
		ArrayList<FieldSet<?>> prefixedStartKeys = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, startKey);
		ArrayList<FieldSet<?>> prefixedEndKeys = getInstanceForAllPossibleScatteringPrefixes(fieldInfo, endKey);
		
		//null endKey should not be exclusive because it would never return results
		boolean endInclusiveOverride = endInclusive;
		if(endKey==null){ endInclusiveOverride = true; }
		
		for(int i=0; i < prefixedStartKeys.size(); ++i){
			Twin<ByteRange> byteRange = HBaseQueryBuilder.getStartEndBytesForRange(
					prefixedStartKeys.get(i), startInclusive, prefixedEndKeys.get(i), endInclusiveOverride);
			ranges.add(byteRange);
		}
		return ranges;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	ArrayList<BatchingSortedScanner<PK>> getBatchingPrimaryKeyScannerForEachPrefix(
			ExecutorService executorService,
			HBaseReaderNode<PK,D,F> node,
			DatabeanFieldInfo<PK,D,F> fieldInfo,
			Range<PK> pkRange,
			final Config pConfig){
		List<List<Field<?>>> allScatteringPrefixes = fieldInfo.getSampleScatteringPrefix()
				.getAllPossibleScatteringPrefixes();
		ArrayList<BatchingSortedScanner<PK>> scanners = new ArrayList<>();
		for(List<Field<?>> scatteringPrefix : allScatteringPrefixes){
			BatchLoader<PK> firstBatchLoaderForPrefix = new HBasePrimaryKeyBatchLoader<PK,D,F>(node, scatteringPrefix, 
					pkRange, pConfig, 1L);//start the counter at 1
			BatchingSortedScanner<PK> scanner = new BatchingSortedScanner<PK>(executorService, firstBatchLoaderForPrefix);
			scanners.add(scanner);
		}
		return scanners;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>> 
	ArrayList<BatchingSortedScanner<D>> getBatchingDatabeanScannerForEachPrefix(
			ExecutorService executorService,
			HBaseReaderNode<PK,D,F> node,
			DatabeanFieldInfo<PK,D,F> fieldInfo,
			Range<PK> pkRange,
			final Config pConfig){
		List<List<Field<?>>> allScatteringPrefixes = fieldInfo.getSampleScatteringPrefix()
				.getAllPossibleScatteringPrefixes();
		ArrayList<BatchingSortedScanner<D>> scanners = new ArrayList<>();
		for(List<Field<?>> scatteringPrefix : allScatteringPrefixes){
//			logger.warn("including scanner for scatteringPrefix:"+scatteringPrefix);
			BatchLoader<D> firstBatchLoaderForPrefix = new HBaseDatabeanBatchLoader<PK,D,F>(node, scatteringPrefix, 
					pkRange, pConfig, 1L);//start the counter at 1
			BatchingSortedScanner<D> scanner = new BatchingSortedScanner<D>(executorService, firstBatchLoaderForPrefix);
			scanners.add(scanner);
		}
		return scanners;
	}
}
