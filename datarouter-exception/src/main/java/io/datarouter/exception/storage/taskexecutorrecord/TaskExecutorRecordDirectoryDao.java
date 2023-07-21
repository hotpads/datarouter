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
package io.datarouter.exception.storage.taskexecutorrecord;

import io.datarouter.exception.dto.TaskExecutorRecordBinaryDto;
import io.datarouter.exception.storage.BaseRecordDirectoryDao;
import io.datarouter.storage.file.Directory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TaskExecutorRecordDirectoryDao
extends BaseRecordDirectoryDao<TaskExecutorRecordBinaryDto>{

	@Inject
	private TaskExecutorRecordDirectorySupplier directory;

	@Override
	protected Directory getDirectory(){
		return directory.getTaskExecutorRecordDirectory();
	}

	@Override
	protected TaskExecutorRecordBinaryDto decode(byte[] bytes){
		return TaskExecutorRecordBinaryDto.decode(bytes);
	}

}
