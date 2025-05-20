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
package io.datarouter.nodewatch.shadowtable;

import java.time.LocalTime;

import io.datarouter.job.detached.DetachedJobResource;
import io.datarouter.job.detached.DetachedJobResource.DetachedJobResourceBuilder;

public record ShadowTableExport(
		String name,
		String clientName,
		LocalTime time,
		ShadowTableExportResource resource){

	public String cronString(){
		return "0 %s %s ? * * *".formatted(time.getMinute(), time.getHour());
	}

	public DetachedJobResource detachedJobResource(){
		return new DetachedJobResourceBuilder()
				.withCpuMilli(1_000 * resource.vcpus)
				.withMemoryMb(1_024 * resource.gibibytes)
				.build();
	}

	public record ShadowTableExportResource(
			int vcpus,
			int gibibytes,
			int databaseExportThreads){
	}

}
