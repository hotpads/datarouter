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
import java.util.function.Function;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.nodewatch.shadowtable.codec.ShadowTableDictionaryCodec.ColumnNamesDictionaryCodec;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableDirectorySupplier;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableRangeDecodeExecutor;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableRangeReadExecutor;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.BlockfileDirectoryStorage;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ShadowTableRangeBlockfileDao{

	public static final ByteLength BLOCK_SIZE = ByteLength.ofKiB(16);
	private static final ByteLength READ_CHUNK_SIZE = ByteLength.ofMiB(16);
	private static final int BLOCK_DECODE_BATCH_SIZE = 8;
	private static final int NUM_CONCURRENT_FILES_WHILE_CONCATENATING = 3;

	private final Directory rangeDirectory;
	private final ExecutorService rangeReadExec;
	private final ExecutorService rangeDecodeExec;

	@Inject
	public ShadowTableRangeBlockfileDao(
			DatarouterShadowTableDirectorySupplier shadowTableDirectorySupplier,
			ShadowTableRangeReadExecutor rangeReadExec,
			ShadowTableRangeDecodeExecutor rangeDecodeExec){
		this.rangeDirectory = shadowTableDirectorySupplier.getRangeDirectory();
		this.rangeReadExec = rangeReadExec;
		this.rangeDecodeExec = rangeDecodeExec;
	}

	/*----------- write -----------*/

	public void writeBlockfile(
			ShadowTableExport export,
			String exportId,
			PhysicalNode<?,?,?> node,
			boolean enableCompression,
			long rangeId,
			long numRanges,
			Scanner<List<BlockfileRow>> rowBatches){
		Directory tableDirectory = makeTableDirectory(export, exportId, node.clientAndTableNames().table());
		var tableBlockfileStorage = new BlockfileDirectoryStorage(tableDirectory);
		BlockfileCompressor compressor = enableCompression
				? BlockfileStandardCompressors.GZIP
				: BlockfileStandardCompressors.NONE;
		String rangeFilename = makeFilename(rangeId, numRanges);
		var headerDictionary = new BinaryDictionary();
		ColumnNamesDictionaryCodec.addToDictionary(node.getFieldInfo().getFieldColumnNames(), headerDictionary);
		new BlockfileGroupBuilder<BlockfileRow>(tableBlockfileStorage)
				.build()
				.newWriterBuilder(rangeFilename)
				.setCompressor(compressor)
				.setHeaderDictionary(headerDictionary)
				.build()
				.writeBlocks(rowBatches);
	}

	/*----------- read -----------*/

	public long numFiles(
			ShadowTableExport export,
			String exportId,
			String tableName){
		return makeTableDirectory(export, exportId, tableName)
				.scanKeys(Subpath.empty())
				.findFirst()
				.map(ShadowTableRangeBlockfileDao::parseNumRanges)
				.orElseThrow();
	}

	public boolean isComplete(
			ShadowTableExport export,
			String exportId,
			String tableName){
		Directory tableDirectory = makeTableDirectory(export, exportId, tableName);
		List<PathbeanKey> keys = tableDirectory.scanKeys(Subpath.empty()).list();
		if(keys.isEmpty()){
			return false;
		}
		long numRanges = parseNumRanges(keys.getFirst());
		return keys.size() == numRanges;
	}

	public Scanner<BlockfileRow> scanConcatenatedRangeRows(
			ShadowTableExport export,
			String exportId,
			String tableName){
		BlockfileGroup<BlockfileRow> blockfileGroup = makeBlockfileGroup(export, exportId, tableName);
		long numFiles = numFiles(export, exportId, tableName);
		return Scanner.iterate(1, rangeId -> rangeId + 1)
				.limit(numFiles)
				.map(rangeId -> makeFilename(rangeId, numFiles))
				.map(filename -> blockfileGroup.newReaderBuilder(filename, Function.identity())
						.setReadThreads(new Threads(rangeReadExec, export.resource().threads()))
						.setReadChunkSize(READ_CHUNK_SIZE)
						.setDecodeBatchSize(BLOCK_DECODE_BATCH_SIZE)
						.setDecodeThreads(new Threads(rangeDecodeExec, export.resource().threads()))
						.build()
						.sequential()
						.scan())
				// Parallel peek: trigger loading the next files so we don't have a pause between them.
				.parallelOrdered(new Threads(rangeReadExec, NUM_CONCURRENT_FILES_WHILE_CONCATENATING))
				// .map(), not .each(), to get the new scanner with the peeked item re-inserted
				.map(rangeFileScanner -> rangeFileScanner.peekFirst($ -> {}))
				.concat(Function.identity());
	}

	/*------------ private ------------*/

	private Directory makeTableDirectory(
			ShadowTableExport export,
			String exportId,
			String tableName){
		return rangeDirectory
				.subdirectoryBuilder(new Subpath(export.name()))
				.withCounterName("shadowTableExport " + export.name())
				.build()
		.subdirectory(new Subpath(exportId))
		.subdirectory(new Subpath(tableName));
	}

	private BlockfileGroup<BlockfileRow> makeBlockfileGroup(
			ShadowTableExport export,
			String exportId,
			String tableName){
		Directory tableDirectory = makeTableDirectory(export, exportId, tableName);
		var tableBlockfileStorage = new BlockfileDirectoryStorage(tableDirectory);
		return new BlockfileGroupBuilder<BlockfileRow>(tableBlockfileStorage).build();
	}

	private static String makeFilename(long rangeId, long numRanges){
		int length = Long.toString(numRanges).length();
		String paddedRangeId = StringTool.pad(Long.toString(rangeId), '0', length);
		return paddedRangeId + "-of-" + numRanges;
	}

	private static long parseRangeId(PathbeanKey pathbeanKey){
		String filename = pathbeanKey.getFile();
		int firstDash = filename.indexOf('-');
		return Long.valueOf(filename.substring(0, firstDash));
	}

	private static long parseNumRanges(PathbeanKey pathbeanKey){
		String filename = pathbeanKey.getFile();
		int lastDash = filename.lastIndexOf('-');
		return Long.valueOf(filename.substring(lastDash + 1, filename.length()));
	}
}
