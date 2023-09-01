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

import java.time.LocalDate;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportDirectorySupplier;
import io.datarouter.plugin.dataexport.util.DatabeanExportFilenameTool;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.node.op.raw.EncodedBlobStorage;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.util.BlockfileDirectoryStorage;
import io.datarouter.storage.util.Subpath;
import io.datarouter.types.Ulid;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatabeanExportKvFileStorageService{

	@Inject
	private ServiceName serviceNameSupplier;
	@Inject
	private DatarouterDataExportDirectorySupplier directorySupplier;

	/*-------- Directory -----------*/

	public Directory makeExportIdDirectory(Ulid exportId){
		LocalDate localDateUtc = exportId.getInstant().atZone(ZoneIds.UTC).toLocalDate();
		return directorySupplier.getDirectory()
				.subdirectory(new Subpath(serviceNameSupplier.get()))
				.subdirectory(new Subpath(localDateUtc.toString()))
				.subdirectory(new Subpath(exportId.value()));
	}

	public Directory makeExportMetaDirectory(Ulid exportId){
		return makeExportIdDirectory(exportId)
				.subdirectory(DatabeanExportFilenameTool.META_SUBPATH);
	}

	public EncodedBlobStorage<BinaryDictionary> makeMetaDictionaryStorage(Ulid exportId){
		return makeExportMetaDirectory(exportId).encoded(BinaryDictionary.CODEC);
	}

	public Directory makeTableDataDirectory(Ulid exportId, PhysicalNode<?,?,?> node){
		return makeExportIdDirectory(exportId)
				.subdirectory(DatabeanExportFilenameTool.DATA_SUBPATH)
				.subdirectory(new Subpath(DatabeanExportFilenameTool.makeClientAndTableName(node)));
	}

	/*-------- BlockfileStorage -----------*/

	public BlockfileStorage makeExportIdStorage(Ulid exportId){
		Directory exportIdDirectory = makeExportIdDirectory(exportId);
		return new BlockfileDirectoryStorage(exportIdDirectory);
	}

	public BlockfileStorage makeTableDataStorage(Ulid exportId, PhysicalNode<?,?,?> node){
		Directory tableDataDirectory = makeTableDataDirectory(exportId, node);
		return new BlockfileDirectoryStorage(tableDataDirectory);
	}

}
