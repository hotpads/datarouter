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
package io.datarouter.aws.secretsmanager.config;

import io.datarouter.aws.secretsmanager.AwsSecretClientCredentialsHolder;
import io.datarouter.aws.secretsmanager.AwsSecretClientCredentialsHolder.HardcodedAwsSecretClientCredentialsHolder;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterAwsSecretsManagerPlugin extends BaseWebPlugin{

	private final String devAccessKey;
	private final String devSecretKey;
	private final String stagingAccessKey;
	private final String stagingSecretKey;
	private final String prodAccessKey;
	private final String prodSecretkey;

	private DatarouterAwsSecretsManagerPlugin(String devAccessKey, String devSecretKey, String stagingAccessKey,
			String stagingSecretKey, String prodAccessKey, String prodSecretkey){
		this.devAccessKey = devAccessKey;
		this.devSecretKey = devSecretKey;
		this.stagingAccessKey = stagingAccessKey;
		this.stagingSecretKey = stagingSecretKey;
		this.prodAccessKey = prodAccessKey;
		this.prodSecretkey = prodSecretkey;
		addDatarouterGithubDocLink("datarouter-aws-secrets-manager");
	}

	@Override
	public String getName(){
		return "DatarouterAwsSecretsManager";
	}

	@Override
	protected void configure(){
		bind(AwsSecretClientCredentialsHolder.class).toInstance(new HardcodedAwsSecretClientCredentialsHolder(
				devAccessKey, devSecretKey, stagingAccessKey, stagingSecretKey, prodAccessKey, prodSecretkey));
	}

	public static class DatarouterAwsSecretsManagerPluginBuilder{

		private final String devAccessKey;
		private final String devSecretKey;
		private final String stagingAccessKey;
		private final String stagingSecretKey;
		private final String prodAccessKey;
		private final String prodSecretkey;

		public DatarouterAwsSecretsManagerPluginBuilder(String devAccessKey, String devSecretKey,
				String stagingAccessKey, String stagingSecretKey, String prodAccessKey, String prodSecretkey){
			this.devAccessKey = devAccessKey;
			this.devSecretKey = devSecretKey;
			this.stagingAccessKey = stagingAccessKey;
			this.stagingSecretKey = stagingSecretKey;
			this.prodAccessKey = prodAccessKey;
			this.prodSecretkey = prodSecretkey;
		}

		public DatarouterAwsSecretsManagerPlugin build(){
			return new DatarouterAwsSecretsManagerPlugin(
					devAccessKey,
					devSecretKey,
					stagingAccessKey,
					stagingSecretKey,
					prodAccessKey,
					prodSecretkey);
		}

	}

}
