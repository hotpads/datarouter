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
package io.datarouter.plugin.copytable;

import java.time.Duration;
import java.util.Optional;

import javax.inject.Inject;

import io.datarouter.joblet.codec.BaseGsonJobletCodec;
import io.datarouter.joblet.model.BaseJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;
import io.datarouter.plugin.copytable.CopyTableJoblet.CopyTableJobletParams;
import io.datarouter.plugin.copytable.CopyTableService.CopyTableSpanResult;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.timer.PhaseTimer;

public class CopyTableJoblet extends BaseJoblet<CopyTableJobletParams>{

	public static final JobletType<CopyTableJobletParams> JOBLET_TYPE = new JobletTypeBuilder<>(
			"CopyTableJoblet", //unnecessary word "Joblet" in name
			CopyTableJobletCodec::new,
			CopyTableJoblet.class)
			.withShortQueueName("CopyTable") //unnecessary shortQueueName
			.withPollingPeriod(Duration.ofMinutes(1))
			.build();

	private static final boolean PERSISTENT_PUT = false;

	@Inject
	private CopyTableService copyTableService;
	@Inject
	private CopyTableConfiguration copyTableConfiguration;

	@Override
	public Long process(){
		PhaseTimer timer = new PhaseTimer();
		params.optFilterName().ifPresent(copyTableConfiguration::assertValidFilter);
		CopyTableSpanResult result = copyTableService.copyTableSpan(
				params.sourceNodeName,
				params.targetNodeName,
				params.fromKeyExclusive,
				params.toKeyInclusive,
				copyTableConfiguration.findFilter(params.filterName).orElse(null),
				copyTableConfiguration.findProcessor(params.processorName).orElse(null),
				BooleanTool.isTrue(params.autoResume),//TODO remove null check after 2019-09-01
				1,//single thread, rely on joblet system for parallelism
				params.batchSize,
				PERSISTENT_PUT,
				NumberTool.nullSafeLong(params.jobletId, 0L),//can remove null check after migration period
				NumberTool.nullSafeLong(params.numJoblets, 0L));
		if(!result.success){
			throw result.exception;
		}
		return timer.getElapsedTimeBetweenFirstAndLastEvent();
	}

	public static class CopyTableJobletParams{

		public final String sourceNodeName;
		public final String targetNodeName;
		public final String fromKeyExclusive;
		public final String toKeyInclusive;
		public final String filterName;
		public final String processorName;
		public final Boolean autoResume;
		public final Integer batchSize;
		public final Long estNumDatabeans;
		public final Long jobletId;
		public final Long numJoblets;

		public CopyTableJobletParams(
				String sourceNodeName,
				String targetNodeName,
				String fromKeyExclusive,
				String toKeyInclusive,
				String filterName,
				String processorName,
				Boolean autoResume,
				Integer batchSize,
				Long estNumDatabeans,
				Long jobletId,
				Long numJoblets){
			this.sourceNodeName = sourceNodeName;
			this.targetNodeName = targetNodeName;
			this.fromKeyExclusive = fromKeyExclusive;
			this.toKeyInclusive = toKeyInclusive;
			this.filterName = filterName;
			this.processorName = processorName;
			this.autoResume = autoResume;
			this.batchSize = batchSize;
			this.estNumDatabeans = estNumDatabeans;
			this.jobletId = jobletId;
			this.numJoblets = numJoblets;
		}

		public Optional<String> optFilterName(){
			return Optional.ofNullable(filterName);
		}
	}

	public static class CopyTableJobletCodec extends BaseGsonJobletCodec<CopyTableJobletParams>{

		public CopyTableJobletCodec(){
			super(CopyTableJobletParams.class);
		}

		@Override
		public int calculateNumItems(CopyTableJobletParams params){
			return params.estNumDatabeans.intValue();
		}
	}

}
