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

import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.Pathbean.PathbeanFielder;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.write.BlobStorageWriter;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public interface BlobStorage extends BlobStorageWriter{

	/*---------------------------- sub-interfaces ---------------------------*/

	public interface BlobStorageNode extends BlobStorage{
	}

	public interface PhysicalBlobStorageNode
	extends BlobStorageNode, PhysicalNode<PathbeanKey,Pathbean,PathbeanFielder>{
	}

}
