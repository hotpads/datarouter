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
package io.datarouter.secret.op;

import io.datarouter.enums.StringMappedEnum;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;

public class SecretOpReason{

	public final SecretOpReasonType type;
	public final String username;
	public final String userToken;
	public final String apiKey;
	public final String reason;

	public SecretOpReason(SecretOpReasonType type, String username, String userToken, String apiKey, String reason){
		this.type = type;
		this.username = username;
		this.userToken = userToken;
		this.apiKey = apiKey;
		this.reason = reason;
	}

	public static SecretOpReason automatedOp(String reason){
		Require.isTrue(StringTool.notEmptyNorWhitespace(reason));
		return new SecretOpReason(SecretOpReasonType.AUTOMATED, null, null, null, reason);
	}

	@Override
	public String toString(){
		switch(type){
		case API:
			return "Triggered by apiKey=" + apiKey + " for reason: " + reason;
		case AUTOMATED:
			return "Triggered automatically for reason: " + reason;
		case MANUAL:
			return "Triggered by username=" + username + " userToken=" + userToken + " for reason: " + reason;
		default:
			throw new RuntimeException("impossible");
		}
	}

	public static enum SecretOpReasonType{
		API("API"),
		AUTOMATED("AUTOMATED"),
		MANUAL("MANUAL");

		public static final StringMappedEnum<SecretOpReasonType> BY_PERSISTENT_STRING
				= new StringMappedEnum<>(values(), value -> value.persistentString);

		public final String persistentString;

		SecretOpReasonType(String persistentString){
			this.persistentString = persistentString;
		}

	}

}
