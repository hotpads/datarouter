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
package io.datarouter.plugin.dataexport.test;

import io.datarouter.client.memory.test.DatarouterMemoryTestClientIds;
import io.datarouter.plugin.dataexport.config.DatarouterDataExportDirectorySupplier;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.node.factory.BlobNodeFactory;
import io.datarouter.storage.node.op.raw.BlobStorage;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TestDatarouterDataExportDirectorySupplier implements DatarouterDataExportDirectorySupplier{

	private final Directory directory;

	@Inject
	public TestDatarouterDataExportDirectorySupplier(
			BlobNodeFactory blobNodeFactory){
		BlobStorage blobNode = blobNodeFactory.create(
				DatarouterMemoryTestClientIds.MEMORY,
				"fakeBucket",
				Subpath.empty());
		directory = new Directory(blobNode);
	}

	@Override
	public Directory getDirectory(){
		return directory;
	}
}
