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
package io.datarouter.bytes.kvfile.blockformat;

import java.util.function.Function;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.kvfile.block.KvFileBlockCodec;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;

public record KvFileBlockFormat(
		String encodedName,
		Function<Codec<?,KvFileEntry>,KvFileBlockCodec<?>> constructor){

	/**
	 * Identity blockCodec for dealing directly with KvFileEntries
	 */
	@SuppressWarnings("unchecked")
	public <T> KvFileBlockCodec<KvFileEntry> newBlockCodec(){
		return (KvFileBlockCodec<KvFileEntry>)constructor.apply(Codec.identity());
	}

	@SuppressWarnings("unchecked")
	public <T> KvFileBlockCodec<T> newBlockCodec(Codec<T,KvFileEntry> kvCodec){
		return (KvFileBlockCodec<T>)constructor.apply(kvCodec);
	}

}
