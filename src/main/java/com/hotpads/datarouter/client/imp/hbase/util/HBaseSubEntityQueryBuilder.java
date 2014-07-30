package com.hotpads.datarouter.client.imp.hbase.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;

import com.hotpads.datarouter.client.imp.hbase.batching.entity.HBaseEntityDatabeanBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.batching.entity.HBaseEntityPrimaryKeyBatchLoader;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fieldcache.EntityFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.entity.EntityPartitioner;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.batch.BatchLoader;
import com.hotpads.util.core.iterable.scanner.batch.BatchingSortedScanner;

public class HBaseSubEntityQueryBuilder<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends HBaseEntityQueryBuilder<EK,E>
{
	
	private DatabeanFieldInfo<PK,D,F> fieldInfo;
	
	public HBaseSubEntityQueryBuilder(EntityFieldInfo<EK,E> entityFieldInfo, DatabeanFieldInfo<PK,D,F> fieldInfo){
		super(entityFieldInfo);
		this.fieldInfo = fieldInfo;
	}
	
	
	/******************* keys ****************************/
	
	public Range<EK> getEkRange(Range<PK> pkRange){
		EK start = pkRange.hasStart() ? pkRange.getStart().getEntityKey() : null;
		EK end = pkRange.hasEnd() ? pkRange.getEnd().getEntityKey() : null;
		return Range.create(start, pkRange.getStartInclusive(), end, pkRange.getEndInclusive());
	}
	
