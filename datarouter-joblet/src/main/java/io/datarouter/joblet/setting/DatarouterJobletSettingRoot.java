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
package io.datarouter.joblet.setting;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.enums.JobletQueueMechanism;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import io.datarouter.util.duration.DatarouterDuration;

@Singleton
public class DatarouterJobletSettingRoot extends SettingRoot{

	public final CachedSetting<Boolean> runJoblets;
	public final CachedSetting<Integer> maxJobletServers;
	public final CachedSetting<Integer> minJobletServers;
	public final CachedSetting<Integer> numServersToAddPerPeriod;
	public final CachedSetting<String> queueMechanism;
	public final CachedSetting<Boolean> runJobletCounterJob;
	public final CachedSetting<Boolean> runJobletRequeueJob;
	public final CachedSetting<Boolean> runJobletInstanceCounterJob;
	public final CachedSetting<Boolean> runJobletVacuum;
	public final CachedSetting<Boolean> runJobletDataVacuum;
	public final CachedSetting<DatarouterDuration> jobletTimeout;

	@Inject
	public DatarouterJobletSettingRoot(
			SettingFinder finder,
			DatarouterJobletThreadCountSettings jobletThreadCountSettings,
			DatarouterJobletClusterThreadCountSettings jobletClusterThreadCountSettings){
		super(finder, DatarouterSettingCategory.DATAROUTER, "datarouterJoblet.");

		registerChild(jobletThreadCountSettings);
		registerChild(jobletClusterThreadCountSettings);

		runJoblets = registerBoolean("runJoblets", false);
		maxJobletServers = registerInteger("maxJobletServers", 16);
		minJobletServers = registerInteger("minJobletServers", 8);
		numServersToAddPerPeriod = registerInteger("numServersToAddPerPeriod", 1);

		queueMechanism = registerString("queueMechanism", JobletQueueMechanism.JDBC_LOCK_FOR_UPDATE
				.getPersistentString());
		runJobletCounterJob = registerBoolean("runJobletCounterJob", false);
		runJobletRequeueJob = registerBoolean("runJobletRequeueJob", false);
		runJobletInstanceCounterJob = registerBoolean("runJobletInstanceCounterJob", false);
		runJobletVacuum = registerBooleans("runJobletVacuum", defaultTo(false));
		runJobletDataVacuum = registerBooleans("runJobletDataVacuum", defaultTo(false));
		jobletTimeout = registerDuration("jobletTimeout", new DatarouterDuration(10, TimeUnit.MINUTES));
	}

	public Integer getClusterThreadCountForJobletType(JobletType<?> jobletType){
		return Optional.ofNullable(getClusterThreadCountSettings().getCountForJobletType(jobletType))
				.orElse(0);
	}

	public Integer getThreadCountForJobletType(JobletType<?> jobletType){
		return Optional.ofNullable(getThreadCountSettings().getCountForJobletType(jobletType)).orElse(0);
	}


	public CachedSetting<Integer> getCachedSettingClusterThreadCountForJobletType(JobletType<?> jobletType){
		return Optional.ofNullable(getClusterThreadCountSettings().getSettingForJobletType(jobletType)).orElse(null);
	}

	public CachedSetting<Integer> getCachedSettingThreadCountForJobletType(JobletType<?> jobletType){
		return Optional.ofNullable(getThreadCountSettings().getSettingForJobletType(jobletType)).orElse(null);
	}

	/*------------------ node getters ------------------*/

	public DatarouterJobletClusterThreadCountSettings getClusterThreadCountSettings(){
		String name = getName() + DatarouterJobletClusterThreadCountSettings.NAME + ".";
		return Objects.requireNonNull((DatarouterJobletClusterThreadCountSettings)getChildren().get(name));
	}

	public DatarouterJobletThreadCountSettings getThreadCountSettings(){
		String name = getName() + DatarouterJobletThreadCountSettings.NAME + ".";
		return Objects.requireNonNull((DatarouterJobletThreadCountSettings)getChildren().get(name));
	}

}
