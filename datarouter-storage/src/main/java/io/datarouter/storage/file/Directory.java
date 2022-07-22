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
package io.datarouter.storage.file;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.BlobStorage;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Require;

/**
 * Lightweight directory representation that is not tracked by the framework.  Duplicates can be created at will for
 * convenience in the application.
 */
public class Directory
implements BlobStorage{

	private final BlobStorage parent;
	private final Subpath subpathInParent;
	private final Optional<String> optCounterName;

	/*---------- construct ------------*/

	public Directory(BlobStorage parent){
		this(parent, Subpath.empty(), null);
	}

	public Directory(BlobStorage parent, Subpath subpathInParent){
		this(parent, subpathInParent, null);
	}

	// private, called by builder
	private Directory(BlobStorage parent, Subpath subpathInParent, String counterName){
		this.parent = Objects.requireNonNull(parent);
		this.subpathInParent = Objects.requireNonNull(subpathInParent);
		this.optCounterName = Optional.ofNullable(counterName);
	}

	/*---------- subdirectory ------------*/

	public Directory subdirectory(Subpath subpathInParent){
		return subdirectoryBuilder(subpathInParent)
				.build();
	}

	public SubdirectoryBuilder subdirectoryBuilder(Subpath subpathInParent){
		return new SubdirectoryBuilder(this, subpathInParent);
	}

	/*---------- write ------------*/

	@Override
	public void write(PathbeanKey key, byte[] value, Config config){
		parent.write(prependStoragePath(key), value, config);
		count("write ops", 1);
		count("write bytes", value.length);
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks, Config config){
		Scanner<byte[]> chunksWithCounts = chunks;
		if(optCounterName.isPresent()){
			chunksWithCounts = chunks.each(chunk -> {
				count("writeChunks chunks", 1);
				count("writeChunks bytes", chunk.length);
			});
		}
		parent.write(prependStoragePath(key), chunksWithCounts, config);
		count("writeChunks ops", 1);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream, Config config){
		parent.write(prependStoragePath(key), inputStream, config);
		//TODO count bytes
		count("writeInputStream ops", 1);
	}

	/*---------- delete ------------*/

	@Override
	public void delete(PathbeanKey key, Config config){
		parent.delete(prependStoragePath(key), config);
		count("delete ops", 1);
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		parent.deleteAll(subpathInParent.append(subpath), config);
		count("deleteAll ops", 1);
	}

	/*---------- metadata ------------*/

	@Override
	public String getBucket(){
		return parent.getBucket();
	}

	@Override
	public Subpath getRootPath(){
		return parent.getRootPath().append(subpathInParent);
	}

	/*---------- read ------------*/

	@Override
	public boolean exists(PathbeanKey key, Config config){
		boolean exists = parent.exists(prependStoragePath(key), config);
		count("exists ops", 1);
		return exists;
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		Optional<Long> optLength = parent.length(prependStoragePath(key), config);
		count("length ops", 1);
		return optLength;
	}

	@Override
	public byte[] read(PathbeanKey key, Config config){
		Optional<byte[]> optBytes = Optional.ofNullable(parent.read(prependStoragePath(key), config));
		count("read ops", 1);
		optBytes.map(bytes -> bytes.length)
				.ifPresent(length -> count("read bytes", length));
		return optBytes.orElse(null);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length, Config config){
		Optional<byte[]> optBytes = Optional.ofNullable(parent.read(prependStoragePath(key), offset, length, config));
		count("readOffsetLimit ops", 1);
		optBytes.map(bytes -> bytes.length)
				.ifPresent(actualLength -> count("readOffsetLimit bytes", actualLength));
		return optBytes.orElse(null);
	}

	@Override
	public Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys, Config config){
		Map<PathbeanKey,byte[]> keyValue = new HashMap<>();
		keys.forEach(key -> {
				Optional<byte[]> optBytes = Optional.ofNullable(parent.read(prependStoragePath(key), config));
				count("read ops", 1);
				optBytes.map(bytes -> bytes.length)
						.ifPresent(actualLength -> count("read bytes", actualLength));
				keyValue.putIfAbsent(key, optBytes.orElse(null));
			});
		return keyValue;
	}

	/*---------- scan ------------*/

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		return parent.scanKeysPaged(subpathInParent.append(subpath), config)
				.map(keys -> Scanner.of(keys)
						.map(this::removeStoragePath)
						.list())
				.each($ -> count("scanKeys ops", 1))
				.each(page -> count("scanKeys items", page.size()));
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		return parent.scanPaged(subpathInParent.append(subpath), config)
				.map(page -> Scanner.of(page)
						.map(pathbean -> new Pathbean(removeStoragePath(pathbean.getKey()), pathbean.getSize()))
						.list())
				.each($ -> count("scan ops", 1))
				.each(page -> count("scan items", page.size()));
	}

	/*------------------ private ---------------------*/

	private PathbeanKey prependStoragePath(PathbeanKey directoryKey){
		Subpath storagePath = subpathInParent.append(directoryKey.getSubpath());
		return PathbeanKey.of(storagePath, directoryKey.getFile());
	}

	private PathbeanKey removeStoragePath(PathbeanKey storageKey){
		String storagePath = storageKey.getPath();
		Require.isTrue(storagePath.startsWith(subpathInParent.toString()));
		String directoryPath = storagePath.substring(subpathInParent.toString().length());
		return new PathbeanKey(directoryPath, storageKey.getFile());
	}

	private void count(String suffix, long by){
		optCounterName.ifPresent(counterName -> {
			String name = String.format("Directory %s %s", counterName, suffix);
			Counters.inc(name, by);
		});
	}

	/*------------------ Object ---------------------*/

	@Override
	public String toString(){
		return getRootPath().toString();
	}

	/*-------------- builder ------------------*/

	public static class DirectoryBuilder{

		private final BlobStorage parent;
		private Subpath subpathInParent;
		private String counterName;

		public DirectoryBuilder(BlobStorage parent){
			this.parent = Objects.requireNonNull(parent);
			this.subpathInParent = Subpath.empty();
		}

		public DirectoryBuilder withSubpathInParent(Subpath subpathInParent){
			this.subpathInParent = Objects.requireNonNull(subpathInParent);
			return this;
		}

		public DirectoryBuilder withCounterName(String counterName){
			this.counterName = Objects.requireNonNull(counterName);
			return this;
		}

		public Directory build(){
			return new Directory(parent, subpathInParent, counterName);
		}

	}

	public static class SubdirectoryBuilder{

		private final DirectoryBuilder directoryBuilder;

		public SubdirectoryBuilder(BlobStorage parent, Subpath subpathInParent){
			this.directoryBuilder = new DirectoryBuilder(parent)
					.withSubpathInParent(subpathInParent);
		}

		public SubdirectoryBuilder withCounterName(String counterName){
			directoryBuilder.withCounterName(counterName);
			return this;
		}

		public Directory build(){
			return directoryBuilder.build();
		}

	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
