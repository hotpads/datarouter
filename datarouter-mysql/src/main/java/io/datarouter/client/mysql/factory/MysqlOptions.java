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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretService;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.string.StringTool;

@Singleton
public class MysqlOptions{
	private static final Logger logger = LoggerFactory.getLogger(MysqlOptions.class);

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private SecretService secretService;

	private final ConcurrentHashMap<String,String> clientPasswords = new ConcurrentHashMap<>();

	protected static final String PROP_url = "url";
	protected static final String PROP_urlParam = "urlParam";
	protected static final String PROP_user = "user";
	protected static final String PROP_passwordLocation = "password.location";
	protected static final String PROP_password = "password";
	protected static final String PROP_minPoolSize = "minPoolSize";
	protected static final String PROP_maxPoolSize = "maxPoolSize";
	protected static final String PROP_acquireIncrement = "acquireIncrement";
	protected static final String PROP_numHelperThreads = "numHelperThreads";
	protected static final String PROP_maxIdleTime = "maxIdleTime";
	protected static final String PROP_idleConnectionTestPeriod = "idleConnectionTestPeriod";
	protected static final String PROP_logging = "logging";
	protected static final String PROP_readOnly = "readOnly";

	public String url(ClientId clientId){
		return clientOptions.getRequiredString(clientId.getName(), PROP_url);
	}

	public String hostname(ClientId clientid){
		return StringTool.getStringBeforeLastOccurrence(':', url(clientid));
	}

	/**
	 * Indexes start from zero and cannot have gaps
	 */
	public List<String> urlParams(String clientName){
		List<String> urlParams = new ArrayList<>();
		for(int index = 0; ; ++index){
			String propertySuffix = PROP_urlParam + "." + index;
			Optional<String> optParam = clientOptions.optString(clientName, propertySuffix);
			optParam.ifPresent(urlParams::add);
			if(optParam.isEmpty()){
				break;
			}
		}
		return urlParams;
	}

	public String user(String clientName, String def){
		return clientOptions.getStringClientPropertyOrDefault(PROP_user, clientName, def);
	}

	public String password(String clientName, String def){
		return clientPasswords.computeIfAbsent(clientName, $ -> {
			Optional<String> optionalSecretLocation = Optional.ofNullable(clientOptions
					.getStringClientPropertyOrDefault(PROP_passwordLocation, clientName, null));
			return optionalSecretLocation.map(secretLocation -> {
				String namespacedLocationForLogs = secretNamespacer.sharedNamespaced(secretLocation);
				try{
					String result = secretService.readSharedWithoutRecord(secretLocation, String.class);
					logger.warn("using secret at secretLocation={}", namespacedLocationForLogs);
					return result;
				}catch(RuntimeException e){
					logger.error("Failed to locate secretLocation={} for clientName={}", namespacedLocationForLogs,
							clientName, e);
					return (String)null;
				}
			}).orElseGet(() -> clientOptions.getStringClientPropertyOrDefault(PROP_password, clientName, def));
		});
	}

	public Integer minPoolSize(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault(PROP_minPoolSize, clientName, def);
	}

	public Integer maxPoolSize(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault(PROP_maxPoolSize, clientName, def);
	}

	public Integer acquireIncrement(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault(PROP_acquireIncrement, clientName, def);
	}

	public Integer numHelperThreads(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault(PROP_numHelperThreads, clientName, def);
	}

	public Integer maxIdleTime(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault(PROP_maxIdleTime, clientName, def);
	}

	public Integer idleConnectionTestPeriod(String clientName, Integer def){
		return clientOptions.getIntegerClientPropertyOrDefault(PROP_idleConnectionTestPeriod, clientName, def);
	}

	public Boolean logging(String clientName, Boolean def){
		return clientOptions.getBooleanClientPropertyOrDefault(PROP_logging, clientName, def);
	}

}