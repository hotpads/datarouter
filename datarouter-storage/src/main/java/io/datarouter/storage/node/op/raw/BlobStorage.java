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
package io.datarouter.storage.node.op.raw;

import io.datarouter.bytes.Codec;
import io.datarouter.storage.file.DatabaseBlob;
import io.datarouter.storage.file.DatabaseBlob.DatabaseBlobFielder;
import io.datarouter.storage.file.DatabaseBlobKey;
import io.datarouter.storage.node.op.raw.write.BlobStorageWriter;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public interface BlobStorage extends BlobStorageWriter{

	char FILE_PATH_DELIMITER_CHAR = '/';
	String FILE_PATH_DELIMITER = Character.toString(FILE_PATH_DELIMITER_CHAR);

	interface BlobStorageNode extends BlobStorage{
	}

	interface PhysicalBlobStorageNode
	extends BlobStorageNode, PhysicalNode<DatabaseBlobKey,DatabaseBlob,DatabaseBlobFielder>{
	}

	default <T> EncodedBlobStorage<T> encoded(Codec<T,byte[]> codec){
		return new EncodedBlobStorage<>(this, codec);
	}

}
