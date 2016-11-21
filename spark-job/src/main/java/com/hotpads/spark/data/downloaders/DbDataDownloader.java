package com.hotpads.spark.data.downloaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.serialize.TsvDatabeanTool;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.spark.data.ProgressListener;
import com.hotpads.util.core.collections.Pair;

public class DbDataDownloader<PK extends PrimaryKey<PK>,D extends Databean<PK, D>> extends Observable
implements DataDownloader {
	private static final Logger logger = LoggerFactory.getLogger(DbDataDownloader.class);
	private static final String FILE_PATH_SEPERATOR = File.separator;
	// this will be compressed later
	private static final long FILE_SPLIT_SIZE_IN_BYTES = 160 * 1024 * 1024;
	private static final int PRINT_EVERY = 10000;
	private final Config config = new Config().setSlaveOk(true).setIterateBatchSize(10000);
	private final List<ProgressListener<String>> listeners = new LinkedList<>();
	private final SortedStorageReader<PK, D> reader;
	private final String outputDirPath;
	private final String jobId;
	private final String tableName;
	private final String columnsToSelect;
	private final DatabeanFielder<PK, D> fielder;

	public DbDataDownloader(String jobId, String tableName, String columnsToSelect, DatabeanFielder<PK, D> fielder,
			SortedStorageReader<PK, D> reader, String outputDirPath){
		this.outputDirPath = outputDirPath;
		this.jobId = jobId;
		this.reader = reader;
		this.tableName = tableName;
		this.columnsToSelect = columnsToSelect;
		this.fielder = fielder;
	}

	private void downloadData() {
		logger.info("About to download table  with columns into output dir {}", outputDirPath);
		String tablePath = outputDirPath + FILE_PATH_SEPERATOR + jobId + FILE_PATH_SEPERATOR + tableName;
		PrintWriter writer = null;
		try {
			Iterable<D> rows = reader.scan(null, config);
			int rowCtr = 0;
			int partNum = 1;
			Pair<String, PrintWriter> filePathToWriterPair = getFilePathToWriter(tablePath, partNum);
			writer = filePathToWriterPair.getRight();
			long currentFileSizeInBytes = 0;
			boolean listenersUpdated = true;
			for(D databean : rows) {
				listenersUpdated = false;
				String row = TsvDatabeanTool.databeanToTsv(columnsToSelect.split(","), databean, fielder);
				writer.println(row);
				currentFileSizeInBytes += row.getBytes().length;
				if(rowCtr % PRINT_EVERY == 0) {
					logger.info("Written " + rowCtr + " lines");
				}
				if(currentFileSizeInBytes > FILE_SPLIT_SIZE_IN_BYTES) {
					currentFileSizeInBytes = 0;
					updateListeners(filePathToWriterPair.getLeft());
					listenersUpdated = true;
					partNum++;
					writer.close();
					filePathToWriterPair =  getFilePathToWriter(tablePath, partNum);
					writer = filePathToWriterPair.getRight();
				}
			}

			if(!listenersUpdated) {
				updateListeners(filePathToWriterPair.getLeft());
			}

			logger.info("Downloaded table {} with columns {} into output dir {}", tableName, columnsToSelect,
					outputDirPath);
		}catch(Exception e) {
			logger.error("Exception while downloading data from DB: " + e.getMessage());
		}finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	private Pair<String, PrintWriter> getFilePathToWriter(String tablePath,int partNum) throws IOException {
		File tableFile = new File(tablePath + FILE_PATH_SEPERATOR + tableName + "-" + partNum + ".tsv");
		tableFile.getParentFile().mkdirs();
		tableFile.createNewFile();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(tableFile)));
		return new Pair<>(tableFile.getPath(), writer);
	}

	@Override
	public void addProgressListener(ProgressListener<String> listener) {
		listeners.add(listener);
	}

	private void updateListeners(String data) {
		for(ProgressListener<String> listener : listeners) {
			listener.update(data);
		}
	}

	@Override
	public Void call() {
		downloadData();
		return null;
	}
}
