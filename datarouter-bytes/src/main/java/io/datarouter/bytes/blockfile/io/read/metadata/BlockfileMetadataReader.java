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
package io.datarouter.bytes.blockfile.io.read.metadata;

import java.io.InputStream;
import java.util.Optional;

import io.datarouter.bytes.blockfile.block.decoded.BlockfileFooterBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileFooterTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileHeaderTokens;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;

public class BlockfileMetadataReader<T>{

	public record BlockfileMetadataReaderConfig<T>(
			BlockfileStorage storage,
			BlockfileHeaderCodec headerCodec,
			Optional<Long> knownFileLength){
	}

	private final BlockfileMetadataReaderConfig<T> config;
	private final String name;
	// cached values
	private final BlockfileMetadataCache<Integer> cachedHeaderBlockLength
			= new BlockfileMetadataCache<>(this::loadHeaderBlockLength);
	private final BlockfileMetadataCache<BlockfileHeaderBlock> cachedHeader
			= new BlockfileMetadataCache<>(this::loadHeader);
	private final BlockfileMetadataCache<BlockfileIndexBlock> cachedRootIndex
			= new BlockfileMetadataCache<>(this::loadRootIndex);
	private final BlockfileMetadataCache<BlockfileFooterBlock> cachedFooter
			= new BlockfileMetadataCache<>(this::loadFooter);
	private final BlockfileMetadataCache<Integer> cachedFooterBlockLength
			= new BlockfileMetadataCache<>(this::loadFooterBlockLength);
	private final BlockfileMetadataCache<Long> cachedFileLength
			= new BlockfileMetadataCache<>(this::loadFileLength);

	/*---------- construct ---------*/

	public BlockfileMetadataReader(BlockfileMetadataReaderConfig<T> config, String name){
		this.config = config;
		this.name = name;
	}

	/*---------- name ---------*/

	public String name(){
		return name;
	}

	/*--------- header block length ----------*/

	public int headerBlockLength(){
		return cachedHeaderBlockLength.get();
	}

	private int loadHeaderBlockLength(){
		var headerBlockLengthLocation = BlockfileHeaderTokens.lengthLocation();
		byte[] headerBlockLengthBytes = config.storage().readPartial(name, headerBlockLengthLocation);
		return BlockfileBaseTokens.decodeLength(headerBlockLengthBytes);
	}

	/*---------- header ---------*/

	public BlockfileHeaderBlock header(){
		return cachedHeader.get();
	}

	//TODO Overfetch the first N bytes of the file to get the headerLengh and header in one fetch
	private BlockfileHeaderBlock loadHeader(){
		BlockfileLocation valueLocation = BlockfileHeaderTokens.valueLocation(headerBlockLength());
		byte[] valueBytes = config.storage().readPartial(name, valueLocation);
		return config.headerCodec().decode(valueBytes);
	}

	// For the case where we're scanning the file from the beggining without loading the trailer.
	public void readAndCacheHeader(InputStream inputStream){
		int blockLength = BlockfileHeaderTokens.readBlockLength(inputStream);
		cachedHeaderBlockLength.set(blockLength);
		BlockfileHeaderTokens.readBlockType(inputStream);// skip blockType
		byte[] valueBytes = BlockfileHeaderTokens.readValueBytes(inputStream, blockLength);
		var header = config.headerCodec().decode(valueBytes);
		cachedHeader.set(header);
	}

	/*---------- root index ------*/

	public BlockfileIndexBlock rootIndex(){
		return cachedRootIndex.get();
	}

	private BlockfileIndexBlock loadRootIndex(){
		byte[] valueBytes = config.storage().readPartial(name, footer().rootIndexBlockLocation());
		return header().indexBlockFormat().supplier().get().decode(valueBytes);
	}

	/*---------- footer ---------*/

	public BlockfileFooterBlock footer(){
		return cachedFooter.get();
	}

	//TODO Overfetch the last N bytes of the file to get the rootIndex and footer in one fetch
	private BlockfileFooterBlock loadFooter(){
		BlockfileLocation blockLocation = BlockfileFooterTokens.blockLocation(fileLength(), footerBlockLength());
		BlockfileLocation valueLocation = BlockfileFooterTokens.valueLocation(blockLocation);
		byte[] valueBytes = config.storage().readPartial(name, valueLocation);
		BlockfileFooterBlock block = BlockfileFooterBlock.VALUE_CODEC.decode(valueBytes);
		cachedHeaderBlockLength.set(block.headerBlockLocation().length());
		return block;
	}

	/*-------- footer block length ----------*/

	public int footerBlockLength(){
		return cachedFooterBlockLength.get();
	}

	private int loadFooterBlockLength(){
		var lengthLocation = BlockfileFooterTokens.lengthLocation(fileLength());
		byte[] lengthBytes = config.storage().readPartial(name, lengthLocation);
		return BlockfileBaseTokens.decodeLength(lengthBytes);
	}

	/*---------- file length ---------*/

	public long fileLength(){
		return cachedFileLength.get();
	}

	private long loadFileLength(){
		return config.knownFileLength()
				.orElseGet(() -> config.storage().length(name));
	}

}
