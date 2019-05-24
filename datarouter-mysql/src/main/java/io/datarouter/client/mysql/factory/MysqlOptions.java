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

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;

@Singleton
public class MysqlOptions{

	@Inject
	private ClientOptions clientOptions;

	public String url(ClientId clientId){
		return clientOptions.getRequiredString(clientId.getName(), "url");
	}

	public String user(String clientName, String def){
		return clientOptions.getStringClientPropertyOrDefault("user", clientName, def);
	}

	public String password(String clientName, String def){
		return clientOptions.getStringClientPropertyOrDefault("password", clientName, def);
	}

	public Integer minPoolSize(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault("minPoolSize", clientName, def);
	}

	public Integer maxPoolSize(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault("maxPoolSize", clientName, def);
	}

	public Integer acquireIncrement(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault("acquireIncrement", clientName, def);
	}

	public Integer numHelperThreads(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault("numHelperThreads", clientName, def);
	}

	public Integer maxIdleTime(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault("maxIdleTime", clientName, def);
	}

	public Integer idleConnectionTestPeriod(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault("idleConnectionTestPeriod", clientName, def);
	}

	public Boolean logging(String clientName, Boolean def){
		return clientOptions.getBooleanClientPropertyOrDefault("logging", clientName, def);
	}

}