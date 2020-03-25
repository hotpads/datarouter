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
package io.datarouter.nodewatch.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.service.JobletService;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;

@Singleton
public class TableSpanSamplerJobletCreatorFactory{

	@Inject
	private DatarouterTableSampleDao tableSampleDao;
	@Inject
	private JobletService jobletService;

	public TableSpanSamplerJobletCreator<?,?,?> create(
			PhysicalSortedStorageReaderNode<?,?,?> node,
			int sampleSize,
			int batchSize,
			boolean forceUpdateRecentCounts,
			boolean submitJoblets,
			long samplerStartMs){
		return new TableSpanSamplerJobletCreator<>(
				tableSampleDao,
				jobletService,
				node,
				sampleSize,
				batchSize,
				forceUpdateRecentCounts,
				submitJoblets,
				samplerStartMs);
	}

}
