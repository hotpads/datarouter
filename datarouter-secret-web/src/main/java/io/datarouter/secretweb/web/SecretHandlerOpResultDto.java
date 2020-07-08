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
package io.datarouter.secretweb.web;

import java.util.List;

public class SecretHandlerOpResultDto{

	public final SecretOpStatus opStatus;
	public final String message;
	public final String value;//set for read
	public final List<String> appSecretNames;//set for list
	public final List<String> sharedSecretNames;//set for list

	private SecretHandlerOpResultDto(SecretOpStatus opStatus, String message, String value, List<String> appSecretNames,
			List<String> sharedSecretNames){
		this.opStatus = opStatus;
		this.message = message;
		this.value = value;
		this.appSecretNames = appSecretNames;
		this.sharedSecretNames = sharedSecretNames;
	}

	public static SecretHandlerOpResultDto denied(String message){
		return new SecretHandlerOpResultDto(SecretOpStatus.DENIED, message, null, null, null);
	}

	public static SecretHandlerOpResultDto error(String message){
		return new SecretHandlerOpResultDto(SecretOpStatus.ERROR, message, null, null, null);
	}

	public static SecretHandlerOpResultDto success(){
		return new SecretHandlerOpResultDto(SecretOpStatus.SUCCESS, null, null, null, null);
	}

	public static SecretHandlerOpResultDto read(String secretValue){
		return new SecretHandlerOpResultDto(SecretOpStatus.SUCCESS, null, secretValue, null, null);
	}

	public static SecretHandlerOpResultDto list(List<String> appSecretNames, List<String> sharedSecretNames){
		return new SecretHandlerOpResultDto(SecretOpStatus.SUCCESS, null, null, appSecretNames, sharedSecretNames);
	}

	public static enum SecretOpStatus{

		SUCCESS,
		ERROR,
		DENIED,
		;

	}

}
