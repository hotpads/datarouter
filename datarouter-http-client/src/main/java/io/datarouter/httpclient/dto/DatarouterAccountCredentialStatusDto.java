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
package io.datarouter.httpclient.dto;

import java.time.Instant;

public class DatarouterAccountCredentialStatusDto{

	public final String accountName;
	public final String credentialIdentifier;
	public final Instant credentialCreated;
	public final Boolean shouldRotate;
	//future features
	public final Instant rotationDeadline;
	public final String rotationUrl;

	public DatarouterAccountCredentialStatusDto(String accountName, String credentialIdentifier,
			Instant credentialCreated, Boolean shouldRotate, Instant rotationDeadline, String rotationUrl){
		this.accountName = accountName;
		this.credentialIdentifier = credentialIdentifier;
		this.credentialCreated = credentialCreated;
		this.shouldRotate = shouldRotate;
		this.rotationDeadline = rotationDeadline;
		this.rotationUrl = rotationUrl;
	}

}
