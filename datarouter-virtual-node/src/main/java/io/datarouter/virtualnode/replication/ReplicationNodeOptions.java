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
package io.datarouter.virtualnode.replication;

import java.util.List;
import java.util.Optional;

import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;
import io.datarouter.storage.privacy.DatarouterPrivacyExemptionReason;
import io.datarouter.storage.privacy.DatarouterPrivacyProcessor;

public class ReplicationNodeOptions{

	public final Optional<String> tableName;
	public final Optional<Integer> everyNToPrimary;
	public final Optional<Boolean> disableForcePrimary;
	public final Optional<Boolean> disableIntroducer;
	public final Optional<NodewatchConfigurationBuilder> nodewatchConfigurationBuilder;
	public final List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors;
	public final Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason;


	private ReplicationNodeOptions(
			Optional<String> tableName,
			Optional<Integer> everyNToPrimary,
			Optional<Boolean> disableForcePrimary,
			Optional<Boolean> disableIntroducer,
			Optional<NodewatchConfigurationBuilder> nodewatchConfigurationBuilder,
			List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors,
			Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason){
		this.tableName = tableName;
		this.everyNToPrimary = everyNToPrimary;
		this.disableForcePrimary = disableForcePrimary;
		this.disableIntroducer = disableIntroducer;
		this.nodewatchConfigurationBuilder = nodewatchConfigurationBuilder;
		this.privacyProcessors = privacyProcessors;
		this.privacyExemptionReason = privacyExemptionReason;
	}

	public static class ReplicationNodeOptionsBuilder{

		private Optional<String> tableName = Optional.empty();
		private Optional<Integer> everyNToPrimary = Optional.empty();
		private Optional<Boolean> disableForcePrimary = Optional.empty();
		private Optional<Boolean> disableIntroducer = Optional.empty();
		private Optional<NodewatchConfigurationBuilder> nodewatchConfigurationBuilder = Optional.empty();
		private List<Class<? extends DatarouterPrivacyProcessor>> privacyProcessors = List.of();
		private Optional<DatarouterPrivacyExemptionReason> privacyExemptionReason = Optional.empty();

		public ReplicationNodeOptionsBuilder withTableName(String tableName){
			this.tableName = Optional.of(tableName);
			return this;
		}

		public ReplicationNodeOptionsBuilder withEveryNToPrimary(Integer everyNToPrimary){
			this.everyNToPrimary = Optional.of(everyNToPrimary);
			return this;
		}

		public ReplicationNodeOptionsBuilder withDisableForcePrimary(boolean disableForcePrimary){
			this.disableForcePrimary = Optional.of(disableForcePrimary);
			return this;
		}

		public ReplicationNodeOptionsBuilder withDisableIntroducer(boolean disableIntroducer){
			this.disableIntroducer = Optional.of(disableIntroducer);
			return this;
		}

		public ReplicationNodeOptionsBuilder withNodewatchConfigurationBuilder(
				NodewatchConfigurationBuilder nodewatchConfigurationBuilder){
			this.nodewatchConfigurationBuilder = Optional.of(nodewatchConfigurationBuilder);
			return this;
		}

		@SafeVarargs
		public final ReplicationNodeOptionsBuilder withPrivacyProcessors(
				Class<? extends DatarouterPrivacyProcessor>... privacyProcessors){
			this.privacyProcessors = List.of(privacyProcessors);
			return this;
		}

		public ReplicationNodeOptionsBuilder withPrivacyExemptionReason(
				DatarouterPrivacyExemptionReason privacyExemptionReason){
			this.privacyExemptionReason = Optional.of(privacyExemptionReason);
			return this;
		}

		public ReplicationNodeOptions build(){
			return new ReplicationNodeOptions(
					tableName,
					everyNToPrimary,
					disableForcePrimary,
					disableIntroducer,
					nodewatchConfigurationBuilder,
					privacyProcessors,
					privacyExemptionReason);
		}

	}

}
