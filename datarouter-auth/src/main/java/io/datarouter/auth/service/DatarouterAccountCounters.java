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
package io.datarouter.auth.service;

import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.storage.util.DatarouterCounters;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountCounters{

	public static final String ACCOUNT = "account";
	public static final String NAME = "name";
	private static final String ENDPOINT = "endpoint";

	private final String prefix;

	public DatarouterAccountCounters(){
		this(ACCOUNT);
	}

	public DatarouterAccountCounters(String prefix){
		this.prefix = prefix;
	}

	public void incPermissionUsage(DatarouterAccountPermissionKey permission){
		incInternal(NAME, permission.getAccountName());
		incInternal(ENDPOINT, permission.getEndpoint(), permission.getAccountName());
	}

	private void incInternal(String format, String... suffixes){
		Counters.inc(DatarouterCounters.PREFIX + " " + prefix + " " + format + " " + String.join(" ", suffixes));
	}

}
