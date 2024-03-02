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
package io.datarouter.bytes.blockfile.encoding.valueblock;

import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Scanner;

public interface BlockfileValueBlockCodec{

	public record BlockfileValueBlockRows(
			long valueBlockId,
			long firstRowIdInBlock,
			List<BlockfileRow> rows){

		public int rowIndex(long rowId){
			return (int)(rowId - firstRowIdInBlock);
		}
	}

	public record BlockfileEncodedValueBlock(
			byte[] bytes){
	}

	BlockfileEncodedValueBlock encode(BlockfileValueBlockRows input);

	BlockfileValueBlockRows decode(
			BlockfileEncodedValueBlock encodedValue,
			BlockfileKeyRange keyRange);

	default BlockfileRow row(
			BlockfileEncodedValueBlock encodedValue,
			long rowId){
		BlockfileValueBlockRows valueBlockRows = decode(encodedValue, BlockfileKeyRange.everything());
		int rowIndex = valueBlockRows.rowIndex(rowId);
		return valueBlockRows.rows().get(rowIndex);
	}

	Scanner<BlockfileRow> scanAllVersions(
			BlockfileEncodedValueBlock encodedBlock,
			byte[] key);

	Optional<BlockfileRow> findLatestVersion(
			BlockfileEncodedValueBlock encodedBlock,
			byte[] key);

	default boolean containsKey(
			BlockfileEncodedValueBlock encodedBlock,
			byte[] key){
		return findLatestVersion(encodedBlock, key).isPresent();
	}

}
