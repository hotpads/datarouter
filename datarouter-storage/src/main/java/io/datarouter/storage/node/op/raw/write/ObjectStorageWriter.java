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

import java.nio.charset.StandardCharsets;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.read.ObjectStorageReader;

/**
 * Methods for writing to an object store such as the filesystem or S3.
 */
public interface ObjectStorageWriter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends ObjectStorageReader<PK,D>{

	public static final String OP_write = "write";
	public static final String OP_writeUtf8 = "writeUtf8";
	public static final String OP_delete = "delete";

	void write(PathbeanKey key, byte[] bytes);

	default void writeUtf8(PathbeanKey key, String string){
		write(key, string.getBytes(StandardCharsets.UTF_8));
	}

	void delete(PathbeanKey key);

}