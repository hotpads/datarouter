package com.hotpads.datarouter.client.imp.hbase.scan;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientTableNodeNames;
import com.hotpads.datarouter.client.imp.hbase.client.HBaseClient;
import com.hotpads.datarouter.client.imp.hbase.util.HBaseSubEntityQueryBuilder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.async.Ref;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.io.RuntimeIOException;
import com.hotpads.util.core.iterable.scanner.Scanner;

public class HBaseSubEntityCellScanner<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Scanner<Cell>{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSubEntityCellScanner.class);

	private static final boolean ALLOW_PARTIAL_RESULTS = true;
	private static final long MAX_RESULT_SIZE_BYTES = 1024 * 1024; // 1 MB

	private final HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder;
	private final HBaseClient client;
	private final ClientTableNodeNames clientTableNodeNames;

	private final Config config;
	private final int partition;
	private final Range<PK> range;
	private final boolean keysOnly;

	private final String scanKeysVsRowsNumRows;
	private final Ref<ResultScanner> hbaseResultScannerRef;

	private Table table;
	private List<Cell> currentBatch;
	private int currentBatchIndex;

	public HBaseSubEntityCellScanner(HBaseClient client, ClientTableNodeNames clientTableNodeNames,
			HBaseSubEntityQueryBuilder<EK,E,PK,D,F> queryBuilder, Config config, int partition, Range<PK> range,
			boolean keysOnly){
		this.client = client;
		this.clientTableNodeNames = clientTableNodeNames;
		this.queryBuilder = queryBuilder;
		this.config = config;
		this.partition = partition;
		this.range = range;
		this.keysOnly = keysOnly;

		this.scanKeysVsRowsNumRows = "scan " + (keysOnly ? "pk" : "entity") + " numRows";
		this.hbaseResultScannerRef = Lazy.of(() -> initResultScanner());
		updateCurrentBatch(null);
	}

	@Override
	public Cell getCurrent(){
		if(currentBatch == null){
			return null;
		}
		return currentBatch.get(currentBatchIndex);
	}

	@Override
	public boolean advance(){
		if(currentBatch != null && currentBatchIndex < currentBatch.size() - 1){
			++currentBatchIndex;
			return true;
		}
		return loadNextResult();
	}

	private ResultScanner initResultScanner(){
		Scan scan = queryBuilder.getScanForPartition(partition, range, config, keysOnly, ALLOW_PARTIAL_RESULTS,
				MAX_RESULT_SIZE_BYTES);
		table = client.getTable(clientTableNodeNames.getTableName());
		try{
			return table.getScanner(scan);
		}catch(IOException e){
			throw new RuntimeIOException(e);
		}

	}

	//return true if a valid (non-null and non-empty) next result was loaded
	private boolean loadNextResult(){
		Result result;
		do{
			try{
				result = hbaseResultScannerRef.get().next();
				if(result != null){
					DRCounters.incClientNodeCustom(client.getType(), scanKeysVsRowsNumRows, clientTableNodeNames
							.getClientName(), clientTableNodeNames.getNodeName());
					if(result.isPartial()){
						logger.info("partial result on {}, {}", clientTableNodeNames.getNodeName(), Bytes
								.toStringBinary(result.getRow()));
						DRCounters.incClientNodeCustom(client.getType(), "partial result", clientTableNodeNames
								.getClientName(), clientTableNodeNames.getNodeName());
					}
				}
			}catch(IOException e){
				logger.error("EXTRA DEBUG LOGGING", e);//this isn't getting logged up the call chain for some reason
				cleanup();
				throw new RuntimeIOException(e);
			}
			if(result == null){
				cleanup();
				return false;
			}
		}while(result.isEmpty());

		updateCurrentBatch(result.listCells());//this internally does Arrays.asList on a Cell[]
		return true;
	}

	private void updateCurrentBatch(List<Cell> cells){
		currentBatch = cells;
		currentBatchIndex = 0;
	}

	private void cleanup(){
		updateCurrentBatch(null);
		hbaseResultScannerRef.get().close();
		try{
			table.close();
		}catch(IOException e){
			throw new RuntimeIOException(e);
		}
	}
}