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
package io.datarouter.bytes.blockfile.storage;

import java.io.InputStream;
import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.dto.BlockfileNameAndSize;
import io.datarouter.scanner.Threads;

/**
 * "name" may contain subpaths, but without a leading slash.  Example: subdir/name
 */
public interface BlockfileStorage{

	List<BlockfileNameAndSize> list();

	/*--------- read -----------*/

	long length(String name);

	byte[] read(String name);

	byte[] readPartial(String name, long offset, int limit);

	InputStream readInputStream(String name, Threads threads, ByteLength chunkSize);

	/*--------- write -----------*/

	void write(String name, byte[] bytes);

	void write(String name, InputStream inputStream, Threads threads);

	/*--------- delete -----------*/

	void deleteMulti(List<String> names);

}
