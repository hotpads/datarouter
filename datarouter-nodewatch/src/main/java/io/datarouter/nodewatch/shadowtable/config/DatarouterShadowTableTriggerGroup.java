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
package io.datarouter.nodewatch.shadowtable.config;

import java.util.function.Supplier;

import io.datarouter.enums.MappedEnum;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.nodewatch.shadowtable.ShadowTableConfig;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.nodewatch.shadowtable.job.BaseShadowTableExportJob;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob0;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob1;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob2;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob3;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob4;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob5;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob6;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob7;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob8;
import io.datarouter.nodewatch.shadowtable.job.exportimpl.ShadowTableExportJob9;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableNodeSelectionService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.time.ZoneIds;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterShadowTableTriggerGroup extends BaseTriggerGroup{

	@Inject
	public DatarouterShadowTableTriggerGroup(
			DatarouterShadowTableSettingRoot shadowTableSettings,
			ShadowTableConfig shadowTableConfig,
			ShadowTableNodeSelectionService nodeSelectionService){
		super("DatarouterShadowTable", Tag.DATAROUTER, ZoneIds.AMERICA_NEW_YORK);

		//Create a separate job for each ShadowTableExport, usually one per database client.
		//Sorted by clientName so you can figure out which job to trigger.
		Scanner.iterate(0, i -> i + 1)
				.limit(shadowTableConfig.numExports())
				.forEach(index -> registerShadowTableExport(
						shadowTableSettings,
						nodeSelectionService,
						shadowTableConfig.exportWithIndex(index),
						index));
	}

	private void registerShadowTableExport(
			DatarouterShadowTableSettingRoot shadowTableSettings,
			ShadowTableNodeSelectionService nodeSelectionService,
			ShadowTableExport export,
			int index){
		//Avoid creating detached job pod if there are no nodes for the export
		Supplier<Boolean> shouldRun = () -> shadowTableSettings.runExports.get()
				&& nodeSelectionService.hasNodesForExport(export);
		registerDetached(
				export.cronString(),
				shouldRun,
				ShadowTableJobId.toJobClass(index),
				true,
				export.detachedJobResource());
	}

	/*
	 * Concrete job implementations.
	 * This is because datarouter-job currently requires a separate class per trigger.
	 * TODO allow specifying a string name to disambiguate triggers with the same job class.
	 */

	public enum ShadowTableJobId{
		JOB_0(0, ShadowTableExportJob0.class),
		JOB_1(1, ShadowTableExportJob1.class),
		JOB_2(2, ShadowTableExportJob2.class),
		JOB_3(3, ShadowTableExportJob3.class),
		JOB_4(4, ShadowTableExportJob4.class),
		JOB_5(5, ShadowTableExportJob5.class),
		JOB_6(6, ShadowTableExportJob6.class),
		JOB_7(7, ShadowTableExportJob7.class),
		JOB_8(8, ShadowTableExportJob8.class),
		JOB_9(9, ShadowTableExportJob9.class);

		public final int index;
		public final Class<? extends BaseShadowTableExportJob> jobClass;

		public static final MappedEnum<ShadowTableJobId,Integer> BY_INDEX
				= new MappedEnum<>(values(), value -> value.index);
		public static final MappedEnum<ShadowTableJobId,Class<? extends BaseShadowTableExportJob>> BY_CLASS
				= new MappedEnum<>(values(), value -> value.jobClass);

		ShadowTableJobId(int index, Class<? extends BaseShadowTableExportJob> jobClass){
			this.index = index;
			this.jobClass = jobClass;
		}

		public static Class<? extends BaseShadowTableExportJob> toJobClass(int index){
			return BY_INDEX.fromOrThrow(index).jobClass;
		}

		public static int toIndex(BaseShadowTableExportJob job){
			return BY_CLASS.fromOrThrow(job.getClass()).index;
		}
	}

}
