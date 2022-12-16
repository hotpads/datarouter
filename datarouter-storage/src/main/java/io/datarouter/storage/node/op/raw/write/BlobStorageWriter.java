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
import java.util.concurrent.ExecutorService;

import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.read.BlobStorageReader;
import io.datarouter.storage.util.Subpath;

/**
 * Methods for writing to an object store such as the filesystem or S3.
 */
public interface BlobStorageWriter extends BlobStorageReader{

	/*----- write a byte[] ---------*/

	void write(PathbeanKey key, byte[] value, Config config);

	default void write(PathbeanKey key, byte[] value){
		write(key, value, new Config());
	}

	/*----- write a Scanner<byte[]> ---------*/

	default void write(PathbeanKey key, Scanner<byte[]> chunks, Config config){
		InputStream inputStream = new MultiByteArrayInputStream(chunks);
		write(key, inputStream, config);
	}

	default void write(PathbeanKey key, Scanner<byte[]> chunks){
		write(key, chunks, new Config());
	}

	/*----- write from an InputStream ---------*/

	void write(PathbeanKey key, InputStream inputStream, Config config);

	default void write(PathbeanKey key, InputStream inputStream){
		write(key, inputStream, new Config());
	}

	/*----- write from an InputStream with parallel uploads ---------*/

	// Override in clients that support multi-part upload
	default void writeParallel(
			PathbeanKey key,
			InputStream inputStream,
			@SuppressWarnings("unused") ExecutorService exec,
			@SuppressWarnings("unused") int numThreads,
			Config config){
		write(key, inputStream, config);
	}

	default void writeParallel(PathbeanKey key, InputStream inputStream, ExecutorService exec, int numThreads){
		writeParallel(key, inputStream, exec, numThreads, new Config());
	}

	/*----- delete ---------*/

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
