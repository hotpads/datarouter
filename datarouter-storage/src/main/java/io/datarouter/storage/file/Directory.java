/**
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
import java.util.Optional;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.BlobStorage;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.Require;

/**
 * Lightweight directory representation that is not tracked by the framework.  Duplicates can be created at will for
 * convenience in the application.
 */
public class Directory
implements BlobStorage<PathbeanKey,Pathbean>{

	private final BlobStorage<PathbeanKey,Pathbean> parent;
	private final Subpath subpathInParent;
	private final String counterName;

	/*---------- construct ------------*/

	public Directory(BlobStorage<PathbeanKey,Pathbean> parent){
		this(parent, Subpath.empty(), null);
	}

	public Directory(BlobStorage<PathbeanKey,Pathbean> parent, Subpath subpathInParent){
		this(parent, subpathInParent, null);
	}

	public Directory(BlobStorage<PathbeanKey,Pathbean> parent, Subpath subpathInParent, String counterName){
		this.parent = parent;
		this.subpathInParent = subpathInParent;
		this.counterName = counterName;
	}

	/*---------- subdirectory ------------*/

	public Directory subdirectory(Subpath subpathInParent){
		return new Directory(this, subpathInParent);
	}

	public Directory subdirectory(Subpath subpathInParent, String counterName){
		return new Directory(this, subpathInParent, counterName);
	}

	/*---------- write ------------*/

	@Override
	public void write(PathbeanKey key, byte[] value){
		parent.write(prependStoragePath(key), value);
		count("write ops", 1);
		count("write bytes", value.length);
	}

	@Override
	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		Scanner<byte[]> chunksWithCounts = chunks;
		if(counterName != null){ //slightly optimized for small chunks with fast disks
			chunksWithCounts = chunks.each(chunk -> {
				count("writeChunks chunks", 1);
				count("writeChunks bytes", chunk.length);
			});
		}
		parent.write(prependStoragePath(key), chunksWithCounts);
		count("writeChunks ops", 1);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		parent.write(prependStoragePath(key), inputStream);
		//TODO count bytes
		count("writeInputStream ops", 1);
	}

	/*---------- delete ------------*/

	@Override
	public void delete(PathbeanKey key){
		parent.delete(prependStoragePath(key));
		count("delete ops", 1);
	}

	@Override
	public void deleteAll(Subpath subpath){
		parent.deleteAll(subpathInParent.append(subpath));
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
	public boolean exists(PathbeanKey key){
		boolean exists = parent.exists(prependStoragePath(key));
		count("exists ops", 1);
		return exists;
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		Optional<Long> optLength = parent.length(prependStoragePath(key));
		count("length ops", 1);
		return optLength;
	}

	@Override
	public byte[] read(PathbeanKey key){
		Optional<byte[]> optBytes = Optional.ofNullable(parent.read(prependStoragePath(key)));
		count("read ops", 1);
		optBytes.map(bytes -> bytes.length)
				.ifPresent(length -> count("read bytes", length));
		return optBytes.orElse(null);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		Optional<byte[]> optBytes = Optional.ofNullable(parent.read(prependStoragePath(key), offset, length));
		count("readOffsetLimit ops", 1);
		optBytes.map(bytes -> bytes.length)
				.ifPresent(actualLength -> count("readOffsetLimit bytes", actualLength));
		return optBytes.orElse(null);
	}

	/*---------- scan ------------*/

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		return parent.scanKeysPaged(subpathInParent.append(subpath))
				.map(keys -> Scanner.of(keys)
						.map(this::removeStoragePath)
						.list())
				.each($ -> count("scanKeys ops", 1))
				.each(page -> count("scanKeys items", page.size()));
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		return parent.scanPaged(subpathInParent.append(subpath))
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
		if(counterName != null){
			String name = String.format("Directory %s %s", counterName, suffix);
			Counters.inc(name, by);
		}
	}

	/*------------------ Object ---------------------*/

	@Override
	public String toString(){
		return getRootPath().toString();
	}

}
