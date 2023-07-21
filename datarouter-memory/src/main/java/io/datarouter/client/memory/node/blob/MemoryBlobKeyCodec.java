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
package io.datarouter.client.memory.node.blob;

import java.util.Arrays;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Range;

public class MemoryBlobKeyCodec
implements Codec<PathbeanKey,byte[]>{

	@Override
	public byte[] encode(PathbeanKey pathbeanKey){
		return StringCodec.UTF_8.encode(pathbeanKey.getPathAndFile());
	}

	@Override
	public PathbeanKey decode(byte[] bytesKey){
		String stringKey = StringCodec.UTF_8.decode(bytesKey);
		return PathbeanKey.of(stringKey);
	}

	public PathbeanKey decode(MemoryBlob blob){
		return decode(blob.getKey());
	}

	public Range<byte[]> encodeSubpathToRange(Subpath subpath){
		byte[] subpathBytes = StringCodec.UTF_8.encode(subpath.toString());
		if(subpathBytes.length == 0){
			return Range.everything();
		}
		byte[] startBytes = subpathBytes;
		byte[] endBytes = ByteTool.unsignedIncrement(startBytes);
		return new Range<>(Arrays::compareUnsigned, startBytes, true, endBytes, false);
	}

}
