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
import io.datarouter.bytes.kvfile.KvFileNameAndSize;
import io.datarouter.bytes.kvfile.KvFileStorage;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.util.tuple.Range;

public class DirectoryKvFileStorage implements KvFileStorage{

	private final Directory directory;

	public DirectoryKvFileStorage(Directory directory){
		this.directory = directory;
	}

	@Override
	public List<KvFileNameAndSize> list(){
		return directory.scan(Subpath.empty())
				.map(pathbean -> new KvFileNameAndSize(pathbean.getKey().getFile(), pathbean.getSize()))
				.list();
	}

	@Override
	public void write(String name, byte[] value){
		directory.write(PathbeanKey.of(name), value);
	}

	@Override
	public void writeParallel(
			String name,
			Scanner<List<byte[]>> parts,
			Threads threads){
		directory.writeParallel(PathbeanKey.of(name), parts, threads);
	}

	@Override
	public byte[] read(String name){
		return directory.read(PathbeanKey.of(name));
	}

	@Override
	public InputStream readInputStream(String name){
		return directory.readInputStream(PathbeanKey.of(name));
	}

	@Override
	public Scanner<byte[]> readParallel(
			String name,
			long fromInclusive,
			long toExclusive,
			Threads threads,
			ByteLength chunkSize){
		return directory.scanChunks(
				PathbeanKey.of(name),
				new Range<>(fromInclusive, true, toExclusive, false),
				threads,
				chunkSize);
	}

	@Override
	public void delete(String name){
		directory.delete(PathbeanKey.of(name));
	}

}
