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
package io.datarouter.plugin.copytable;

import java.time.Duration;
import java.util.Optional;

import io.datarouter.joblet.codec.DatarouterBaseGsonJobletCodec;
import io.datarouter.joblet.model.BaseJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;
import io.datarouter.plugin.copytable.CopyTableJoblet.CopyTableJobletParams;
import io.datarouter.plugin.copytable.CopyTableService.CopyTableSpanResult;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.number.NumberTool;
import jakarta.inject.Inject;

public class CopyTableJoblet extends BaseJoblet<CopyTableJobletParams>{

	public static final JobletType<CopyTableJobletParams> JOBLET_TYPE = new JobletTypeBuilder<>(
			"CopyTableJoblet", //unnecessary word "Joblet" in name
			CopyTableJobletCodec::new,
			CopyTableJoblet.class)
			.withShortQueueName("CopyTable") //unnecessary shortQueueName
			.withPollingPeriod(Duration.ofMinutes(1))
			.withTag(Tag.DATAROUTER)
			.build();

	@Inject
	private CopyTableService copyTableService;

	@Override
	public void process() throws Throwable{
		CopyTableSpanResult result = copyTableService.copyTableSpan(
				params.sourceNodeName,
				params.targetNodeName,
				params.fromKeyExclusive,
				params.toKeyInclusive,
				1,//single thread, rely on joblet system for parallelism
				params.scanBatchSize,
				params.putBatchSize,
				NumberTool.nullSafeLong(params.jobletId, 0L),//can remove null check after migration period
				NumberTool.nullSafeLong(params.numJoblets, 0L),
				Optional.ofNullable(params.skipInvalidDatabeans)//TODO remove null check after migration period
						.orElse(true));
		if(!result.success()){
			throw result.exception();
		}
	}

	public record CopyTableJobletParams(
			String sourceNodeName,
			String targetNodeName,
			String fromKeyExclusive,
			String toKeyInclusive,
			Integer scanBatchSize,
			Integer putBatchSize,
			Long estNumDatabeans,
			Long jobletId,
			Long numJoblets,
			Boolean skipInvalidDatabeans){
	}

	public static class CopyTableJobletCodec
	extends DatarouterBaseGsonJobletCodec<CopyTableJobletParams>{

		public CopyTableJobletCodec(){
			super(CopyTableJobletParams.class);
		}

		@Override
		public int calculateNumItems(CopyTableJobletParams params){
			return params.estNumDatabeans.intValue();
		}
	}

}
