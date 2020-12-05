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
package io.datarouter.client.memcached.codec;

import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.tuple.Pair;

public class MemcachedTallyCodec{

	private final String nodeName;
	private final int databeanVersion;

	public MemcachedTallyCodec(
			String nodeName,
			int databeanVersion){
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
	}

	public String encodeKey(TallyKey pk){
		return MemcachedKey.encode(nodeName, databeanVersion, pk);
	}

	public Pair<String,Long> decodeResult(Pair<String,Object> result){
		MemcachedKey<TallyKey> memcachedKey = MemcachedKey.decode(result.getLeft(), TallyKey.class);
		TallyKey tallyKey = memcachedKey.primaryKey;
		String stringKey = tallyKey.getId();

		/* From MemcachedClient::incr javadoc: Due to the way the memcached server operates on items, incremented and
		 * decremented items will be returned as Strings with any operations that return a value. */
		String stringValue = (String)result.getRight();
		Long longValue = Long.parseLong(stringValue);

		return new Pair<>(stringKey, longValue);
	}

}
