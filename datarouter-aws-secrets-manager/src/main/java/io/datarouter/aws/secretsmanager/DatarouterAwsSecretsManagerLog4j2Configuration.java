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
package io.datarouter.aws.secretsmanager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;

import io.datarouter.logging.BaseLog4j2Configuration;
import io.datarouter.logging.DatarouterLog4j2Configuration;

public class DatarouterAwsSecretsManagerLog4j2Configuration extends BaseLog4j2Configuration{

	public DatarouterAwsSecretsManagerLog4j2Configuration(){
		registerParent(DatarouterLog4j2Configuration.class);

		Appender console = getAppender(DatarouterLog4j2Configuration.CONSOLE_APPENDER_NAME);
		addLoggerConfig("com.amazonaws.auth.profile.internal.BasicProfileConfigLoader", Level.ERROR, false, console);
	}

}