	public byte[] getQualifier(PK primaryKey, String fieldName){
		return ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey),
				StringByteTool.getUtf8Bytes(fieldName));
	}
	
	public byte[] getQualifierPrefix(PK primaryKey){
		return ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey));
	}
	
	public byte[] getQualifierPkBytes(PK primaryKey){
		if(primaryKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(primaryKey.getPostEntityKeyFields(), true, true);
	}
	
	public Range<ByteRange> getRowRange(int partition, Range<PK> pkRange){
		byte[] partitionPrefix = partitioner.getPrefix(partition);
		ByteRange startBytes = new ByteRange(partitionPrefix);
		if(pkRange.hasStart()){
			EK startEk = pkRange.getStart().getEntityKey();
			byte[] startByteArray = ByteTool.concatenate(partitionPrefix, getRowBytes(startEk));
			startBytes = new ByteRange(startByteArray);
		}
		
		ByteRange endBytes = null;
		if(pkRange.hasEnd()){
			EK endEk = pkRange.getEnd().getEntityKey();
			byte[] endByteArray = ByteTool.concatenate(partitionPrefix, getRowBytes(endEk));
			endBytes = new ByteRange(endByteArray);
		}else if(!partitioner.isLastPartition(partition)){
			byte[] nextPartitionPrefix = partitioner.getNextPrefix(partition);
			endBytes = new ByteRange(nextPartitionPrefix);
		}//else no end
		return Range.create(startBytes, pkRange.getStartInclusive(), endBytes, pkRange.getEndInclusive());
	}
	
	public ColumnRangeFilter getColumnRangeFilter(Range<PK> pkRange){
		byte[] start = getQualifierPrefix(pkRange.getStart());
		byte[] end = getQualifierPrefix(pkRange.getEnd());
		return new ColumnRangeFilter(start, pkRange.getStartInclusive(), end, pkRange.getEndInclusive());
	}
	
	
	/*********************** Get ****************************/
	
	public List<Get> getPrefixGets(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		List<Get> gets = ListTool.createArrayList();
		for(PK prefix : prefixes){
			gets.add(getPrefixGet(prefix, wildcardLastField, config));
		}
		return gets;
	}
	
	public Get getPrefixGet(PK pkPrefix, boolean wildcardLastField, Config config){
		EK ek = pkPrefix.getEntityKey();
		byte[] rowBytes = getRowBytesWithPartition(ek);//require all EK fields
		boolean includeTrailingSeparator = ! wildcardLastField;
		byte[] pkQualifierBytes = FieldTool.getConcatenatedValueBytes(pkPrefix.getPostEntityKeyFields(), true, 
				includeTrailingSeparator);
		byte[] qualifierPrefix = ByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), pkQualifierBytes);
		Get get = new Get(rowBytes);
		get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
		//TODO obey config.getLimit()
		return get;
	}

	public Scan getPrefixScan(PK pkPrefix, boolean wildcardLastField, Config config){
		Scan scan = HBaseQueryBuilder.getPrefixScanner(pkPrefix, wildcardLastField, config);
		scan.setFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
		return scan;
	}
	
	public boolean isSingleEkPrefixQuery(PK pk, boolean wildcardLastField){
		EK ek = pk.getEntityKey();
		List<Field<?>> ekFields = ek.getFields();
		List<Field<?>> pkFields = pk.getFields();
		int numNonNullPkFields = FieldTool.countNonNullLeadingFields(pkFields);
		if(numNonNullPkFields > ekFields.size()){ return true; }
		if(numNonNullPkFields == ekFields.size() && ! wildcardLastField){ return true; }
		return false;//spans multiple entities
	}
	
	public Get getSingleRowRange(EK ek, Range<PK> pkRange, boolean keysOnly){
		Get get = new Get(getRowBytesWithPartition(ek));
		ColumnRangeFilter columnRangeFilter = getColumnRangeFilter(pkRange);
		if(keysOnly){
			FilterList filterList = new FilterList();
			filterList.addFilter(new KeyOnlyFilter());
			filterList.addFilter(columnRangeFilter);
			get.setFilter(filterList);
		}else{
			get.setFilter(columnRangeFilter);
		}
		return get;
	}
	
	
	/***************** partitioned *******************************/

	public List<BatchingSortedScanner<PK>> getPkScanners(HBaseSubEntityReaderNode<EK,E,PK,D,F> node, 
			Range<PK> range, Config pConfig){
		EntityPartitioner<EK> partitioner = entityFieldInfo.getEntityPartitioner();
		List<BatchingSortedScanner<PK>> scanners = new ArrayList<>();
		for(int partition=0; partition < partitioner.getNumPartitions(); ++partition){
			BatchLoader<PK> firstBatchLoader = new HBaseEntityPrimaryKeyBatchLoader<EK,E,PK,D,F>(node, partition, 
					range, pConfig, 1L);//start the counter at 1
			BatchingSortedScanner<PK> scanner = new BatchingSortedScanner<PK>(node.getClient().getExecutorService(), 
					firstBatchLoader);
			scanners.add(scanner);
		}
		return scanners;
	}

	public List<BatchingSortedScanner<D>> getDatabeanScanners(HBaseSubEntityReaderNode<EK,E,PK,D,F> node, 
			Range<PK> range, Config pConfig){
		EntityPartitioner<EK> partitioner = entityFieldInfo.getEntityPartitioner();
		List<BatchingSortedScanner<D>> scanners = new ArrayList<>();
		for(int partition=0; partition < partitioner.getNumPartitions(); ++partition){
			BatchLoader<D> firstBatchLoader = new HBaseEntityDatabeanBatchLoader<EK,E,PK,D,F>(node, partition, 
					range, pConfig, 1L);//start the counter at 1
			BatchingSortedScanner<D> scanner = new BatchingSortedScanner<D>(node.getClient().getExecutorService(), 
					firstBatchLoader);
			scanners.add(scanner);
		}
		return scanners;
	}
	
//	public BatchingSortedScanner<D> getScanner(HBaseSubEntityReaderNode<EK,E,PK,D,F> node, Range<PK> range, 
//			Config pConfig){
//		BatchLoader<D> firstBatchLoader = new HBaseEntityDatabeanBatchLoader<EK,E,PK,D,F>(node, range, pConfig, 1L);//start the counter at 1
//		BatchingSortedScanner<D> scanner = new BatchingSortedScanner<D>(node.getClient().getExecutorService(), 
//				firstBatchLoader);
//		return scanner;
//	}
}
