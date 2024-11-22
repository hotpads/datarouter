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
package io.datarouter.nodewatch.storage.binarydto.storagestats.table;

import java.util.List;
import java.util.Optional;

import io.datarouter.nodewatch.config.DatarouterStorageStatsDirectorySupplier;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TableStorageStatsBinaryDao{

	private final Directory directory;

	@Inject
	public TableStorageStatsBinaryDao(
			DatarouterStorageStatsDirectorySupplier directorySupplier){
		directory = directorySupplier.getStorageStatsTableDirectory();
	}

	public PathbeanKey makeKey(PhysicalSortedStorageReaderNode<?,?,?> node){
		String clientTypeName = node.getClientType().getName();
		String clientName = node.clientAndTableNames().client();
		String tableName = node.clientAndTableNames().table();
		//TODO clientType is probably not needed in the path
		var subpath = new Subpath(clientTypeName).append(clientName);
		return PathbeanKey.of(subpath, tableName);
	}

	public void saveTableDto(
			PhysicalSortedStorageReaderNode<?,?,?> node,
			TableStorageStatsBinaryDto dto){
		directory.write(makeKey(node), dto.encodeIndexed());
	}

	public Optional<TableStorageStatsBinaryDto> find(
			PhysicalSortedStorageReaderNode<?,?,?> node){
		return directory.read(makeKey(node))
				.map(TableStorageStatsBinaryDto::decode);

	}

	public Scanner<TableStorageStatsBinaryDto> scanTableSummaryDtos(
			ClientType<?,?> clientType,
			List<PhysicalSortedStorageReaderNode<?,?,?>> nodes){
		return Scanner.of(nodes)
				.concatOpt(node -> directory.read(PathbeanKey.of(
						new Subpath(clientType.getName())
								.append(node.clientAndTableNames().client()),
						node.clientAndTableNames().table())))
				.map(TableStorageStatsBinaryDto::decode);
	}

}
