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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Pair;

public class MemcachedTallyCodecV2{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedTallyCodecV2.class);

	private final Subpath nodeSubpath;
	private final int nodeSubpathLength;

	public MemcachedTallyCodecV2(Subpath nodeSubpath){
		this.nodeSubpath = nodeSubpath;
		nodeSubpathLength = nodeSubpath.toString().length();
	}

	public Optional<String> encodeKey(String id){
		String encodedKey = MemcachedKeyV2.encodeTallyId(nodeSubpath, id);
		if(encodedKey.length() > CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH){
			logger.warn("tally key too long for memcached"
					+ " tallyKey={} tallyKeyLength={}"
					+ " nodeSubpath={} nodeSubpathLength={}"
					+ " encodedKey={} encodedKeyLength={}"
					+ " maxEncodedKeyLength={}",
					id,
					id.length(),
					nodeSubpath,
					nodeSubpathLength,
					encodedKey,
					encodedKey.length(),
					CommonFieldSizes.MEMCACHED_MAX_KEY_LENGTH);
			return Optional.empty();
		}
		return Optional.of(encodedKey);
	}

	/**
	 * Spy client returns incremented values as strings
	 */
	public Pair<String,Long> decodeResult(Pair<String,Object> result){
		String stringPk = MemcachedKeyV2.decodeTallyId(nodeSubpathLength, result.getLeft());
		//TODO push the casting up to MemcachedOps
		String stringValue = (String)result.getRight();
		Long longValue = Long.parseLong(stringValue);
		return new Pair<>(stringPk, longValue);
	}

}
