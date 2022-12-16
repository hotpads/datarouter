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
package io.datarouter.aws.s3;

import java.io.Serializable;

import io.datarouter.aws.s3.SerializableStaticAwsCredentialsProviderProvider.S3CredentialsDto;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@SuppressWarnings("serial")
public class SerializableStaticAwsCredentialsProviderProvider
extends SerializableAwsCredentialsProviderProvider<S3CredentialsDto>{

	public SerializableStaticAwsCredentialsProviderProvider(S3CredentialsDto params){
		super(params);
	}

	@Override
	public AwsCredentialsProvider get(){
		return StaticCredentialsProvider.create(AwsBasicCredentials.create(params.accessKey, params.secretKey));
	}

	public record S3CredentialsDto(
			String accessKey,
			String secretKey)
	implements Serializable{
	}

}
