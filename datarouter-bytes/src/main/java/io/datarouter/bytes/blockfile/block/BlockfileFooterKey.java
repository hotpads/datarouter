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
package io.datarouter.bytes.blockfile.block;

import io.datarouter.bytes.codec.stringcodec.StringCodec;

public enum BlockfileFooterKey{

	HEADER("HEADER"),
	HEADER_BLOCK_LOCATION("HEADER_BLOCK_LOCATION"),
	ROOT_INDEX_BLOCK_LOCATION("ROOT_INDEX_BLOCK_LOCATION"),
	NUM_VALUE_BLOCKS("NUM_VALUE_BLOCKS"),
	NUM_INDEX_BLOCKS("NUM_INDEX_BLOCKS"),
	USER_DICTIONARY("USER_DICTIONARY");

	public final byte[] bytes;

	private BlockfileFooterKey(String keyString){
		this.bytes = StringCodec.UTF_8.encode(keyString);
	}

}
