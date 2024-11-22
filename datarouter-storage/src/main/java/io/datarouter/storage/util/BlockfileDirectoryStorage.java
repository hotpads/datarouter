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
package io.datarouter.storage.util;

import java.io.InputStream;
import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.io.MultiByteArrayInputStream;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.util.tuple.Range;

public class BlockfileDirectoryStorage implements BlockfileStorage{

	private final Directory directory;

	public BlockfileDirectoryStorage(Directory directory){
		this.directory = directory;
	}

	@Override
	public List<BlockfileNameAndSize> list(){
		return directory.scan(Subpath.empty())
				.map(pathbean -> new BlockfileNameAndSize(pathbean.getKey().getFile(), pathbean.getSize()))
				.list();
	}

	@Override
	public long length(String name){
		return directory.length(PathbeanKey.of(name)).orElseThrow();
	}

	@Override
	public void write(String name, byte[] bytes){
		directory.write(PathbeanKey.of(name), bytes);
	}

	@Override
	public void write(String name, InputStream inputStream, Threads threads){
		directory.writeParallel(
				PathbeanKey.of(name),
				inputStream,
				threads,
				ByteLength.ofMiB(1));
	}

	@Override
	public byte[] read(String name){
		return directory.read(PathbeanKey.of(name)).orElseThrow();
	}

	@Override
	public byte[] readPartial(String name, BlockfileLocation location){
		return directory.readPartial(
				PathbeanKey.of(name),
				location.from(),
				location.length())
				.orElseThrow();
	}

	@Override
	public byte[] readEnding(String name, int length){
		return directory.readEnding(
				PathbeanKey.of(name),
				length)
				.orElseThrow();
	}

	@Override
	public InputStream readInputStream(String name, Threads threads, ByteLength chunkSize){
		return directory.scanChunks(
				PathbeanKey.of(name),
				Range.everything(),
				threads,
				chunkSize)
				.apply(MultiByteArrayInputStream::new);

	}

	@Override
	public void deleteMulti(List<String> names){
		Scanner.of(names)
				.map(PathbeanKey::of)
				.flush(directory::deleteMulti);
	}

}
