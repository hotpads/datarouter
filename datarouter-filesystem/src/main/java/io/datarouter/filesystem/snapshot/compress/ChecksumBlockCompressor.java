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
package io.datarouter.filesystem.snapshot.compress;

import java.util.Arrays;
import java.util.zip.CRC32;

import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;

public class ChecksumBlockCompressor implements BlockCompressor{

	public static final String NAME = "checksum";

	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public CompressedBlock compress(EncodedBlock encodedBlock, boolean concatChunks){
		var crc = new CRC32();
		byte[][] chunksAndChecksum = new byte[encodedBlock.chunks.length + 1][];
		for(int i = 0; i < encodedBlock.chunks.length; ++i){
			crc.update(encodedBlock.chunks[i]);
			chunksAndChecksum[i] = encodedBlock.chunks[i];
		}
		byte[] checksum = RAW_LONG_CODEC.encode(crc.getValue());
		chunksAndChecksum[encodedBlock.chunks.length] = checksum;
		return new CompressedBlock(chunksAndChecksum);
	}

	@Override
	public byte[] decompress(byte[] withChecksum, boolean validateChecksum){
		byte[] withoutChecksum = Arrays.copyOfRange(withChecksum, 0, withChecksum.length - 8);
		if(validateChecksum){
			var crc = new CRC32();
			crc.update(withoutChecksum);
			long expectedChecksum = RAW_LONG_CODEC.decode(withChecksum, withChecksum.length - 8);
			long actualChecksum = crc.getValue();
			if(expectedChecksum != actualChecksum){
				String message = String.format("invalid checksum, expected=%s, actual=%s",
						expectedChecksum,
						actualChecksum);
				throw new IllegalStateException(message);
			}
		}
		return withoutChecksum;
	}

}
