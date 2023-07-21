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
package io.datarouter.bytes.blockfile.read;

import java.util.Optional;

import io.datarouter.bytes.blockfile.section.BlockfileFooter;
import io.datarouter.bytes.blockfile.section.BlockfileHeader;
import io.datarouter.bytes.blockfile.section.BlockfileHeader.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.section.BlockfileTrailer;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.write.BlockfileWriter;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;

public class BlockfileMetadataReader<T>{

	public record BlockfileMetadataReaderConfig<T>(
			BlockfileStorage storage,
			BlockfileHeaderCodec headerCodec,
			Optional<Long> knownFileLength){
	}

	private final BlockfileMetadataReaderConfig<T> config;
	private final String name;
	// cached values
	private final BlockfileMetadataCache<Long> cachedLength
			= new BlockfileMetadataCache<>(this::loadLength);
	private final BlockfileMetadataCache<DecodedHeader> cachedDecodedHeader
			= new BlockfileMetadataCache<>(this::loadDecodedHeader);
	private final BlockfileMetadataCache<BlockfileFooter> cachedFooter
			= new BlockfileMetadataCache<>(this::loadFooter);
	private final BlockfileMetadataCache<BlockfileTrailer> cachedTrailer
			= new BlockfileMetadataCache<>(this::loadTrailer);

	/*---------- construct ---------*/

	public BlockfileMetadataReader(BlockfileMetadataReaderConfig<T> config, String name){
		this.config = config;
		this.name = name;
	}

	/*---------- metadata ---------*/

	public String name(){
		return name;
	}

	private long loadLength(){
		return config.knownFileLength()
				.orElseGet(() -> config.storage().length(name));
	}

	public int numBlockMetadataBytes(){
		return BlockfileWriter.NUM_VALUE_LENGTH_BYTES
				+ header().checksummer().numBytes()
				+ BlockfileWriter.NUM_SECTION_BYTES;
	}

	/*---------- header ---------*/

	public record DecodedHeader(
			BlockfileHeader header,
			int blockLength){
	}

	public int headerBlockLength(){
		return cachedDecodedHeader.get().blockLength();
	}

	public BlockfileHeader header(){
		return cachedDecodedHeader.get().header();
	}

	private DecodedHeader loadDecodedHeader(){
		byte[] blockLengthBytes = config.storage().readPartial(
				name,
				0,
				BlockfileWriter.NUM_VALUE_LENGTH_BYTES);
		int blockLength = RawIntCodec.INSTANCE.decode(blockLengthBytes);
		int valueLength = blockLength - BlockfileWriter.NUM_HEADER_METADATA_BYTES;
		byte[] valueBytes = config.storage().readPartial(
				name,
				BlockfileWriter.NUM_HEADER_METADATA_BYTES,
				valueLength);
		return new DecodedHeader(
				config.headerCodec().decode(valueBytes),
				blockLength);
	}

	public void setHeader(DecodedHeader decodedHeader){
		cachedDecodedHeader.set(decodedHeader);
	}

	/*---------- footer ---------*/

	public BlockfileFooter footer(){
		return cachedFooter.get();
	}

	private BlockfileFooter loadFooter(){
		byte[] valueBytes = config.storage().readPartial(
				name,
				footerValueOffset(),
				footerValueLength());
		return BlockfileFooter.VALUE_CODEC.decode(valueBytes);
	}

	/*---------- trailer ---------*/

	public long footerBlockOffset(){
		return cachedLength.get()
				- BlockfileWriter.NUM_TRAILER_BYTES
				- cachedTrailer.get().footerBlockLength();
	}

	public long footerValueOffset(){
		return footerBlockOffset() + BlockfileWriter.NUM_FOOTER_METADATA_BYTES;
	}

	public int footerValueLength(){
		return cachedTrailer.get().footerBlockLength() - BlockfileWriter.NUM_FOOTER_METADATA_BYTES;
	}

	public BlockfileTrailer trailer(){
		return cachedTrailer.get();
	}

	private BlockfileTrailer loadTrailer(){
		byte[] trailerBytes = config.storage().readPartial(
				name,
				cachedLength.get() - BlockfileWriter.NUM_TRAILER_BYTES,
				BlockfileWriter.NUM_TRAILER_BYTES);
		return BlockfileTrailer.decode(trailerBytes);
	}

}
