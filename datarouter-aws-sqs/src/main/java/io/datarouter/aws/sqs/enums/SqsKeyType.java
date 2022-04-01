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
package io.datarouter.aws.sqs.enums;

import io.datarouter.enums.StringEnum;

public enum SqsKeyType implements StringEnum<SqsKeyType>{
	FILE("file");

	private final String persistentString;

	SqsKeyType(String persistentString){
		this.persistentString = persistentString;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public SqsKeyType fromPersistentString(String string){
		return fromPersistentStringStatic(string);
	}

	public static SqsKeyType fromPersistentStringStatic(String string){
		return StringEnum.getEnumFromString(values(), string, null);
	}

}
