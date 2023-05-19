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
package io.datarouter.bytes.kvfile;

import java.io.InputStream;
import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public interface KvFileStorage{

	/*--------- read -----------*/

	List<KvFileNameAndSize> list();

	byte[] read(String name);

	InputStream readInputStream(String name);

	Scanner<byte[]> readParallel(
			String name,
			long fromInclusive,
			long toExclusive,
			Threads threads,
			ByteLength chunkSize);

	/*--------- read -----------*/

	void write(String name, byte[] value);

	void writeParallel(
			String name,
			Scanner<List<byte[]>> parts,
			Threads threads);

	void deleteMulti(List<String> names);

}
