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
package io.datarouter.bytes.blockfile.block.tokens;

import java.util.List;

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.index.BlockfileRowIdRange;
import io.datarouter.bytes.blockfile.index.BlockfileRowRange;
import io.datarouter.bytes.blockfile.row.BlockfileRow;

public class BlockfileValueTokens
extends BlockfileBaseTokens{

	private final long valueBlockId;
	private final long firstRowId;
	private final List<BlockfileRow> rows;
	private final byte[] checksum;
	private final byte[] value;

	public BlockfileValueTokens(
			long valueBlockId,
			long firstRowId,
			List<BlockfileRow> rows,
			int length,
			byte[] checksum,
			byte[] value){
		super(length, BlockfileBlockType.VALUE);
		this.valueBlockId = valueBlockId;
		this.firstRowId = firstRowId;
		this.rows = rows;
		this.checksum = checksum;
		this.value = value;
	}

	@Override
	public int suffixLength(){
		return checksum.length + value.length;
	}

	@Override
	public List<byte[]> toList(){
		return List.of(prefixBytes(), checksum, value);
	}

	public static int lengthWithoutValue(int checksumLength){
		return BlockfileBaseTokens.NUM_PREFIX_BYTES + checksumLength;
	}

	public long valueBlockId(){
		return valueBlockId;
	}

	public BlockfileRowIdRange toRowIdRange(){
		return new BlockfileRowIdRange(
				firstRowId,
				firstRowId + rows.size() - 1);
	}

	public BlockfileRowRange toRowKeyRange(){
		return new BlockfileRowRange(
				rows.getFirst().toRowVersion(),
				rows.getLast().toRowVersion());
	}

	public List<BlockfileRow> rows(){
		return rows;
	}

}
