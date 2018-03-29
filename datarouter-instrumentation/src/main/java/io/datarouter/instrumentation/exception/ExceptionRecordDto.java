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
package io.datarouter.instrumentation.exception;

import java.util.Date;

public class ExceptionRecordDto{

	public final String id;
	public final Date created;
	public final String serverName;
	public final String stackTrace;
	public final String type;
	public final String appVersion;
	public final String exceptionLocation;

	public ExceptionRecordDto(String id, Date created, String serverName, String stackTrace, String type,
			String appVersion, String exceptionLocation){
		this.id = id;
		this.created = created;
		this.serverName = serverName;
		this.stackTrace = stackTrace;
		this.type = type;
		this.appVersion = appVersion;
		this.exceptionLocation = exceptionLocation;
	}

}