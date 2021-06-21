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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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

	private final BlobStorage<PathbeanKey,Pathbean> storage;
	private final Subpath subpathInStorage;

	public Directory(BlobStorage<PathbeanKey,Pathbean> storage){
		this(storage, Subpath.empty());
	}

	public Directory(BlobStorage<PathbeanKey,Pathbean> storage, Subpath subpathInStorage){
		this.storage = storage;
		this.subpathInStorage = subpathInStorage;
	}

	public Directory subdirectory(Subpath subpathInThisDirectory){
		return new Directory(storage, subpathInStorage.append(subpathInThisDirectory));
	}

	@Override
	public void write(PathbeanKey key, byte[] value){
		storage.write(prependStoragePath(key), value);
	}

	@Override
	public void write(PathbeanKey key, Iterator<byte[]> chunks){
		storage.write(prependStoragePath(key), chunks);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		storage.write(prependStoragePath(key), inputStream);
	}

	@Override
	public void delete(PathbeanKey key){
		storage.delete(prependStoragePath(key));
	}

	@Override
	public void deleteAll(Subpath subpath){
		storage.deleteAll(subpathInStorage.append(subpath));
	}

	@Override
	public String getBucket(){
		return storage.getBucket();
	}

	@Override
	public Subpath getRootPath(){
		return storage.getRootPath().append(subpathInStorage);
	}

	@Override
	public boolean exists(PathbeanKey key){
		return storage.exists(prependStoragePath(key));
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		return storage.length(prependStoragePath(key));
	}

	@Override
	public byte[] read(PathbeanKey key){
		return storage.read(prependStoragePath(key));
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		return storage.read(prependStoragePath(key), offset, length);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		return storage.scanKeysPaged(subpathInStorage.append(subpath))
				.map(keys -> Scanner.of(keys)
						.map(this::removeStoragePath)
						.list());
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		return storage.scanPaged(subpathInStorage.append(subpath))
				.map(page -> Scanner.of(page)
						.map(pathbean -> new Pathbean(removeStoragePath(pathbean.getKey()), pathbean.getSize()))
						.list());
	}

	private PathbeanKey prependStoragePath(PathbeanKey directoryKey){
		Subpath storagePath = subpathInStorage.append(directoryKey.getSubpath());
		return PathbeanKey.of(storagePath, directoryKey.getFile());
	}

	private PathbeanKey removeStoragePath(PathbeanKey storageKey){
		String storagePath = storageKey.getPath();
		Require.isTrue(storagePath.startsWith(subpathInStorage.toString()));
		String directoryPath = storagePath.substring(subpathInStorage.toString().length());
		return new PathbeanKey(directoryPath, storageKey.getFile());
	}

}
