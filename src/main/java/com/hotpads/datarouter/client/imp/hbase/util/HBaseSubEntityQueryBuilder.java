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
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.ByteRange;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.collections.Twin;
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
	
	
	/******************* rows ****************************/
	
	public boolean isSingleEntity(Range<PK> pkRange){
		Range<EK> ekRange = getEkRange(pkRange);
		return ekRange.hasStart() && ekRange.equalsStartEnd();
	}
	
	public Range<EK> getEkRange(Range<PK> pkRange){
		EK start = pkRange.hasStart() ? pkRange.getStart().getEntityKey() : null;
		EK end = pkRange.hasEnd() ? pkRange.getEnd().getEntityKey() : null;
		return Range.create(start, pkRange.getStartInclusive(), end, pkRange.getEndInclusive());
	}
	
	public Range<ByteRange> getRowRange(int partition, Range<PK> pkRange){
		byte[] partitionPrefix = partitioner.getPrefix(partition);
		ByteRange startBytes = new ByteRange(partitionPrefix);
		if(pkRange.hasStart()){
			EK startEk = pkRange.getStart().getEntityKey();
			byte[] startByteArray = DrByteTool.concatenate(partitionPrefix, getRowBytes(startEk));
			startBytes = new ByteRange(startByteArray);
		}
		
		ByteRange endBytes = null;
		if(pkRange.hasEnd()){
			EK endEk = pkRange.getEnd().getEntityKey();
			byte[] endByteArray = DrByteTool.concatenate(partitionPrefix, getRowBytes(endEk));
			endBytes = new ByteRange(endByteArray);
		}else if(!partitioner.isLastPartition(partition)){
			byte[] nextPartitionPrefix = partitioner.getNextPrefix(partition);
			endBytes = new ByteRange(nextPartitionPrefix);
		}//else no end
		return Range.create(startBytes, pkRange.getStartInclusive(), endBytes, pkRange.getEndInclusive());
	}
	
	/******************** qualifiers ***********************/
	
	public byte[] getQualifier(PK primaryKey, String fieldName){
		return DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey, true),
				StringByteTool.getUtf8Bytes(fieldName));
	}
	
	public byte[] getQualifierPrefix(PK primaryKey){
		return DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), getQualifierPkBytes(primaryKey, false));
	}
	
	public byte[] getQualifierPkBytes(PK primaryKey, boolean trailingSeparatorAfterEndingString){
		if(primaryKey==null){ return new byte[]{}; }
		return FieldTool.getConcatenatedValueBytes(primaryKey.getPostEntityKeyFields(), true, 
				trailingSeparatorAfterEndingString);
	}

	
	/********************* prefix bound logic **********************/
	
	public boolean isSingleEkPrefixQuery(PK pk, boolean wildcardLastField){
		EK ek = pk.getEntityKey();
		List<Field<?>> ekFields = ek.getFields();
		List<Field<?>> pkFields = pk.getFields();
		int numNonNullPkFields = FieldTool.countNonNullLeadingFields(pkFields);
		if(numNonNullPkFields > ekFields.size()){ return true; }
		if(numNonNullPkFields == ekFields.size() && ! wildcardLastField){ return true; }
		return false;//spans multiple entities
	}
	
	
	/******************* get / getMulti ***************************/
	
	public List<Get> getGets(Collection<PK> pks, boolean keysOnly){
		List<Get> gets = DrListTool.createArrayListWithSize(pks);
		for(PK pk : pks){
			byte[] rowBytes = getRowBytesWithPartition(pk.getEntityKey());
			byte[] qualifierPrefix = getQualifierPrefix(pk);
			Get get = new Get(rowBytes);
			if(keysOnly){
				FilterList filters = new FilterList();
				filters.addFilter(new KeyOnlyFilter());
				filters.addFilter(new ColumnPrefixFilter(qualifierPrefix));
				get.setFilter(filters);
			}else{
				get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
			}
			gets.add(get);
		}
		return gets;
	}
	
	
	/********************* single row prefix **********************/
	
	public List<Get> getPrefixGets(Collection<PK> prefixes, boolean wildcardLastField, Config config){
		List<Get> gets = new ArrayList<>();
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
		byte[] qualifierPrefix = DrByteTool.concatenate(fieldInfo.getEntityColumnPrefixBytes(), pkQualifierBytes);
		Get get = new Get(rowBytes);
		get.setFilter(new ColumnPrefixFilter(qualifierPrefix));
		//TODO obey config.getLimit()
		return get;
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
	
	public ColumnRangeFilter getColumnRangeFilter(Range<PK> pkRange){
		byte[] start = getQualifierPrefix(pkRange.getStart());
		byte[] end = getQualifierPrefix(pkRange.getEnd());
		return new ColumnRangeFilter(start, pkRange.getStartInclusive(), end, pkRange.getEndInclusive());
	}
	
	
	/************** multi row prefix ********************/

	public List<Scan> getPrefixScans(EK prefix, boolean wildcardLastField, Config config){
		List<Scan> scans = new ArrayList<>();
		for(int partition=0; partition < partitioner.getNumPartitions(); ++partition){
			Twin<ByteRange> rowBounds = HBaseQueryBuilder.getStartEndBytesForPrefix(prefix.getFields(), 
					wildcardLastField);
			Scan scan = getScan(partition, rowBounds, config);
			scans.add(scan);
		}
		return scans;
	}
		
	private Twin<ByteRange> addPrefixToRowBounds(int partition, Twin<ByteRange> rowBounds){
		return Twin.createTwin(addPartitionPrefix(partition, rowBounds.getLeft()), 
				addPartitionPrefix(partition, rowBounds.getRight()));
	}
	
	private ByteRange addPartitionPrefix(int partition, ByteRange in){
		return new ByteRange(DrByteTool.concatenate(partitioner.getPrefix(partition), in.copyToNewArray()));
	}
	
	private Scan getScan(int partition, Twin<ByteRange> rowBounds, Config config){
		Twin<ByteRange> prefixedRowBounds = addPrefixToRowBounds(partition, rowBounds);
		Range<ByteRange> prefixedRowRange = Range.create(prefixedRowBounds.getLeft(), true, prefixedRowBounds.getRight(), 
				false);
		Scan scan = HBaseQueryBuilder.getScanForRange(prefixedRowRange, config);
		return scan;
	}
	
	
	/***************** batching scanners *******************/

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
	
	
	/************* get results in sub range ********************/
	
	public Scan getScanForSubrange(final int partition, final Range<PK> rowRange, final Config pConfig, boolean keysOnly){
		Config config = Config.nullSafe(pConfig);
		Range<ByteRange> rowBytesRange = getRowRange(partition, rowRange);
		//TODO Get if single row
		Scan scan = HBaseQueryBuilder.getScanForRange(rowBytesRange, config);
		FilterList filterList = new FilterList();
		if(keysOnly){ filterList.addFilter(new KeyOnlyFilter()); }
		filterList.addFilter(new ColumnPrefixFilter(fieldInfo.getEntityColumnPrefixBytes()));
		scan.setFilter(filterList);
		return scan;
	}
	
}
