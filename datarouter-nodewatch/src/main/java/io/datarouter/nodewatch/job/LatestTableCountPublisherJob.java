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
package io.datarouter.nodewatch.job;

import javax.inject.Inject;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.instrumentation.tablecount.TableCountBatchDto;
import io.datarouter.instrumentation.tablecount.TableCountDto;
import io.datarouter.instrumentation.tablecount.TableCountPublisher;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;

public class LatestTableCountPublisherJob extends BaseJob{

	@Inject
	private TableCountPublisher publisher;
	@Inject
	private DatarouterLatestTableCountDao dao;
	@Inject
	private DatarouterService datarouterService;

	@Override
	public void run(TaskTracker tracker){
		dao.scan()
				.map(this::toDto)
				.batch(50)
				.map(TableCountBatchDto::new)
				.forEach(publisher::add);
	}

	private TableCountDto toDto(LatestTableCount count){
		return new TableCountDto(
				datarouterService.getName(),
				count.getKey().getClientName(),
				count.getKey().getTableName(),
				count.getNumRows(),
				count.getDateUpdated(),
				count.getCountTimeMs(),
				count.getNumSpans(),
				count.getNumSlowSpans());
	}

}
