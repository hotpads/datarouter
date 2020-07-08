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
package io.datarouter.aws.secretsmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.ResourceExistsException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;

import io.datarouter.secret.client.BaseSecretClient;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.service.SecretStageDetector;

/**
 * Notes:
 *
 * clientRequestToken (usually UUID) is used for idempotency of secret manipulation AND becomes the versionID of
 * the secret after changes. not sure if it would be better to try to use that or use version stage for explicit
 * versioning tracking/manipulation. the latter is intended for rotation, rather than just explicit versioning. but the
 * form is a randomly generated UUID, so I don't know if it makes sense to manually use it instead.
 */
@Singleton
public class AwsSecretClient extends BaseSecretClient{

	private static final String US_EAST_1 = "us-east-1";

	@Inject
	private SecretStageDetector secretStageDetector;
	@Inject
	private AwsCredentialsSupplier awsCredentialsSupplier;

	private AWSSecretsManager client;
	private boolean isInited;

	private void init(){
		if(isInited){
			return;
		}
		AWSCredentials awsCredentials = secretStageDetector.mightBeProduction()
				? new BasicAWSCredentials(
						awsCredentialsSupplier.getProdAccessKey(),
						awsCredentialsSupplier.getProdSecretKey())
				: new BasicAWSCredentials(
						awsCredentialsSupplier.getDevAccessKey(),
						awsCredentialsSupplier.getDevSecretKey());

		client = AWSSecretsManagerClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.withRegion(US_EAST_1)
				.build();
		isInited = true;
	}

	@Override
	protected void createInternal(Secret secret){
		init();
		var request = new CreateSecretRequest()
				.withName(secret.getName())
				.withSecretString(secret.getValue());
		try{
			client.createSecret(request);
		}catch(ResourceExistsException e){
			throw new RuntimeException("secret already exists for create name=" + secret.getName(), e);
		}
	}

	@Override
	protected Secret readInternal(String name){
		init();
		var request = new GetSecretValueRequest()
				.withSecretId(name);
				// NOTES:
				// only specify one of the following (optional)
				// .withVersionId("")// manual version
				// .withVersionStage("")// related to AWS rotation
		try{
			GetSecretValueResult result = client.getSecretValue(request);
			return new Secret(name, result.getSecretString());
		}catch(ResourceNotFoundException e){
			throw new RuntimeException("failed to find secret for get name=" + name, e);
		}
	}

	@Override
	protected List<String> listInternal(Optional<String> exclusivePrefix){
		init();
		List<String> secretNames = new ArrayList<>();
		String nextToken = null;
		do{
			var request = new ListSecretsRequest().withNextToken(nextToken);
			ListSecretsResult result = client.listSecrets(request);
			nextToken = result.getNextToken();
			result.getSecretList().stream()
					.map(SecretListEntry::getName)
					.filter(name -> {
						return exclusivePrefix.map(prefix -> prefix.length() < name.length() && name.startsWith(prefix))
								.orElse(true);
					})
					.forEach(secretNames::add);
		}while(nextToken != null);
		return secretNames;
	}

	@Override
	protected void updateInternal(Secret secret){
		init();
		// this can update various stuff (like description and kms key) AND updates the version stage to AWSCURRENT.
		// for rotation, use PutSecretValue, which only updates the version stages and value of a secret explicitly
		var request = new UpdateSecretRequest()
				.withSecretId(secret.getName())
				.withSecretString(secret.getValue());
		try{
			client.updateSecret(request);
		}catch(ResourceExistsException e){
			throw new RuntimeException("duplicate inconsistent update name=" + secret.getName(), e);
		}catch(ResourceNotFoundException e){
			throw new RuntimeException("failed to find secret for update name=" + secret.getName(), e);
		}
	}

	@Override
	protected void deleteInternal(String name){
		init();
		var request = new DeleteSecretRequest()
				.withSecretId(name);
				// additional options:
				// .withForceDeleteWithoutRecovery(true)//might be useful at some point?
				// .withRecoveryWindowInDays(0L);//7-30 days to undelete. default 30
		try{
			client.deleteSecret(request);
		}catch(ResourceNotFoundException e){
			throw new RuntimeException("failed to find secret for delete name=" + name, e);
		}
	}

	/**
	 * validates secret name according to the following rules, specified by {@link CreateSecretRequest}:
	 * The secret name must be ASCII letters, digits, or the following characters : /_+=.@-
	 * Don't end your secret name with a hyphen followed by six characters.
	 */
	@Override
	protected void validateNameInternal(String name){
		validateNameStatic(name);
	}

	protected static void validateNameStatic(String name){
		if(name == null || name.length() == 0){
			throw new RuntimeException("validation failed name=" + name);
		}
		boolean allCharactersAllowed = name.toLowerCase().chars()
			.allMatch(character -> {
				//numbers
				if(character > 47 && character < 58){
					return true;
				}
				//lower case letters
				if(character > 96 && character < 123){
					return true;
				}
				if(character == '/'
						|| character == '_'
						|| character == '+'
						|| character == '='
						|| character == '.'
						|| character == '@'
						|| character == '-'){
					return true;
				}
				return false;
			});
		if(!allCharactersAllowed || (name.length() > 6 && name.charAt(name.length() - 7) == '-')){
			throw new RuntimeException("validation failed name=" + name);
		}
	}

}
