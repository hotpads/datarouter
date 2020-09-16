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
package io.datarouter.client.hbase.config;

import io.datarouter.client.hbase.balancer.DefaultHBaseBalancerFactory;
import io.datarouter.client.hbase.balancer.HBaseBalancerFactory;
import io.datarouter.client.hbase.compaction.DefaultHBaseCompactionInfo;
import io.datarouter.client.hbase.compaction.HBaseCompactionInfo;
import io.datarouter.job.config.BaseJobPlugin;

public class DatarouterHbasePlugin extends BaseJobPlugin{

	private final Class<? extends HBaseCompactionInfo> hbaseCompactionInfoClass;
	private final Class<? extends HBaseBalancerFactory> hbaseBalancerFactoryClass;

	private DatarouterHbasePlugin(
			Class<? extends HBaseCompactionInfo> hbaseCompactionInfoClass,
			Class<? extends HBaseBalancerFactory> hbaseBalancerFactoryClass){
		this.hbaseCompactionInfoClass = hbaseCompactionInfoClass;
		this.hbaseBalancerFactoryClass = hbaseBalancerFactoryClass;
		addRouteSet(DatarouterHBaseRouteSet.class);
		addSettingRoot(DatarouterHBaseSettingRoot.class);
		addTriggerGroup(DatarouterHBaseTriggerGroup.class);
		addDatarouterGithubDocLink("datarouter-hbase");
	}

	@Override
	public String getName(){
		return "DatarouterHbase";
	}

	@Override
	protected void configure(){
		bind(HBaseCompactionInfo.class).to(hbaseCompactionInfoClass);
		bind(HBaseBalancerFactory.class).to(hbaseBalancerFactoryClass);
	}

	public static class DatarouterHbasePluginBuilder{

		private Class<? extends HBaseCompactionInfo> hbaseCompactionInfoClass = DefaultHBaseCompactionInfo.class;
		private Class<? extends HBaseBalancerFactory> hbaseBalancerFactoryClass = DefaultHBaseBalancerFactory.class;

		public DatarouterHbasePluginBuilder setHbaseCompactionInfoClass(
				Class<? extends HBaseCompactionInfo> hbaseCompactionInfoClass){
			this.hbaseCompactionInfoClass = hbaseCompactionInfoClass;
			return this;
		}

		public DatarouterHbasePluginBuilder setHBaseBalancerFactoryClass(
				Class<? extends HBaseBalancerFactory> hbaseBalancerFactoryClass){
			this.hbaseBalancerFactoryClass = hbaseBalancerFactoryClass;
			return this;
		}

		public DatarouterHbasePlugin build(){
			return new DatarouterHbasePlugin(hbaseCompactionInfoClass, hbaseBalancerFactoryClass);
		}

	}

}
