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
package io.datarouter.httpclient.json;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.internal.PreJava9DateFormatProvider;

import io.datarouter.httpclient.Java9;

public class DateTypeAdapterTests{

	@Test
	public void testJava9DateSerialization() throws ParseException{
		String java8Date = "Feb 13, 2019 10:40:25 PM";
		DateFormat java8Format = PreJava9DateFormatProvider.getUSDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT);
		Date date = java8Format.parse(java8Date);
		Assert.assertEquals(HttpClientGsonTool.GSON.fromJson('"' + java8Date + '"', Date.class), date);
		if(Java9.IS_JAVA_9){
			String java9Date = "Feb 13, 2019, 10:40:25 PM";
			Assert.assertEquals(HttpClientGsonTool.GSON.fromJson('"' + java9Date + '"', Date.class), date);
		}
	}

}
