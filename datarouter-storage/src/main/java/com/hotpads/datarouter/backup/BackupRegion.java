package com.hotpads.datarouter.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.PrimaryKeyStringConverter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrNumberFormatter;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.number.VarLong;

public abstract class BackupRegion<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
	protected static Logger logger = LoggerFactory.getLogger(BackupRegion.class);

	public static final Config DATABEAN_CONFIG = new Config()
			.setIterateBatchSize(1000)
			.setNumAttempts(30)
			.setTimeout(10, TimeUnit.SECONDS);

	public static final Config ENTITY_CONFIG = new Config()
			.setIterateBatchSize(5)//small
			.setNumAttempts(30)
			.setTimeout(10, TimeUnit.SECONDS);

	public static final int GZIP_BUFFER_BYTES = 256<<10;

	private final SortedStorageReaderNode<PK,D> node;
	private final Config config;
	private final PK startKeyInclusive;
	private final PK endKeyExclusive;
	private final Predicate<D> predicate;
	private final long maxRows;

	protected OutputStream os;

	protected Long rawBytes = 0L;
	protected Long compressedBytes = 0L;
	protected Long numRecords = 0L;

	public BackupRegion(SortedStorageReaderNode<PK,D> node, Config config, PK startKeyInclusive, PK endKeyExclusive,
			Predicate<D> predicate, long maxRows){
		this.node = node;
		this.config = config;
		this.startKeyInclusive = startKeyInclusive;
		this.endKeyExclusive = endKeyExclusive;
		this.predicate = predicate;
		this.maxRows = maxRows;
	}

	public BackupRegion(SortedStorageReaderNode<PK,D> node, Config config, String startKeyInclusive,
			String endKeyExclusive, Predicate<D> predicate, long maxRows){
		this(node, config, convertStringToPk(startKeyInclusive, node), convertStringToPk(endKeyExclusive, node),
				predicate, maxRows);
	}

	public abstract void execute() throws IOException;

	protected void exportWithoutClosingOutputStream() throws IOException{
		System.out.println("called BackupRegion");
		System.out.println("predicate "+predicate);
		Iterable<D> iterable = node.scan(Range.create(startKeyInclusive, true, endKeyExclusive, false), config);
		if( ! node.getFieldInfo().getFieldAware()){
			throw new IllegalArgumentException("databeans must be field aware");
		}
		for(D databean : DrIterableTool.nullSafe(iterable)){
			if(predicate != null && !predicate.test(databean)){
				continue;
			}
			//include zero-length fields in key bytes
			byte[] bytes = DatabeanTool.getBytes(databean, node.getFieldInfo().getSampleFielder());
			VarLong length = new VarLong(DrArrayTool.length(bytes));
			os.write(length.getBytes());
			os.write(bytes);
			++numRecords;
			rawBytes += length.getValue() + length.getNumBytes();
			if(numRecords % 10000 == 0){
				String numRecordsString = DrNumberFormatter.addCommas(numRecords);
				String numBytesString = DrNumberFormatter.addCommas(rawBytes)+"b";
				String maxRowsString = maxRows == Long.MAX_VALUE ? "" : "/" + DrNumberFormatter.addCommas(maxRows);
				logger.warn("exported {}{}, {} from {} {}", numRecordsString, maxRowsString, numBytesString,
						node.getName(), databean);
			}
			if(numRecords >= maxRows){
				break;
			}
		}
	}

	public Long getRawBytes(){
		return rawBytes;
	}

	public Long getCompressedBytes(){
		return compressedBytes;
	}

	public Long getNumRecords(){
		return numRecords;
	}

	protected static <PK extends PrimaryKey<PK>> PK convertStringToPk(String stringKey, Node<PK,?> node){
		return PrimaryKeyStringConverter.primaryKeyFromString(node.getFieldInfo().getPrimaryKeyClass(), node
				.getFieldInfo().getSamplePrimaryKey(), stringKey);
	}

}
