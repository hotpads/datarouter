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
package io.datarouter.aws.sqs;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.util.string.StringTool;

@Singleton
public class SqsQueueNameService{
	private static final Logger logger = LoggerFactory.getLogger(SqsQueueNameService.class);

	public static final int MIN_QUEUE_NAME_LENGTH = 1;
	public static final int MAX_QUEUE_NAME_LENGTH = 80;

	@Inject
	private EnvironmentName environmentName;
	@Inject
	private ServiceName serviceName;

	public String buildDefaultNamespace(){
		return buildDefaultNamespaceForEnvironment(environmentName.get());
	}

	public String buildDefaultNamespaceForEnvironment(String environment){
		return environment + "-" + serviceName.get();
	}

	public String buildDefaultQueueName(String tableName){
		return buildDefaultQueueNameForEnvironment(environmentName.get(), tableName);
	}

	public String buildDefaultQueueNameForEnvironment(String environment, String tableName){
		String namespace = buildDefaultNamespaceForEnvironment(environment);
		String queueName = StringTool.isEmpty(namespace) ? tableName : (namespace + "-" + tableName);
		if(queueName.length() > MAX_QUEUE_NAME_LENGTH){
			// Future change to a throw.
			logger.error("queue={} overflows the max size {}", queueName, MAX_QUEUE_NAME_LENGTH);
		}
		return queueName;
	}

	public String buildQueueName(String queueUrl, String tableName){
		if(queueUrl != null){
			return queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
		}
		return buildDefaultQueueName(tableName);
	}

}
