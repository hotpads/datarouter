/**
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.datarouter.filesystem.snapshot.encode.EncodedBlock;

public class GzipBlockCompressor implements BlockCompressor{

	public static final String NAME = "gzip";

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public CompressedBlock compress(EncodedBlock encodedBlock, boolean concatChunks){
		var outputStream = new ByteArrayOutputStream();
		try(var gzipOutputStream = new GZIPOutputStream(outputStream)){
			for(int i = 0; i < encodedBlock.chunks.length; ++i){
				gzipOutputStream.write(encodedBlock.chunks[i], 0, encodedBlock.chunks[i].length);
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return new CompressedBlock(new byte[][]{outputStream.toByteArray()});
	}

	@Override
	public byte[] decompress(byte[] bytes, boolean validateChecksum){
		var inputStream = new ByteArrayInputStream(bytes);
		try(var gzipInputStream = new GZIPInputStream(inputStream)){
			return gzipInputStream.readAllBytes();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
