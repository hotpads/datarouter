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
package io.datarouter.plugin.copytable.tableprocessor;

import java.time.Duration;

import javax.inject.Inject;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.joblet.codec.BaseGsonJobletCodec;
import io.datarouter.joblet.model.BaseJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletType.JobletTypeBuilder;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorJoblet.TableProcessorJobletParams;
import io.datarouter.plugin.copytable.tableprocessor.TableProcessorService.TableProcessorSpanResult;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.number.NumberTool;

public class TableProcessorJoblet extends BaseJoblet<TableProcessorJobletParams>{

	public static final JobletType<TableProcessorJobletParams> JOBLET_TYPE = new JobletTypeBuilder<>(
			"TableProcessor",
			TableProcessorJobletCodec::new,
			TableProcessorJoblet.class)
			.withPollingPeriod(Duration.ofMinutes(1))
			.withTag(Tag.DATAROUTER)
			.build();

	@Inject
	private TableProcessorService service;
	@Inject
	private TableProcessorRegistry processorRegistry;
	@Inject
	private DatarouterInjector injector;

	@Override
	public void process() throws Throwable{
		TableProcessor<?,?> processor = injector.getInstance(processorRegistry.find(params.processorName)
				.get());
		TableProcessorSpanResult result = service.runTableProcessor(
				params.nodeName,
				params.fromKeyExclusive,
				params.toKeyInclusive,
				processor,
				1,//single thread, rely on joblet system for parallelism
				params.batchSize,
				NumberTool.nullSafeLong(params.jobletId, 0L),//can remove null check after migration period
				NumberTool.nullSafeLong(params.numJoblets, 0L));
		if(!result.success){
			throw result.exception;
		}
	}

	public static class TableProcessorJobletParams{

		public final String nodeName;
		public final String fromKeyExclusive;
		public final String toKeyInclusive;
		public final String processorName;
		public final Integer batchSize;
		public final Long estNumDatabeans;
		public final Long jobletId;
		public final Long numJoblets;

		public TableProcessorJobletParams(
				String nodeName,
				String fromKeyExclusive,
				String toKeyInclusive,
				String processorName,
				Integer batchSize,
				Long estNumDatabeans,
				Long jobletId,
				Long numJoblets){
			this.nodeName = nodeName;
			this.fromKeyExclusive = fromKeyExclusive;
			this.toKeyInclusive = toKeyInclusive;
			this.processorName = processorName;
			this.batchSize = batchSize;
			this.estNumDatabeans = estNumDatabeans;
			this.jobletId = jobletId;
			this.numJoblets = numJoblets;
		}

	}

	public static class TableProcessorJobletCodec extends BaseGsonJobletCodec<TableProcessorJobletParams>{

		public TableProcessorJobletCodec(){
			super(TableProcessorJobletParams.class);
		}

		@Override
		public int calculateNumItems(TableProcessorJobletParams params){
			return params.estNumDatabeans.intValue();
		}

	}

}
