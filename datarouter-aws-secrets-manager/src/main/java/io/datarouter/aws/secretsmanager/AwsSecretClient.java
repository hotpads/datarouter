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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.exception.SecretExistsException;
import io.datarouter.secret.exception.SecretNotFoundException;
import io.datarouter.web.config.AwsSupport;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceExistsException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;

/**
 * Notes:
 *
 * clientRequestToken (usually UUID) is used for idempotency of secret manipulation AND becomes the versionID of
 * the secret after changes. not sure if it would be better to try to use that or use version stage for explicit
 * versioning tracking/manipulation. the latter is intended for rotation, rather than just explicit versioning. but the
 * form is a randomly generated UUID, so I don't know if it makes sense to manually use it instead.
 */
public class AwsSecretClient implements SecretClient{

	private final SecretsManagerClient client;

	public AwsSecretClient(AwsCredentialsProvider awsCredentialsProvider, String region, AwsSupport awsSupport){
		var httpClient = ApacheHttpClient.builder()
				.socketTimeout(Duration.ofSeconds(1))
				.connectionTimeout(Duration.ofSeconds(1))
				.build();
		client = SecretsManagerClient.builder()
				.httpClient(httpClient)
				.credentialsProvider(awsCredentialsProvider)
				.region(Region.of(region))
				.build();
		awsSupport.registerConnectionManagerFromHttpClient("secretManager", httpClient);
	}

	@Override
	public final void create(Secret secret){
		var request = CreateSecretRequest.builder()
				.name(secret.getName())
				.secretString(secret.getValue())
				.build();
		try{
			count("create");
			try(var _ = TracerTool.startSpan("AWSSecretsManager createSecret", TraceSpanGroupType.CLOUD_STORAGE)){
				TracerTool.appendToSpanInfo(secret.getName());
				client.createSecret(request);
			}
		}catch(ResourceExistsException e){
			throw new SecretExistsException(secret.getName(), e);
		}
	}

	@Override
	public final Secret read(String name){
		var request = GetSecretValueRequest.builder()
				.secretId(name)
				.build();
				// NOTES:
				// only specify one of the following (optional)
				// .withVersionId("")// manual version
				// .withVersionStage("")// related to AWS rotation
		try{
			GetSecretValueResponse response;
			count("get");
			try(var _ = TracerTool.startSpan("AWSSecretsManager getSecretValue", TraceSpanGroupType.CLOUD_STORAGE)){
				TracerTool.appendToSpanInfo(name);
				response = client.getSecretValue(request);
			}
			return new Secret(name, response.secretString());
		}catch(InvalidRequestException e){
			throw new RuntimeException("InvalidRequest secretName=" + name, e);
		}catch(ResourceNotFoundException e){
			throw new SecretNotFoundException(name, e);
		}
	}

	@Override
	public final List<String> listNames(Optional<String> prefix){
		List<String> secretNames = new ArrayList<>();
		String nextToken = null;
		do{
			var request = ListSecretsRequest.builder()
					.maxResults(100)
					.nextToken(nextToken)
					.build();
			ListSecretsResponse response;
			count("list");
			try(var _ = TracerTool.startSpan("AWSSecretsManager listSecrets", TraceSpanGroupType.CLOUD_STORAGE)){
				TracerTool.appendToSpanInfo(prefix.orElse(""));
				response = client.listSecrets(request);
				TracerTool.appendToSpanInfo("count", response.secretList().size());
			}
			nextToken = response.nextToken();
			response.secretList().stream()
					.map(SecretListEntry::name)
					.filter(name -> prefix.map(
							current -> current.length() < name.length() && name.startsWith(current))
									.orElse(true))
					.forEach(secretNames::add);
		}while(nextToken != null);
		return secretNames;
	}

	@Override
	public final void update(Secret secret){
		// this can update various stuff (like description and kms key) AND updates the version stage to AWSCURRENT.
		// for rotation, use PutSecretValue, which only updates the version stages and value of a secret explicitly
		var request = UpdateSecretRequest.builder()
				.secretId(secret.getName())
				.secretString(secret.getValue())
				.build();
		try{
			count("update");
			try(var _ = TracerTool.startSpan("AWSSecretsManager updateSecret", TraceSpanGroupType.CLOUD_STORAGE)){
				TracerTool.appendToSpanInfo(secret.getName());
				client.updateSecret(request);
			}
		}catch(ResourceExistsException e){
			throw new SecretExistsException("Requested update already exists.", secret.getName(), e);
		}catch(ResourceNotFoundException e){
			throw new SecretNotFoundException(secret.getName(), e);
		}
	}

	@Override
	public final void delete(String name){
		var request = DeleteSecretRequest.builder()
				.secretId(name)
				.build();
				// additional options:
				// .withForceDeleteWithoutRecovery(true)//might be useful at some point?
				// .withRecoveryWindowInDays(0L);//7-30 days to undelete. default 30
		try{
			count("delete");
			try(var _ = TracerTool.startSpan("AWSSecretsManager deleteSecret", TraceSpanGroupType.CLOUD_STORAGE)){
				TracerTool.appendToSpanInfo(name);
				client.deleteSecret(request);
			}
		}catch(ResourceNotFoundException e){
			throw new SecretNotFoundException(name, e);
		}
	}

	/**
	 * validates secret name according to the following rules, specified by {@link CreateSecretRequest}:
	 * The secret name must be ASCII letters, digits, or the following characters : /_+=.@-
	 * Don't end your secret name with a hyphen followed by six characters.
	 */
	@Override
	public final void validateName(String name){
		validateNameStatic(name);
	}

	public static void validateNameStatic(String name){
		if(name == null || name.isEmpty()){
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
				return character == '/'
						|| character == '_'
						|| character == '+'
						|| character == '='
						|| character == '.'
						|| character == '@'
						|| character == '-';
			});
		if(!allCharactersAllowed || name.length() > 6 && name.charAt(name.length() - 7) == '-'){
			throw new RuntimeException("validation failed name=" + name);
		}
	}

	private static void count(String key){
		Metrics.count("AwsSecretClient " + key);
	}

}
