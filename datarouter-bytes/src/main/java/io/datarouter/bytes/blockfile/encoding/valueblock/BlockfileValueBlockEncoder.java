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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.datarouter.bytes.blockfile.block.tokens.BlockfileValueTokens;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileValueBlockRows;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Scanner;

public class BlockfileValueBlockEncoder{

	public record BlockfileValueBlockEncoderConfig(
			BlockfileValueBlockFormat valueBlockFormat,
			BlockfileCompressor compressor,
			BlockfileChecksummer checksummer){
	}

	private final BlockfileValueBlockEncoderConfig config;
	private final BlockfileValueBlockCodec valueBlockCodec;

	public BlockfileValueBlockEncoder(BlockfileValueBlockEncoderConfig config){
		this.config = config;
		valueBlockCodec = config.valueBlockFormat().supplier().get();
	}

	public List<BlockfileValueTokens> encodeValueBlocks(List<BlockfileValueBlockRows> blocks){
		Function<byte[],byte[]> compressFunction = config.compressor().newEncoder();
		Function<byte[],byte[]> checksumFunction = config.checksummer().newEncoder();
		return Scanner.of(blocks)
				.map(block -> encodeValueBlock(compressFunction, checksumFunction, block))
				.collect(() -> new ArrayList<>(blocks.size()));
	}

	private BlockfileValueTokens encodeValueBlock(
			Function<byte[],byte[]> compressFunction,
			Function<byte[],byte[]> checksumFunction,
			BlockfileValueBlockRows blockRows){
		List<BlockfileRow> rows = blockRows.rows();
		byte[] encodedBytes = valueBlockCodec.encode(blockRows).bytes();
		byte[] compressedBytes = compressFunction.apply(encodedBytes);
		int nonValueLength = BlockfileValueTokens.lengthWithoutValue(config.checksummer().numBytes());
		int blockLength = nonValueLength + compressedBytes.length;
		byte[] checksumBytes = checksumFunction.apply(compressedBytes);
		return new BlockfileValueTokens(
				blockRows.valueBlockId(),
				blockRows.firstRowIdInBlock(),
				rows,
				blockLength,
				checksumBytes,
				compressedBytes);
	}

}
