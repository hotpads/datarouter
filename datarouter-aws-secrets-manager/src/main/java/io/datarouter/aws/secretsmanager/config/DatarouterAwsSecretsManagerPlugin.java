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

import java.util.Optional;

import io.datarouter.aws.secretsmanager.AwsSecretClientCredentialsHolder;
import io.datarouter.aws.secretsmanager.AwsSecretClientCredentialsHolder.DefaultAwsSecretClientCredentialsHolder;
import io.datarouter.aws.secretsmanager.AwsSecretClientCredentialsHolder.HardcodedAwsSecretClientCredentialsHolder;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterAwsSecretsManagerPlugin extends BaseWebPlugin{

	private final Optional<HardcodedAwsSecretClientCredentialsHolder> hardcodedCredentialsHolder;

	private DatarouterAwsSecretsManagerPlugin(
			Optional<HardcodedAwsSecretClientCredentialsHolder> hardcodedCredentialsHolder){
		this.hardcodedCredentialsHolder = hardcodedCredentialsHolder;
		addDatarouterGithubDocLink("datarouter-aws-secrets-manager");
	}

	@Override
	protected void configure(){
		requireBinding(ServerTypeDetector.class);
		if(hardcodedCredentialsHolder.isPresent()){
			bind(AwsSecretClientCredentialsHolder.class).toInstance(hardcodedCredentialsHolder.get());
		}else{
			bind(AwsSecretClientCredentialsHolder.class).to(DefaultAwsSecretClientCredentialsHolder.class);
		}
	}

	public static class DatarouterAwsSecretsManagerPluginBuilder{

		private Optional<HardcodedAwsSecretClientCredentialsHolder> hardcodedCredentialsHolder;

		public DatarouterAwsSecretsManagerPluginBuilder(){
			this.hardcodedCredentialsHolder = Optional.empty();
		}

		public DatarouterAwsSecretsManagerPluginBuilder setHardcodedCredentials(String devAccessKey,
				String devSecretKey, String stagingAccessKey, String stagingSecretKey, String prodAccessKey,
				String prodSecretkey){
			this.hardcodedCredentialsHolder = Optional.of(new HardcodedAwsSecretClientCredentialsHolder(
					devAccessKey,
					devSecretKey,
					stagingAccessKey,
					stagingSecretKey,
					prodAccessKey,
					prodSecretkey));
			return this;
		}

		public DatarouterAwsSecretsManagerPlugin build(){
			return new DatarouterAwsSecretsManagerPlugin(hardcodedCredentialsHolder);
		}

	}

}
