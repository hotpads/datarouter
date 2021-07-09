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
package io.datarouter.filesystem.node.object;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import io.datarouter.filesystem.raw.DirectoryManager;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

/**
 * Wrap DirectoryManager with methods dealing in Pathbeans
 */
public class DirectoryBlobStorage{

	private final DirectoryManager directoryManager;

	public DirectoryBlobStorage(DirectoryManager directoryManager){
		this.directoryManager = directoryManager;
	}

	public String getRoot(){
		return directoryManager.getRoot().toString();
	}

	public boolean exists(PathbeanKey key){
		return directoryManager.exists(key.getPathAndFile());
	}

	public Optional<Long> length(PathbeanKey key){
		return directoryManager.length(key.getPathAndFile());
	}

	public byte[] read(PathbeanKey key){
		return directoryManager.read(key.getPathAndFile());
	}

	public byte[] read(PathbeanKey key, long offset, int length){
		return directoryManager.read(key.getPathAndFile(), offset, length);
	}

	public void write(PathbeanKey key, byte[] bytes){
		write(key, ObjectScanner.of(bytes));
	}

	public void write(PathbeanKey key, Scanner<byte[]> chunks){
		directoryManager.write(key.getPathAndFile(), chunks);
	}

	public void write(PathbeanKey key, InputStream inputStream){
		directoryManager.write(key.getPathAndFile(), inputStream);
	}

	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		return directoryManager.scanDescendantsPaged(subpath, false, true)
				.map(paths -> Scanner.of(paths)
						.map(path -> {
							PathbeanKey key = PathbeanKey.of(path);
							Long size = directoryManager.size(key.getPathAndFile());
							return new Pathbean(key, size);
						})
						.list());
	}

	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		return directoryManager.scanDescendantsPaged(subpath, false, true)
				.map(paths -> Scanner.of(paths)
						.map(PathbeanKey::of)
						.list());
	}

	public void delete(PathbeanKey key){
		directoryManager.delete(key.getPathAndFile());
	}

	public void deleteAll(Subpath subpath){
		directoryManager.deleteDescendants(subpath);
		directoryManager.delete(subpath.toString());
	}

}
