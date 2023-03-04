/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.plugin.dataexport.service;

import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Range;

public class DatabeanExportToDirectory<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanExportToDirectory.class);

	public static final String MIGRATION_EXPORT_FOLDER = "migration";

	private final Directory directory;
	private final String exportId;
	private final String key;
	private final Threads writeParallelThreads;
	private final PhaseTimer timer;
	private final DatabeanExport<PK,D,F> backupRegion;

	public DatabeanExportToDirectory(
			Directory directory,
			String exportId,
			SortedStorageReaderNode<PK,D,F> node,
			Config config,
			Range<String> range,
			Predicate<D> predicate,
			long maxRows,
			ExecutorService prefetchExec,
			Threads writeParallelThreads,
			PhaseTimer timer){
		this.directory = directory;
		this.exportId = exportId;
		this.key = makeKeySuffix(exportId, node);
		this.writeParallelThreads = writeParallelThreads;
		this.timer = timer;
		backupRegion = new DatabeanExport<>(
				exportId,
				node,
				config,
				range.map(stringKey -> convertStringToPk(stringKey, node)),
				predicate,
				maxRows,
				prefetchExec);
	}

	public void execute(){
		PathbeanKey pathbeanKey = PathbeanKey.of(key);
		directory.writeParallel(
				pathbeanKey,
				backupRegion.makeGzipInputStream(),
				writeParallelThreads,
				ByteLength.MIN);
		String timerMessage = String.format(
				"exported %s, %s bytes",
				NumberFormatter.addCommas(backupRegion.getNumRecords()),
				NumberFormatter.addCommas(backupRegion.getRawBytes()));
		timer.add(timerMessage);
		logger.warn("exportId={}, timer={}", exportId, timer.toString());
	}

	public String getFullKey(){
		return directory.getRootPath() + key;
	}

	public static String makeKeySuffix(String exportId, Node<?,?,?> node){
		return exportId + "/" + node.getName();
	}

	public static <PK extends PrimaryKey<PK>> PK convertStringToPk(String stringKey, Node<PK,?,?> node){
		return PrimaryKeyPercentCodecTool.decode(node.getFieldInfo().getPrimaryKeySupplier(), stringKey);
	}

	public long getNumRecords(){
		return backupRegion.getNumRecords();
	}

}
