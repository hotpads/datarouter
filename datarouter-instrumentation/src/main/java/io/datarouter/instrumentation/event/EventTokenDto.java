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
package io.datarouter.instrumentation.event;

public class EventTokenDto{

	public final String path; // a category of fields in /path/format (ex: /photo)
	public final String field; // an item to report on, like a user (ex: 1234)
	public final String term; // a column in the report, like a photo view (ex: photoView)
	public final Boolean indexed; //true: include in reporting; false: store for potential future usage

	public EventTokenDto(String path, String field, String term, Boolean indexed){
		this.path = path;
		this.field = field;
		this.term = term;
		this.indexed = indexed;
	}

	@Override
	public String toString(){
		return String.join(":", field, term, indexed.toString());
	}

}
