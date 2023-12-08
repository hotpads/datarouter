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
package io.datarouter.nodewatch.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.datarouter.bytes.ByteLength;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto.ColumnStorageStatsBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.type.index.ManagedNode;

public class TableStorageSizeTool{

	public record IndexSize(
			String indexName,
			ByteLength totalSize,
			ByteLength avgRowSize){
	}

	public static IndexSize calcIndexSize(
			TableStorageStatsBinaryDto stats,
			ManagedNode<?,?,?,?,?> managedNode,
			long totalRows){
		List<String> columnNames = managedNode.getIndexEntryFieldInfo().getFieldColumnNames();
		Map<String,ColumnStorageStatsBinaryDto> columnStatsByName = Scanner.of(stats.columns)
				.toMap(columnStats -> columnStats.name);
		List<ColumnStorageStatsBinaryDto> columns = Scanner.of(columnNames)
				.map(columnStatsByName::get)
				.exclude(Objects::isNull)
				.list();
		long avgRowSize = columns.stream()
				.mapToLong(column -> column.avgValueBytes)
				.sum();
		long totalSize = avgRowSize * totalRows;
		return new IndexSize(
				managedNode.getName(),
				ByteLength.ofBytes(totalSize),
				ByteLength.ofBytes(avgRowSize));
	}

	public static ByteLength calcTotalIndexSize(
			TableStorageStatsBinaryDto stats,
			List<? extends ManagedNode<?,?,?,?,?>> managedNodes,
			long totalRows){
		return Scanner.of(managedNodes)
				.map(managedNode -> calcIndexSize(stats, managedNode, totalRows))
				.map(IndexSize::totalSize)
				.listTo(ByteLength::sum);
	}

}
