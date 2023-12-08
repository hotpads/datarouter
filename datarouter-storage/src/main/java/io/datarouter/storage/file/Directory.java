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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.io.CountingInputStream;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.BlobStorage;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Require;

/**
 * Lightweight directory representation that is not tracked by the framework.
 * Duplicates can be created at will for convenience in the application.
 * Counting is implemented at this layer if specified.
 */
public class Directory
implements BlobStorage{

	private static final int INPUT_STREAM_COUNT_INTERVAL = ByteLength.ofKiB(256).toBytesInt();

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
		count(CounterSuffix.WRITE_OPS, 1);
		count(CounterSuffix.WRITE_BYTES, value.length);
	}

	@Override
	public void writeInputStream(PathbeanKey key, InputStream inputStream, Config config){
		var countingInputStream = new CountingInputStream(
				inputStream,
				INPUT_STREAM_COUNT_INTERVAL,
				numBytes -> count(CounterSuffix.WRITE_INPUT_STREAM_BYTES, numBytes));
		parent.writeInputStream(prependStoragePath(key), countingInputStream, config);
		count(CounterSuffix.WRITE_INPUT_STREAM_OPS, 1);
	}

	@Override
	public void writeParallel(
			PathbeanKey key,
			InputStream inputStream,
			Threads threads,
			ByteLength minPartSize,
			Config config){
		var countingInputStream = new CountingInputStream(
				inputStream,
				INPUT_STREAM_COUNT_INTERVAL,
				numBytes -> count(CounterSuffix.WRITE_INPUT_STREAM_BYTES, numBytes));
		parent.writeParallel(prependStoragePath(key), countingInputStream, threads, minPartSize, config);
		count(CounterSuffix.WRITE_INPUT_STREAM_OPS, 1);
	}

	@Override
	public void writeParallel(
			PathbeanKey key,
			Scanner<List<byte[]>> parts,
			Threads threads,
			Config config){
		Scanner<List<byte[]>> countedParts = parts.each(part -> {
			count(CounterSuffix.WRITE_PARTS_PARTS, 1);
			count(CounterSuffix.WRITE_PARTS_BYTES, ByteTool.totalLength(part));
		});
		parent.writeParallel(prependStoragePath(key), countedParts, threads, config);
		count(CounterSuffix.WRITE_PARTS_OPS, 1);
	}

	/*---------- delete ------------*/

	@Override
	public void delete(PathbeanKey key, Config config){
		parent.delete(prependStoragePath(key), config);
		count(CounterSuffix.DELETE_OPS, 1);
	}

	@Override
	public void deleteMulti(List<PathbeanKey> keys, Config config){
		Scanner.of(keys)
				.map(this::prependStoragePath)
				.flush(parent::deleteMulti);
		count(CounterSuffix.DELETE_MULTI_OPS, 1);
		count(CounterSuffix.DELETE_MULTI_ITEMS, keys.size());
	}

	@Override
	public void deleteAll(Subpath subpath, Config config){
		parent.deleteAll(subpathInParent.append(subpath), config);
		count(CounterSuffix.DELETE_ALL_OPS, 1);
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
		count(CounterSuffix.EXISTS_OPS, 1);
		return exists;
	}

	@Override
	public Optional<Long> length(PathbeanKey key, Config config){
		Optional<Long> optLength = parent.length(prependStoragePath(key), config);
		count(CounterSuffix.LENGTH_OPS, 1);
		return optLength;
	}

	@Override
	public Optional<byte[]> read(PathbeanKey key, Config config){
		Optional<byte[]> optBytes = parent.read(prependStoragePath(key), config);
		count(CounterSuffix.READ_OPS, 1);
		optBytes.map(bytes -> bytes.length)
				.ifPresent(length -> count(CounterSuffix.READ_BYTES, length));
		return optBytes;
	}

	@Override
	public Optional<byte[]> readPartial(PathbeanKey key, long offset, int length, Config config){
		Optional<byte[]> optBytes = parent.readPartial(
				prependStoragePath(key),
				offset,
				length,
				config);
		count(CounterSuffix.READ_PARTIAL_OPS, 1);
		optBytes.map(bytes -> bytes.length)
				.ifPresent(actualLength -> count(CounterSuffix.READ_PARTIAL_BYTES, actualLength));
		return optBytes;
	}

	@Override
	public Map<PathbeanKey,byte[]> readMulti(List<PathbeanKey> keys, Config config){
		Map<PathbeanKey,byte[]> result = parent.readMulti(keys, config);
		count(CounterSuffix.READ_MULTI_OPS, 1);
		count(CounterSuffix.READ_MULTI_KEYS, keys.size());
		long totalBytes = result.values().stream()
				.mapToLong(bytes -> bytes.length)
				.sum();
		count(CounterSuffix.READ_MULTI_BYTES, totalBytes);
		return result;
	}

	@Override
	public InputStream readInputStream(PathbeanKey key, Config config){
		count(CounterSuffix.READ_INPUT_STREAM_OPS, 1);
		return new CountingInputStream(
				parent.readInputStream(prependStoragePath(key), config),
				INPUT_STREAM_COUNT_INTERVAL,
				numBytes -> count(CounterSuffix.READ_INPUT_STREAM_BYTES, numBytes));
	}

	/*---------- scan ------------*/

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config){
		return parent.scanKeysPaged(subpathInParent.append(subpath), config)
				.map(keys -> Scanner.of(keys)
						.map(this::removeStoragePath)
						.list())
				.each($ -> count(CounterSuffix.SCAN_KEYS_OPS, 1))
				.each(page -> count(CounterSuffix.SCAN_KEYS_ITEMS, page.size()));
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config){
		return parent.scanPaged(subpathInParent.append(subpath), config)
				.map(page -> Scanner.of(page)
						.map(pathbean -> new Pathbean(removeStoragePath(pathbean.getKey()), pathbean.getSize()))
						.list())
				.each($ -> count(CounterSuffix.SCAN_OPS, 1))
				.each(page -> count(CounterSuffix.SCAN_ITEMS, page.size()));
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

	/*------------- counts --------------*/

	private void count(CounterSuffix suffix, long by){
		optCounterName.ifPresent(counterName -> {
			String name = String.format("Directory %s %s", counterName, suffix.suffix);
			Counters.inc(name, by);
		});
	}

	private enum CounterSuffix{
		DELETE_ALL_OPS("deleteAll ops"),
		DELETE_MULTI_ITEMS("deleteMulti items"),
		DELETE_MULTI_OPS("deleteMulti ops"),
		DELETE_OPS("delete ops"),
		EXISTS_OPS("exists ops"),
		LENGTH_OPS("length ops"),
		READ_BYTES("read bytes"),
		READ_INPUT_STREAM_BYTES("readInputStream bytes"),
		READ_INPUT_STREAM_OPS("readInputStream ops"),
		READ_PARTIAL_BYTES("readPartial bytes"),
		READ_PARTIAL_OPS("readPartial ops"),
		READ_OPS("read ops"),
		READ_MULTI_OPS("readMulti ops"),
		READ_MULTI_KEYS("readMulti keys"),
		READ_MULTI_BYTES("readMulti bytes"),
		SCAN_KEYS_OPS("scanKeys ops"),
		SCAN_KEYS_ITEMS("scanKeys items"),
		SCAN_OPS("scan ops"),
		SCAN_ITEMS("scan items"),
		WRITE_BYTES("write bytes"),
		WRITE_OPS("write ops"),
		WRITE_INPUT_STREAM_BYTES("writeInputStream bytes"),
		WRITE_INPUT_STREAM_OPS("writeInputStream ops"),
		WRITE_PARTS_BYTES("writeParts bytes"),
		WRITE_PARTS_OPS("writeParts ops"),
		WRITE_PARTS_PARTS("writeParts parts");

		public final String suffix;

		CounterSuffix(String suffix){
			this.suffix = suffix;
		}
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
