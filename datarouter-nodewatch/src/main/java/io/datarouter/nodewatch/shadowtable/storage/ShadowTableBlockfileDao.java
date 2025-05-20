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
package io.datarouter.nodewatch.shadowtable.storage;

import java.util.List;
import java.util.concurrent.ExecutorService;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.nodewatch.shadowtable.codec.ShadowTableStatefulDictionaryCodec.ColumnNamesDictionaryCodec;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableDirectorySupplier;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableTableEncodeExecutor;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableTableWriteExecutor;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.BlockfileDirectoryStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ShadowTableBlockfileDao{

	private static final int BLOCK_BATCH_SIZE = 16;
	// Number of parallel multipart uploads stored in memory.  These can be hundreds of MiB each.
	private static final int MAX_WRITE_THREADS = 16;

	private final DatarouterShadowTableDirectorySupplier directorySupplier;
	private final ExecutorService tableEncodeExec;
	private final ExecutorService tableWriteExec;

	@Inject
	public ShadowTableBlockfileDao(
			DatarouterShadowTableDirectorySupplier directorySupplier,
			ShadowTableTableEncodeExecutor tableEncodeExec,
			ShadowTableTableWriteExecutor tableWriteExec){
		this.directorySupplier = directorySupplier;
		this.tableEncodeExec = tableEncodeExec;
		this.tableWriteExec = tableWriteExec;
	}

	/*----------- write -----------*/

	public void writeBlockfile(
			ShadowTableExport export,
			String exportId,
			PhysicalNode<?,?,?> node,
			boolean enableCompression,
			Scanner<List<BlockfileRow>> blocksOfRows){
		Directory exportDirectory = directorySupplier.makeTableDirectory(export, exportId);
		var tableBlockfileStorage = new BlockfileDirectoryStorage(exportDirectory);
		BlockfileCompressor compressor = enableCompression
				? BlockfileStandardCompressors.GZIP
				: BlockfileStandardCompressors.NONE;
		String filename = node.clientAndTableNames().table();
		var headerDictionary = new BinaryDictionary();
		ColumnNamesDictionaryCodec.addToDictionary(node.getFieldInfo().getFieldColumnNames(), headerDictionary);
		int numWriteThreads = Math.max(export.resource().vcpus(), MAX_WRITE_THREADS);
		new BlockfileGroupBuilder<BlockfileRow>(tableBlockfileStorage)
				.build()
				.newWriterBuilder(filename)
				.setHeaderDictionary(headerDictionary)
				.setCompressor(compressor)
				.setEncodeBatchSize(BLOCK_BATCH_SIZE)
				.setEncodeThreads(new Threads(tableEncodeExec, 1 + export.resource().vcpus()))
				.setWriteThreads(new Threads(tableWriteExec, numWriteThreads))
				.setMinWritePartSize(ByteLength.ofMiB(100))
				.build()
				.writeBlocks(blocksOfRows);
	}

}
