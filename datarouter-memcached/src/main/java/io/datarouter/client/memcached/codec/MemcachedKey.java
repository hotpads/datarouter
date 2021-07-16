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

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;

public class MemcachedKey<PK extends PrimaryKey<PK>>{

	public static final int CODEC_VERSION = 3;
	private static final int NUM_TOKENS = 4;

	public final int codecVersion;
	public final String nodeName;
	public final int databeanVersion;
	public final PK primaryKey;

	public MemcachedKey(
			int codecVersion,
			String nodeName,
			int databeanVersion,
			PK primaryKey){
		this.codecVersion = codecVersion;
		this.nodeName = nodeName;
		this.databeanVersion = databeanVersion;
		this.primaryKey = primaryKey;
	}

	public static String encode(String nodeName, int databeanVersion, PrimaryKey<?> pk){
		String encodedPk = PrimaryKeyPercentCodecTool.encode(pk);
		return CODEC_VERSION + ":" + nodeName + ":" + databeanVersion + ":" + encodedPk;
	}

	public static <PK extends PrimaryKey<PK>> MemcachedKey<PK> decode(String stringKey, Class<PK> pkClass){
		String[] tokens = stringKey.split(":");
		if(tokens.length != 4){
			String message = String.format("Incorrect number of key parts.  Expected=%s, found=%s, input=%s",
					NUM_TOKENS,
					tokens.length,
					stringKey);
			throw new RuntimeException(message);
		}
		int codecVersion = Integer.parseInt(tokens[0]);
		String nodeName = tokens[1];
		int databeanVersion = Integer.parseInt(tokens[2]);
		PK primaryKey = PrimaryKeyPercentCodecTool.decode(pkClass, tokens[3]);
		return new MemcachedKey<>(codecVersion, nodeName, databeanVersion, primaryKey);
	}

}
