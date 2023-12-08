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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.storage.client.ClientAndTableNames;

public class TableStorageStatsBinaryDto extends BinaryDto<TableStorageStatsBinaryDto>{

	@BinaryDtoField(index = 0)
	public final String clientName;
	@BinaryDtoField(index = 1)
	public final String tableName;
	@BinaryDtoField(index = 2)
	public final Long numRowsIncluded;
	@BinaryDtoField(index = 3)
	public final List<ColumnStorageStatsBinaryDto> columns;

	public TableStorageStatsBinaryDto(
			String clientName,
			String tableName,
			Long numRowsIncluded,
			List<ColumnStorageStatsBinaryDto> columns){
		this.clientName = clientName;
		this.tableName = tableName;
		this.numRowsIncluded = numRowsIncluded;
		this.columns = columns;
	}

	public static TableStorageStatsBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(TableStorageStatsBinaryDto.class).decode(bytes);
	}

	public ClientAndTableNames clientAndTableNames(){
		return new ClientAndTableNames(clientName, tableName);
	}

	public long avgNameBytesPerRow(){
		return columns.stream()
				.mapToLong(columnStats -> columnStats.avgNameBytes)
				.sum();
	}

	public long avgValueBytesPerRow(){
		return columns.stream()
				.mapToLong(columnStats -> columnStats.avgValueBytes)
				.sum();
	}

	public long avgBytesPerRow(boolean includeNameBytes){
		return includeNameBytes
				? avgValueBytesPerRow() + avgNameBytesPerRow()
				: avgValueBytesPerRow();
	}

	/*----- nested -------*/

	public static class ColumnStorageStatsBinaryDto extends BinaryDto<ColumnStorageStatsBinaryDto>{

		@BinaryDtoField(index = 0)
		public final String name;
		@BinaryDtoField(index = 1)
		public final Long avgNameBytes;
		@BinaryDtoField(index = 2)
		public final Long avgValueBytes;

		public ColumnStorageStatsBinaryDto(
				String name,
				Long avgNameBytes,
				Long avgValueBytes){
			this.name = name;
			this.avgNameBytes = avgNameBytes;
			this.avgValueBytes = avgValueBytes;
		}

		public static ColumnStorageStatsBinaryDto decode(byte[] bytes){
			return BinaryDtoIndexedCodec.of(ColumnStorageStatsBinaryDto.class).decode(bytes);
		}

	}
}
