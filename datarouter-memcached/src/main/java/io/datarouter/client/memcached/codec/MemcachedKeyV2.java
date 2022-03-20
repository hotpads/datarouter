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
package io.datarouter.client.memcached.codec;

import java.util.Base64;

import io.datarouter.bytes.codec.stringcodec.TerminatedStringCodec;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

public class MemcachedKeyV2{

	/**
	 * Unifies blob/databean/tally storage key format even though tally doesn't use blob storage.
	 * Ideally it wouldn't be necessary, but databean/tally are tied together.
	 */
	public static String encodeTallyId(Subpath subpath, String tallyId){
		byte[] tallyIdBytes = TerminatedStringCodec.UTF_8.encode(tallyId);
		String encodedTallyId = encodeBytes(tallyIdBytes);
		return subpath + encodedTallyId;
	}

	public static String decodeTallyId(int subpathLength, String fullMemcachedKey){
		String encodedTallyId = fullMemcachedKey.substring(subpathLength);
		byte[] tallyIdBytes = decodeString(encodedTallyId);
		return TerminatedStringCodec.UTF_8.decode(tallyIdBytes).value;
	}
	/**
	 * Base64URL encoding was designed with filesystem paths in mind.
	 *
	 * It could also be decoded it to reproduce the full key, avoiding the need to store PK fields in the payload.
	 *
	 * Unfortunately, it doesn't sort identically to the bytes, but that isn't a concern for MapStorage.
	 *
	 * A Base16 (hex) format could be used if sorting becomes necessary, but would use more key space which is
	 * limited with memcached.
	 */
	public static PathbeanKey encodeDatabeanKey(PrimaryKey<?> pk){
		byte[] bytes = FieldTool.getConcatenatedValueBytes(pk.getFields());
		String string = encodeBytes(bytes);
		return PathbeanKey.of(string);
	}

	private static String encodeBytes(byte[] bytes){
		return Base64.getUrlEncoder().encodeToString(bytes);
	}

	private static byte[] decodeString(String string){
		return Base64.getUrlDecoder().decode(string);
	}

}
