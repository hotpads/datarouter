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
package io.datarouter.exception.storage.exceptionrecord;

import io.datarouter.util.number.RandomTool;

public class ExceptionRecordKey extends BaseExceptionRecordKey<ExceptionRecordKey>{

	private static final long PADDING = String.valueOf(Long.MAX_VALUE).length();
	private static final String ID_FORMAT = "%d%0" + PADDING + "d";

	public ExceptionRecordKey(){
	}

	public ExceptionRecordKey(String id){
		super(id);
	}

	public static ExceptionRecordKey generate(){
		String id = String.format(ID_FORMAT, System.currentTimeMillis(), RandomTool.nextPositiveLong());
		return new ExceptionRecordKey(id);
	}

}
