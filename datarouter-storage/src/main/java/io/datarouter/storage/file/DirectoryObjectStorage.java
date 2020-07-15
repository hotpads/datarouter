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

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.read.ObjectStorageReader;
import io.datarouter.util.Require;

public class DirectoryObjectStorage implements ObjectStorageReader{
	private static final Logger logger = LoggerFactory.getLogger(DirectoryObjectStorage.class);

	private final DirectoryManager directoryManager;

	public DirectoryObjectStorage(DirectoryManager directoryManager){
		this.directoryManager = directoryManager;
	}

	@Override
	public boolean exists(PathbeanKey key){
		return directoryManager.exists(key.getPathAndFile());
	}

	@Override
	public Scanner<Pathbean> scan(){
		return directoryManager.scanDescendants(false, true)
				.map(path -> {
					PathbeanKey key = makeKey(path);
					Long size = directoryManager.size(key.getPathAndFile());
					return new Pathbean(key, size);
				});
	}

	@Override
	public Scanner<PathbeanKey> scanKeys(){
		return directoryManager.scanDescendants(false, true)
				.map(path -> makeKey(path));
	}

	private static PathbeanKey makeKey(Path path){
		Require.greaterThan(path.getNameCount(), 0);
		String keyPath = makeKeyPath(path);
		String keyFile = path.getFileName().toString();
		return new PathbeanKey(keyPath, keyFile);
	}

	private static String makeKeyPath(Path path){
		int nameCount = path.getNameCount();
		return nameCount == 1
				? ""
				: path.subpath(0, nameCount - 1) + "/";
	}

}
