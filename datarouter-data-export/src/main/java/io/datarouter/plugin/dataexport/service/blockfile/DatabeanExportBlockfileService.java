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
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
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
public class DatabeanExportBlockfileService{

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
	Codec<D,BlockfileRow> makeBlockfileRowCodec(Node<PK,D,F> node){
		ColumnNameCodec columnNameCodec = ColumnNameCodec.createNewMappings(node.getFieldInfo());
		return new DatabeanExportCodec<>(node.getFieldInfo(), columnNameCodec);
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	BlockfileWriter<D> makeBlockfileWriter(
			BlockfileStorage tableStorage,
			Node<PK,D,F> node,
			int partId){
		var blockfileGroup = new BlockfileGroupBuilder<D>(tableStorage).build();
		var headerDictionary = new BinaryDictionary();
		ColumnNamesDictionaryCodec.addToDictionary(node.getFieldInfo().getFieldColumnNames(), headerDictionary);
		String filename = DatabeanExportFilenameTool.makePartFilename(partId);
		return blockfileGroup.newWriterBuilder(filename)
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
	BlockfileReader<D> makeBlockfileReader(
			BlockfileStorage tableStorage,
			Node<PK,D,F> node,
			int partId){
		String filename = DatabeanExportFilenameTool.makePartFilename(partId);
		BlockfileGroup<D> blockfileGroup = new BlockfileGroupBuilder<D>(tableStorage).build();
		var metadataReader = blockfileGroup.newMetadataReaderBuilder(filename).build();
		BinaryDictionary userDictionary = metadataReader.header().userDictionary();
		ColumnNameCodec columnNameCodec = ColumnNameCodec.fromBinaryDictionary(userDictionary);
		var rowCodec = new DatabeanExportCodec<>(node.getFieldInfo(), columnNameCodec);
		return blockfileGroup.newReaderBuilder(metadataReader, rowCodec::decode)
				.setReadChunkSize(ByteLength.ofMiB(4))
				.setReadThreads(new Threads(readExec, 4))
				.setDecodeThreads(new Threads(decodeExec, 2))
				.build();
	}

}
