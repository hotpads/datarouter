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
package io.datarouter.storage.node.op.raw.write;

import java.io.InputStream;
import java.util.Iterator;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.read.BlobStorageReader;
import io.datarouter.storage.util.Subpath;

/**
 * Methods for writing to an object store such as the filesystem or S3.
 */
public interface BlobStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BlobStorageReader<PK,D>{

	void write(PathbeanKey key, byte[] value);

	void write(PathbeanKey key, Iterator<byte[]> chunks);

	void write(PathbeanKey key, InputStream inputStream);

	void delete(PathbeanKey key);

	/**
	 * Delete all descendants and the subpath directory
	 */
	void deleteAll(Subpath subpath);

}
