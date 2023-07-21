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
package io.datarouter.bytes.kvfile.io.write;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.blockfile.write.BlockfileWriter.BlockfileWriteResult;
import io.datarouter.bytes.blockfile.write.BlockfileWriterBuilder;
import io.datarouter.bytes.kvfile.io.footer.KvFileFooter;
import io.datarouter.bytes.kvfile.io.header.KvFileHeader;
import io.datarouter.scanner.Scanner;

public class KvFileWriter<T>{

	public record KvFileWriterConfig<T>(
			BlockfileWriterBuilder<List<T>> blockfileWriterBuilder,
			BinaryDictionary headerUserDictionary,
			Supplier<BinaryDictionary> footerUserDictionarySupplier){
	}

	private final KvFileWriterConfig<T> params;
	private final AtomicLong kvCounter;

	public KvFileWriter(KvFileWriterConfig<T> params){
		this.params = params;
		kvCounter = new AtomicLong();
	}

	public record KvFileWriteResult(
			BlockfileWriteResult blockfileWriteResult,
			long kvCount){
	}

	public KvFileWriteResult write(Scanner<List<T>> blockBatches){
		Scanner<List<T>> instrumentedBlockBatches = blockBatches
				.each(batch -> kvCounter.addAndGet(batch.size()));
		var blockfileWriter = params.blockfileWriterBuilder()
				.setHeaderDictionary(makeHeaderDictionary())
				.setFooterDictionarySupplier(this::makeFooterDictionary)
				.build();
		BlockfileWriteResult blockfileWriteResult = blockfileWriter.write(instrumentedBlockBatches);
		return new KvFileWriteResult(
				blockfileWriteResult,
				kvCounter.get());
	}

	private BinaryDictionary makeHeaderDictionary(){
		var header = new KvFileHeader(
				params.headerUserDictionary(),
				KvFileHeader.BLOCK_FORMAT_PLACEHOLDER);
		return header.toBinaryDictionary();
	}

	private BinaryDictionary makeFooterDictionary(){
		var footer = new KvFileFooter(
				params.footerUserDictionarySupplier().get(),
				kvCounter.get());
		return KvFileFooter.DICTIONARY_CODEC.encode(footer);
	}

}