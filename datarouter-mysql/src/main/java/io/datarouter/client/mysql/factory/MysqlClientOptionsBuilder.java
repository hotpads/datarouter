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

import java.util.Properties;

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientInitMode;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class MysqlClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientIdName;
	private final Properties properties;

	public MysqlClientOptionsBuilder(ClientId clientId){
		clientIdName = clientId.getName();
		properties = new Properties();
		setClientTypeOption(clientId);
	}

	private void setClientTypeOption(ClientId clientId){
		if(!clientId.equals(ClientOptionsBuilder.DEFAULT_CLIENT_ID)){
			properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), MysqlClientType.NAME);
		}
	}

	public MysqlClientOptionsBuilder withUrl(String urlValue){
		String optionKey = makeKey(MysqlOptions.PROP_url);
		properties.setProperty(optionKey, urlValue);
		return this;
	}

	public MysqlClientOptionsBuilder withUser(String userValue){
		String optionKey = makeKey(MysqlOptions.PROP_user);
		properties.setProperty(optionKey, userValue);
		return this;
	}

	public MysqlClientOptionsBuilder withPasswordLocation(String passwordLocationValue){
		String optionKey = makeKey(MysqlOptions.PROP_passwordLocation);
		properties.setProperty(optionKey, passwordLocationValue);
		return this;
	}

	public MysqlClientOptionsBuilder withPassword(String passwordValue){
		String optionKey = makeKey(MysqlOptions.PROP_password);
		properties.setProperty(optionKey, passwordValue);
		return this;
	}

	public MysqlClientOptionsBuilder withMinPoolSize(int minPoolSizeValue){
		String optionKey = makeKey(MysqlOptions.PROP_minPoolSize);
		properties.setProperty(optionKey, String.valueOf(minPoolSizeValue));
		return this;
	}

	public MysqlClientOptionsBuilder withMaxPoolSize(int maxPoolSizeValue){
		String optionKey = makeKey(MysqlOptions.PROP_maxPoolSize);
		properties.setProperty(optionKey, String.valueOf(maxPoolSizeValue));
		return this;
	}

	public MysqlClientOptionsBuilder withAcquireIncrement(int acquireIncrementValue){
		String optionKey = makeKey(MysqlOptions.PROP_acquireIncrement);
		properties.setProperty(optionKey, String.valueOf(acquireIncrementValue));
		return this;
	}

	public MysqlClientOptionsBuilder withNumHelperThreads(int numHelperThreadsValue){
		String optionKey = makeKey(MysqlOptions.PROP_numHelperThreads);
		properties.setProperty(optionKey, String.valueOf(numHelperThreadsValue));
		return this;
	}

	public MysqlClientOptionsBuilder withMaxIdleTimeSeconds(int maxIdleTimeValue){
		String optionKey = makeKey(MysqlOptions.PROP_maxIdleTime);
		properties.setProperty(optionKey, String.valueOf(maxIdleTimeValue));
		return this;
	}

	public MysqlClientOptionsBuilder withIdleConnectionTestPeriod(int idleConnectionTestPeriodValue){
		String optionKey = makeKey(MysqlOptions.PROP_idleConnectionTestPeriod);
		properties.setProperty(optionKey, String.valueOf(idleConnectionTestPeriodValue));
		return this;
	}

	public MysqlClientOptionsBuilder enableLogging(){
		String optionKey = makeKey(MysqlOptions.PROP_logging);
		properties.setProperty(optionKey, Boolean.TRUE.toString());
		return this;
	}

	public MysqlClientOptionsBuilder readOnly(){
		String optionKey = makeKey(MysqlOptions.PROP_readOnly);
		properties.setProperty(optionKey, Boolean.TRUE.toString());
		return this;
	}

	public MysqlClientOptionsBuilder withInitMode(ClientInitMode initMode){
		String optionKey = ClientOptions.makeClientInitModeKey(clientIdName);
		properties.setProperty(optionKey, initMode.name());
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

	private String makeKey(String suffix){
		return ClientOptions.makeClientPrefixedKey(clientIdName, suffix);
	}

}
