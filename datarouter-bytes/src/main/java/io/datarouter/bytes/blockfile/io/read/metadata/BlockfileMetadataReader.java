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
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileFooterBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileFooterTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileHeaderTokens;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;

public class BlockfileMetadataReader<T>{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileMetadataReader.class);

	// Must be larger than the footer's length bytes.
	private static final ByteLength INITIAL_ENDING_FETCH_SIZE = ByteLength.ofKiB(16);

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
		loadEndOfFile();
		return cachedHeaderBlockLength.get();
	}

	/*---------- header ---------*/

	public BlockfileHeaderBlock header(){
		return cachedHeader.get();
	}

	private BlockfileHeaderBlock loadHeader(){
		loadEndOfFile();
		return cachedHeader.get();
	}

	// For the case where we're scanning the file from the beginning without loading the footer.
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
		loadEndOfFile();
		return cachedRootIndex.get();
	}

	/*---------- footer ---------*/

	public BlockfileFooterBlock footer(){
		return cachedFooter.get();
	}

	private BlockfileFooterBlock loadFooter(){
		loadEndOfFile();
		return cachedFooter.get();
	}

	/*-------- footer block length ----------*/

	public int footerBlockLength(){
		return cachedFooterBlockLength.get();
	}

	private int loadFooterBlockLength(){
		loadEndOfFile();
		return cachedFooterBlockLength.get();
	}

	/*---------- file length ---------*/

	public long fileLength(){
		return cachedFileLength.get();
	}

	private long loadFileLength(){
		return config.knownFileLength()
				.orElseGet(() -> config.storage().length(name));
	}

	/*--------- end of file -----------*/

	private void loadEndOfFile(){
		int numBytesToFetch = INITIAL_ENDING_FETCH_SIZE.toBytesInt();
		while(true){
			byte[] endingBytes = config.storage().readEnding(name, numBytesToFetch);
			if(tryLoadEndOfFile(endingBytes)){
				return;
			}
			if(endingBytes.length < numBytesToFetch){
				String message = String.format("Failed to load footer after reading entire file %s", new KvString()
						.add("name", name)
						.add("bytesFetched", numBytesToFetch, Number::toString));
				throw new RuntimeException(message);
			}
			logger.warn("incomplete end of file", new KvString()
					.add("name", name)
					.add("bytesFetched", numBytesToFetch, Number::toString));
			numBytesToFetch *= 2;
		}
	}

	private boolean tryLoadEndOfFile(byte[] endingBytes){
		// Obtain footerBlockLength.
		int footerBlockLength = BlockfileFooterTokens.decodeFooterBlockLengthFromEndOfFileBytes(endingBytes);
		cachedFooterBlockLength.set(footerBlockLength);

		// Obtain footer.
		if(footerBlockLength > endingBytes.length){
			return false;
		}
		byte[] footerValueBytes = BlockfileFooterTokens.decodeFooterValueBytesFromEndOfFileBytes(endingBytes);
		BlockfileFooterBlock footer = BlockfileFooterBlock.VALUE_CODEC.decode(footerValueBytes);
		cachedFooter.set(footer);

		// Obtain header from footer.
		cachedHeaderBlockLength.set(footer.headerBlockLocation().length());
		cachedHeader.set(config.headerCodec().decode(footer.headerBytes().bytes()));

		// Obtain rootIndex.
		int rootIndexOffsetFromEnd = footerBlockLength + footer.rootIndexBlockLocation().length();
		if(rootIndexOffsetFromEnd > endingBytes.length){
			return false;
		}
		byte[] rootIndexBytes = Arrays.copyOfRange(
				endingBytes,
				endingBytes.length - rootIndexOffsetFromEnd,
				endingBytes.length - footerBlockLength);
		BlockfileIndexBlock rootIndex = header().indexBlockFormat().supplier().get().decode(rootIndexBytes);
		cachedRootIndex.set(rootIndex);
		return true;
	}

}
