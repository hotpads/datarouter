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
package io.datarouter.client.mysql.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.util.properties.TypedProperties;

@Singleton
public class MysqlOptionsFactory{

	@Inject
	private DatarouterClients datarouterClients;

	public class MysqlOptions extends TypedProperties{

		private final String clientPrefix;

		public MysqlOptions(String clientName){
			super(datarouterClients.getMultiProperties());
			this.clientPrefix = "client." + clientName + ".";
		}

		public String url(){
			return getRequiredString(clientPrefix + "url");
		}

		public String user(String def){
			return getString(clientPrefix + "user", def);
		}

		public String password(String def){
			return getString(clientPrefix + "password", def);
		}

		public Integer minPoolSize(Integer def){
			return getInteger(clientPrefix + "minPoolSize", def);
		}

		public Integer maxPoolSize(Integer def){
			return getInteger(clientPrefix + "maxPoolSize", def);
		}

		public Integer acquireIncrement(Integer def){
			return getInteger(clientPrefix + "acquireIncrement", def);
		}

		public Integer numHelperThreads(Integer def){
			return getInteger(clientPrefix + "numHelperThreads", def);
		}

		public Integer maxIdleTime(Integer def){
			return getInteger(clientPrefix + "maxIdleTime", def);
		}

		public Integer idleConnectionTestPeriod(Integer def){
			return getInteger(clientPrefix + "idleConnectionTestPeriod", def);
		}

		public Boolean logging(Boolean def){
			return getBoolean(clientPrefix + "logging", def);
		}
	}

}