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
package io.datarouter.exception;

import java.util.Date;
import java.util.TreeMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.exception.storage.httprecord.BaseHttpRequestRecord.FieldKeys;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.web.util.http.RecordedHttpHeaders;

public class TrimTests{

	@Test
	public void trimTest(){
		var path = new StringBuilder();
		for(int i = 0; i < FieldKeys.path.getSize(); i++){
			path.append(" ");
		}
		path.append(" ");
		HttpRequestRecord httpRequestRecord = new HttpRequestRecord(new Date(), "123", "", "exceptionRecordId",
				"httpMethod", "httpParams", "protocol", "hostname", 443, "contextPath", path.toString(), "queryString",
				null, "ip", "sessionRoles", "userToken", new RecordedHttpHeaders(new TreeMap<>()));
		httpRequestRecord.trimPath();
		Assert.assertEquals(httpRequestRecord.getPath().length(), FieldKeys.path.getSize());
	}

}
