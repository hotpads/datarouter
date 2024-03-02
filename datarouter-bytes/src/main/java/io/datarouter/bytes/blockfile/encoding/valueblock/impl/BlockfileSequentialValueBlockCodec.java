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
package io.datarouter.bytes.blockfile.encoding.valueblock.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.row.BlockfileRowCodec;
import io.datarouter.bytes.blockfile.row.BlockfileRowOp;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.Scanner;

/**
 * Rows are appended without any offset dictionary.
 * Slow for point queries because we must decode all entries rather than something like binary searching.
 */
public class BlockfileSequentialValueBlockCodec
implements BlockfileValueBlockCodec{

	private record Prefix(
			long valueBlockId,
			long firstRowIdInBlock,
			int numRows,
			int length){

		static Prefix decode(byte[] bytes){
			int cursor = 0;
			long valueBlockId = VarIntTool.decodeLong(bytes, cursor);
			cursor += VarIntTool.length(valueBlockId);
			long firstRowId = VarIntTool.decodeLong(bytes, cursor);
			cursor += VarIntTool.length(firstRowId);
			int numRows = VarIntTool.decodeInt(bytes, cursor);
			cursor += VarIntTool.length(numRows);
			return new Prefix(valueBlockId, firstRowId, numRows, cursor);
		}
	}

	@Override
	public BlockfileEncodedValueBlock encode(BlockfileValueBlockRows input){
		// allocate byte[]
		int numHeaderBytes = VarIntTool.length(input.valueBlockId())
				+ VarIntTool.length(input.firstRowIdInBlock())
				+ VarIntTool.length(input.rows().size());
		int numDataBytes = 0;
		for(BlockfileRow row : input.rows()){
			numDataBytes += row.length();
		}
		int numBytes = numHeaderBytes + numDataBytes;
		byte[] bytes = new byte[numBytes];

		// populate byte[]
		int cursor = 0;
		cursor += VarIntTool.encode(bytes, cursor, input.valueBlockId());
		cursor += VarIntTool.encode(bytes, cursor, input.firstRowIdInBlock());
		cursor += VarIntTool.encode(bytes, cursor, input.rows().size());
		for(BlockfileRow row : input.rows()){
			System.arraycopy(row.backingBytes(), row.offset(), bytes, cursor, row.length());
			cursor += row.length();
		}
		return new BlockfileEncodedValueBlock(bytes);
	}

	// The row is backed by a reference to the entire block array.
	@Override
	public BlockfileValueBlockRows decode(
			BlockfileEncodedValueBlock encodedBlock,
			BlockfileKeyRange keyRange){
		byte[] bytes = encodedBlock.bytes();
		Prefix prefix = Prefix.decode(bytes);
		int cursor = prefix.length();
		List<BlockfileRow> rows = new ArrayList<>(prefix.numRows());
		if(keyRange.isEverything()){
			for(int i = 0; i < prefix.numRows(); ++i){
				BlockfileRow row = BlockfileRowCodec.fromBytes(bytes, cursor);
				cursor += row.length();
				rows.add(row);
			}
		}else{
			for(int i = 0; i < prefix.numRows(); ++i){
				BlockfileRow row = BlockfileRowCodec.fromBytes(bytes, cursor);
				cursor += row.length();
				//TODO this could be smarter, only comparing first/last keys,
				// or not calling at all if it's a middle block.
				if(keyRange.contains(row)){
					rows.add(row);
				}
			}
		}
		return new BlockfileValueBlockRows(prefix.valueBlockId(), prefix.firstRowIdInBlock(), rows);
	}

	@Override
	public Scanner<BlockfileRow> scanAllVersions(
			BlockfileEncodedValueBlock encodedBlock,
			byte[] key){
		byte[] bytes = encodedBlock.bytes();
		Prefix prefix = Prefix.decode(bytes);
		var cursor = new AtomicInteger(prefix.length);
		// Skip rows before the searchKey.
		while(cursor.get() < bytes.length){
			BlockfileRow row = BlockfileRowCodec.fromBytes(bytes, cursor.get());
			if(row.compareToKey(key) < 0){
				cursor.addAndGet(row.length());
			}else{
				break;
			}
		}
		// Return matching rows as long as they are found.
		Supplier<BlockfileRow> nextRowSupplier = () -> {
			if(cursor.get() == bytes.length){
				return null;
			}
			BlockfileRow row = BlockfileRowCodec.fromBytes(bytes, cursor.get());
			cursor.addAndGet(row.length());
			return row.equalsKey(key) ? row : null;
		};
		return Scanner.generate(nextRowSupplier)
				.advanceWhile(row -> row != null);
	}

	@Override
	public Optional<BlockfileRow> findLatestVersion(
			BlockfileEncodedValueBlock encodedBlock,
			byte[] key){
		return scanAllVersions(encodedBlock, key)
				.findLast()
				.filter(row -> row.op() == BlockfileRowOp.PUT);
	}

}
