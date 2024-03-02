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
package io.datarouter.bytes.blockfile.encoding.indexblock.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileIndexTokens;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.index.BlockfileByteRange;
import io.datarouter.bytes.blockfile.index.BlockfileIndexBlockInput;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.index.BlockfileRowIdRange;
import io.datarouter.bytes.blockfile.index.BlockfileRowRange;
import io.datarouter.bytes.blockfile.index.BlockfileValueBlockIdRange;
import io.datarouter.bytes.blockfile.row.BlockfileRowVersionCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.Scanner;

/**
 * Sections:
 * - block length
 * - block type
 * - number of index entries
 * - children info table: blockId, fromItemId, toItemId, fromBytes, length, indexEntryOffset
 * - concatenation of index entries
 */
public class BlockfileV1IndexBlockCodec implements BlockfileIndexBlockCodec{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileV1IndexBlockCodec.class);

	/*--------- encode -----------*/

	@Override
	public int estEncodedBytes(BlockfileIndexEntry indexEntry){
		return 4 // Offsets table ending
				+ BlockfileIndexOffsetsTableRow.ROW_LENGTH // Offsets table row
				+ indexEntry.rowRange().sumOfLengths(); // rowVersions lengths
	}

	@Override
	public BlockfileIndexTokens encode(BlockfileIndexBlockInput input){
		List<BlockfileIndexEntry> children = input.children();

		List<byte[]> rowKeyTokens = Scanner.of(children)
				.map(BlockfileIndexEntry::rowRange)
				.map(BlockfileV1IndexBlockCodec::encodeRowRange)
				.collect(() -> new ArrayList<>(children.size()));
		List<byte[]> tokens = new ArrayList<>();

		// section 1: header
		tokens.add(VarIntTool.encode(input.globalBlockId()));
		tokens.add(VarIntTool.encode(input.indexBlockId()));
		tokens.add(VarIntTool.encode(input.level()));
		tokens.add(VarIntTool.encode(children.size()));

		// section 2: offsets table
		int indexOffset = 0;
		for(int i = 0; i < children.size(); ++i){
			BlockfileIndexEntry child = children.get(i);
			var offsetsTableRow = new BlockfileIndexOffsetsTableRow(
					child.childGlobalBlockId(),
					child.childIndexOrValueBlockId(),
					child.valueBlockIdRange().first(),
					child.valueBlockIdRange().last(),
					child.rowIdRange().first(),
					child.rowIdRange().last(),
					child.byteRange().from(),
					child.byteRange().lengthInt(),
					indexOffset);
			tokens.add(offsetsTableRow.encode());
			indexOffset += rowKeyTokens.get(i).length;
		}

		// section 3: row keys
		tokens.addAll(rowKeyTokens);

		byte[] valueBytes = ByteTool.concat(tokens);
		// Need to use RawIntCodec for length to match other block types
		// TODO centralize encoding of the initial standard fields
		int length = BlockfileBaseTokens.NUM_LENGTH_BYTES + BlockfileBlockType.NUM_BYTES + valueBytes.length;
		return new BlockfileIndexTokens(input.indexBlockId(), length, valueBytes);
	}

	private static byte[] encodeRowRange(BlockfileRowRange rowRange){
		byte[] firstBytes = rowRange.first().copyOfBytes();
		byte[] lastBytes = rowRange.last().copyOfBytes();
		return ByteTool.concat(
				VarIntTool.encode(firstBytes.length),
				firstBytes,
				VarIntTool.encode(lastBytes.length),
				lastBytes);
	}

	/*--------- decode -----------*/

	@Override
	public BlockfileIndexBlock decode(byte[] bytes){
		int cursor = BlockfileBaseTokens.NUM_LENGTH_BYTES + BlockfileBlockType.NUM_BYTES;
		long globalBlockId = VarIntTool.decodeLong(bytes, cursor);
		cursor += VarIntTool.length(globalBlockId);
		long indexBlockId = VarIntTool.decodeLong(bytes, cursor);
		cursor += VarIntTool.length(indexBlockId);
		int level = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(level);
		int numChildren = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(numChildren);
		int offsetsTableLength = numChildren * BlockfileIndexOffsetsTableRow.ROW_LENGTH;
		byte[] offsetsTable = Arrays.copyOfRange(bytes, cursor, cursor + offsetsTableLength);
		cursor += offsetsTableLength;
		byte[] indexRecords = Arrays.copyOfRange(bytes, cursor, bytes.length);
		return new BlockfileIndexBlock(globalBlockId, indexBlockId, level, numChildren, offsetsTable, indexRecords);
	}

	@Override
	public BlockfileIndexEntry decodeChild(BlockfileIndexBlock indexBlock, int childIndex){
		BlockfileIndexOffsetsTableRow offsets = decodeOffsets(indexBlock.offsetsTable(), childIndex);
		byte[] keysBytes = indexBlock.indexKeys();

		int cursor = offsets.indexOffset();
		int firstLength = VarIntTool.decodeInt(keysBytes, cursor);
		cursor += VarIntTool.length(firstLength);
		byte[] first = Arrays.copyOfRange(keysBytes, cursor, cursor + firstLength);
		cursor += first.length;
		int lastLength = VarIntTool.decodeInt(keysBytes, cursor);
		cursor += VarIntTool.length(lastLength);
		byte[] last = Arrays.copyOfRange(keysBytes, cursor, cursor + lastLength);
		cursor += last.length;

		return new BlockfileIndexEntry(
				indexBlock.level(),
				offsets.globalBlockId(),
				offsets.indexOrValueBlockId(),
				new BlockfileValueBlockIdRange(
						offsets.firstValueBlockId(),
						offsets.lastValueBlockId()),
				new BlockfileRowIdRange(
						offsets.firstRowId(),
						offsets.lastRowId()),
				new BlockfileRowRange(
						BlockfileRowVersionCodec.fromBytes(first),
						BlockfileRowVersionCodec.fromBytes(last)),
				new BlockfileByteRange(
						offsets.bytesFrom(),
						offsets.bytesTo()));
	}

	private static BlockfileIndexOffsetsTableRow decodeOffsets(byte[] offsetsTable, int index){
		int offset = index * BlockfileIndexOffsetsTableRow.ROW_LENGTH;
		return BlockfileIndexOffsetsTableRow.decode(offsetsTable, offset);
	}

	/*--------- nested -----------*/

	private record BlockfileIndexOffsetsTableRow(
			long globalBlockId,
			long indexOrValueBlockId,
			long firstValueBlockId,
			long lastValueBlockId,
			long firstRowId,
			long lastRowId,
			long bytesFrom,
			int bytesLength,
			int indexOffset){

		private static final int ROW_LENGTH = 7 * RawLongCodec.INSTANCE.length()
				+ 2 * RawIntCodec.INSTANCE.length();

		public long bytesTo(){
			return bytesFrom + bytesLength;
		}

		public byte[] encode(){
			return ByteTool.concat(
					RawLongCodec.INSTANCE.encode(globalBlockId),
					RawLongCodec.INSTANCE.encode(indexOrValueBlockId),
					RawLongCodec.INSTANCE.encode(firstValueBlockId),
					RawLongCodec.INSTANCE.encode(lastValueBlockId),
					RawLongCodec.INSTANCE.encode(firstRowId),
					RawLongCodec.INSTANCE.encode(lastRowId),
					RawLongCodec.INSTANCE.encode(bytesFrom),
					RawIntCodec.INSTANCE.encode(bytesLength),
					RawIntCodec.INSTANCE.encode(indexOffset));
		}

		public static BlockfileIndexOffsetsTableRow decode(byte[] bytes, int offset){
			int cursor = offset;

			long globalBlockId = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();
			long indexOrValueBlockId = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();

			long firstValueBlockId = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();
			long lastValueBlockId = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();

			long firstRowId = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();
			long lastRowId = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();

			long bytesFrom = RawLongCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawLongCodec.INSTANCE.length();
			int bytesLength = RawIntCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawIntCodec.INSTANCE.length();

			int indexOffset = RawIntCodec.INSTANCE.decode(bytes, cursor);
			cursor += RawIntCodec.INSTANCE.length();

			return new BlockfileIndexOffsetsTableRow(
					globalBlockId,
					indexOrValueBlockId,
					firstValueBlockId,
					lastValueBlockId,
					firstRowId,
					lastRowId,
					bytesFrom,
					bytesLength,
					indexOffset);
		}
	}

}
