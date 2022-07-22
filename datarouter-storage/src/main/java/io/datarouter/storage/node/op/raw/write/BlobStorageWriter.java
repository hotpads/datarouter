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
package io.datarouter.storage.node.op.raw.write;

import java.io.InputStream;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.read.BlobStorageReader;
import io.datarouter.storage.util.Subpath;

/**
 * Methods for writing to an object store such as the filesystem or S3.
 */
public interface BlobStorageWriter extends BlobStorageReader{

	void write(PathbeanKey key, byte[] value, Config config);

	default void write(PathbeanKey key, byte[] value){
		write(key, value, new Config());
	}

	void write(PathbeanKey key, Scanner<byte[]> chunks, Config config);

	default void write(PathbeanKey key, Scanner<byte[]> chunks){
		write(key, chunks, new Config());
	}

	void write(PathbeanKey key, InputStream inputStream, Config config);

	default void write(PathbeanKey key, InputStream inputStream){
		write(key, inputStream, new Config());
	}

	void delete(PathbeanKey key, Config config);

	default void delete(PathbeanKey key){
		delete(key, new Config());
	}

	/**
	 * Delete all descendants and the subpath directory
	 */
	void deleteAll(Subpath subpath, Config config);

	default void deleteAll(Subpath subpath){
		deleteAll(subpath, new Config());
	}

	void vacuum(Config config);

}
