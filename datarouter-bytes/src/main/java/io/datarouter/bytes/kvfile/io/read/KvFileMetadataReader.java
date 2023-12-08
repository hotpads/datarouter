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
package io.datarouter.bytes.kvfile.io.read;

import java.util.List;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.blockfile.read.BlockfileMetadataCache;
import io.datarouter.bytes.blockfile.read.BlockfileMetadataReader;
import io.datarouter.bytes.kvfile.blockformat.KvFileBlockFormats;
import io.datarouter.bytes.kvfile.io.footer.KvFileFooter;
import io.datarouter.bytes.kvfile.io.header.KvFileHeader;
import io.datarouter.bytes.kvfile.io.header.KvFileHeader.KvFileHeaderCodec;

public class KvFileMetadataReader<T>{

	public record KvFileMetadataReaderConfig<T>(
			BlockfileMetadataReader<List<T>> blockfileMetadataReader,
			KvFileBlockFormats blockFormats){
	}

	private final KvFileMetadataReaderConfig<T> config;
	private final BlockfileMetadataCache<KvFileHeader> cachedDecodedHeader
			= new BlockfileMetadataCache<>(this::loadDecodedHeader);
	private final BlockfileMetadataCache<KvFileFooter> cachedDecodedFooter
			= new BlockfileMetadataCache<>(this::loadDecodedFooter);

	public KvFileMetadataReader(KvFileMetadataReaderConfig<T> config){
		this.config = config;
	}

	public BlockfileMetadataReader<List<T>> blockfileMetadataReader(){
		return config.blockfileMetadataReader();
	}

	/*------ header ------*/

	private KvFileHeader loadDecodedHeader(){
		BinaryDictionary kvHeaderDictionary = blockfileMetadataReader().header().userDictionary();
		return new KvFileHeaderCodec(config.blockFormats()).decode(kvHeaderDictionary);
	}

	public KvFileHeader header(){
		return cachedDecodedHeader.get();
	}

	/*------ footer ------*/

	private KvFileFooter loadDecodedFooter(){
		BinaryDictionary kvFooterDictionary = blockfileMetadataReader().footer().userDictionary();
		return KvFileFooter.DICTIONARY_CODEC.decode(kvFooterDictionary);
	}

	public KvFileFooter footer(){
		return cachedDecodedFooter.get();
	}

}