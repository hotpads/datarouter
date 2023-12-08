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
package io.datarouter.plugin.dataexport.service.blockfile;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.compress.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.kvfile.blockformat.KvFileStandardBlockFormats;
import io.datarouter.bytes.kvfile.io.KvFileBuilder;
import io.datarouter.bytes.kvfile.io.read.KvFileReader;
import io.datarouter.bytes.kvfile.io.write.KvFileWriter;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanExportEncodeExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanImportDecodeExecutor;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportExecutors.DatabeanImportReadExecutor;
import io.datarouter.plugin.dataexport.util.DatabeanExportCodec;
import io.datarouter.plugin.dataexport.util.DatabeanExportCodec.ColumnNameCodec;
import io.datarouter.plugin.dataexport.util.DatabeanExportCodec.ColumnNamesDictionaryCodec;
import io.datarouter.plugin.dataexport.util.DatabeanExportFilenameTool;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.node.Node;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatabeanExportKvFileService{

	@Inject
	private DatabeanExportEncodeExecutor encodeExec;
	@Inject
	private DatabeanImportReadExecutor readExec;
	@Inject
	private DatabeanImportDecodeExecutor decodeExec;

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	KvFileWriter<D> makeKvFileWriter(
			BlockfileStorage tableStorage,
			Node<PK,D,F> node,
			int partId){
		var kvFile = new KvFileBuilder<D>(tableStorage).build();
		ColumnNameCodec columnNameCodec = ColumnNameCodec.createNewMappings(node.getFieldInfo());
		var kvCodec = new DatabeanExportCodec<>(node.getFieldInfo(), columnNameCodec);
		var headerDictionary = new BinaryDictionary();
		ColumnNamesDictionaryCodec.addToDictionary(node.getFieldInfo().getFieldColumnNames(), headerDictionary);
		String filename = DatabeanExportFilenameTool.makePartFilename(partId);
		return kvFile.newWriterBuilder(
				filename,
				kvCodec,
				KvFileStandardBlockFormats.SEQUENTIAL)
				.setCompressor(BlockfileStandardCompressors.GZIP)
				.setHeaderDictionary(headerDictionary)
				.setEncodeThreads(new Threads(encodeExec, 4))
				// Not using parallel writes to conserve memory since we have parallel exports
				.build();
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	KvFileReader<D> makeKvFileReader(
			BlockfileStorage tableStorage,
			Node<PK,D,F> node,
			int partId){
		var kvFile = new KvFileBuilder<D>(tableStorage).build();
		String filename = DatabeanExportFilenameTool.makePartFilename(partId);
		var metadataReader = kvFile.newMetadataReader(filename);
		BinaryDictionary userDictionary = metadataReader.header().userDictionary();
		ColumnNameCodec columnNameCodec = ColumnNameCodec.fromBinaryDictionary(userDictionary);
		var kvCodec = new DatabeanExportCodec<>(node.getFieldInfo(), columnNameCodec);
		return kvFile.newReaderBuilder(filename, kvCodec)
				.setReadChunkSize(ByteLength.ofMiB(4))
				.setReadThreads(new Threads(readExec, 4))
				.setDecodeThreads(new Threads(decodeExec, 2))
				.build();
	}

}
