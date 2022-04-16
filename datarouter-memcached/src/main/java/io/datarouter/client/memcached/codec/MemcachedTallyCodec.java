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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.codec.stringcodec.TerminatedStringCodec;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.tuple.Pair;

public class MemcachedTallyCodec{
	private static final Logger logger = LoggerFactory.getLogger(MemcachedTallyCodec.class);

	public static final String CODEC_VERSION = "1";

	private final String clientTypeName;
	private final Subpath nodeSubpath;
	private final int clientMaxKeyLength;
	private final int nodeSubpathLength;

	public MemcachedTallyCodec(
			String clientTypeName,
			Subpath nodePath,
			int clientMaxKeyLength){
		this.clientTypeName = clientTypeName;
		this.nodeSubpath = nodePath;
		this.clientMaxKeyLength = clientMaxKeyLength;
		nodeSubpathLength = nodePath.toString().length();
	}

	public Optional<String> encodeKeyIfValid(String id){
		String encodedKey = encodeTallyId(nodeSubpath, id);
		if(encodedKey.length() > clientMaxKeyLength){
			logger.warn("tally id too long for {}"
					+ " tallyId={} tallyIdLength={}"
					+ " nodePath={} nodePathLength={}"
					+ " encodedKey={} encodedKeyLength={}"
					+ " maxEncodedKeyLength={}",
					clientTypeName,
					id,
					id.length(),
					nodeSubpath,
					nodeSubpathLength,
					encodedKey,
					encodedKey.length(),
					clientMaxKeyLength);
			return Optional.empty();
		}
		return Optional.of(encodedKey);
	}

	public Pair<String,Long> decodeResult(Pair<String,String> result){
		String stringPk = decodeTallyId(nodeSubpathLength, result.getLeft());
		String stringValue = result.getRight();
		long longValue = Long.parseLong(stringValue);
		return new Pair<>(stringPk, longValue);
	}

	private static String encodeTallyId(Subpath subpath, String tallyId){
		byte[] tallyIdBytes = TerminatedStringCodec.UTF_8.encode(tallyId);
		String encodedTallyId = Base64.getUrlEncoder().encodeToString(tallyIdBytes);
		return subpath + encodedTallyId;
	}

	private static String decodeTallyId(int subpathLength, String fullMemcachedKey){
		String encodedTallyId = fullMemcachedKey.substring(subpathLength);
		byte[] tallyIdBytes = Base64.getUrlDecoder().decode(encodedTallyId);
		return TerminatedStringCodec.UTF_8.decode(tallyIdBytes).value;
	}

}
