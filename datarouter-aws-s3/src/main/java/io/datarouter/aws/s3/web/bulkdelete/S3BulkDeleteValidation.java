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
package io.datarouter.aws.s3.web.bulkdelete;

import java.util.Optional;

import io.datarouter.aws.s3.DatarouterS3Client;
import io.datarouter.aws.s3.client.S3ClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.DatarouterClients;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.s3.model.Bucket;

@Singleton
public class S3BulkDeleteValidation{

	@Inject
	private DatarouterClients clients;
	@Inject
	private S3ClientManager s3ClientManager;

	/*--------- validate / find errors ----------*/

	public Optional<String> validateClientName(String clientName){
		if(!s3ClientExists(clientName)){
			return Optional.of("Client not found");
		}
		return Optional.empty();
	}

	public Optional<String> validateBucketName(Optional<String> optClientName, String bucketName){
		if(optClientName.isEmpty()){
			return Optional.empty();
		}
		if(!s3ClientExists(optClientName.orElseThrow())){
			return Optional.empty();
		}
		DatarouterS3Client s3Client = getS3Client(optClientName.orElseThrow());
		boolean bucketExists = s3Client.scanBuckets()
				.map(Bucket::name)
				.anyMatch(scannedBucketName -> scannedBucketName.equals(bucketName));
		if(!bucketExists){
			return Optional.of("Bucket not found");
		}
		return Optional.empty();
	}

	/*---------- private ----------*/

	// TODO move somewhere more generic?
	private DatarouterS3Client getS3Client(String clientName){
		ClientId clientId = clients.getClientId(clientName);
		return s3ClientManager.getClient(clientId);
	}

	private boolean s3ClientExists(String clientName){
		return clients.getClientId(clientName) != null && getS3Client(clientName) != null;
	}

}
