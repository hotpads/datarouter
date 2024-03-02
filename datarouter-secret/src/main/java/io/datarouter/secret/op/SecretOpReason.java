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

public record SecretOpReason(
		SecretOpReasonType type,
		String username,
		String userToken,
		String apiKey,
		String reason){

	public static SecretOpReason automatedOp(String reason){
		Require.isTrue(StringTool.notEmptyNorWhitespace(reason));
		return new SecretOpReason(SecretOpReasonType.AUTOMATED, null, null, null, reason);
	}

	@Override
	public String toString(){
		return switch(type){
		case API -> String.format("Triggered by apiKey=%s for reason: %s", apiKey, reason);
		case AUTOMATED -> String.format("Triggered automatically for reason: %s", reason);
		case MANUAL -> String.format("Triggered by username=%s userToken=%s for reason: %s", username, userToken,
				reason);
		};
	}

	public enum SecretOpReasonType{
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
