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
package io.datarouter.client.memcached.client;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.util.EncodedPrimaryKeyPercentCodec;
import io.datarouter.storage.util.PrimaryKeyPercentCodecTool;

public class MemcachedEncodedKey extends EncodedPrimaryKeyPercentCodec{

	public static Integer ENCODING_VERSION = 3;

	public MemcachedEncodedKey(String nodeName, Integer databeanVersion, PrimaryKey<?> primaryKey){
		super(nodeName, databeanVersion, primaryKey);
	}

	@Override
	protected Integer getEncodingVersion(){
		return ENCODING_VERSION;
	}

	public static <PK extends PrimaryKey<PK>> MemcachedEncodedKey parse(String string, Class<PK> pkClass){
		StringBuilder current = new StringBuilder();
		String[] parts = new String[3];
		int counter = 0;
		for(int i = 0; i < string.length(); i++){
			char character = string.charAt(i);
			if(character == ':'){
				parts[counter++] = current.toString();
				current = new StringBuilder();
			}else{
				current.append(character);
			}
		}
		if(counter != 3){
			throw new RuntimeException("incorrect number of parts, counter=" + counter + " input=" + string);
		}
		int databeanVersion = Integer.parseInt(parts[2]);
		PK primaryKey = PrimaryKeyPercentCodecTool.decode(pkClass, current.toString());
		return new MemcachedEncodedKey(parts[1], databeanVersion, primaryKey);
	}

}
