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

public class SecretHandlerOpRequestDto{

	public final SecretOpDto op;
	public final String name;//also used for prefix in list op
	public final String value;
	public final String secretClass;

	public SecretHandlerOpRequestDto(SecretOpDto op, String name, String value, String secretClass){
		this.op = op;
		this.name = name;
		this.value = value;
		this.secretClass = secretClass;
	}

	public static enum SecretOpDto{

		CREATE("create"),
		READ("read"),
		READ_SHARED("readShared"),
		UPDATE("update"),
		DELETE("delete"),
		LIST_ALL("listAll"),
		;

		private final String persistentString;

		SecretOpDto(String persistentString){
			this.persistentString = persistentString;
		}

		public String getPersistentString(){
			return persistentString;
		}

	}

}
