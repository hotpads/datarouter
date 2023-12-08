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

import com.google.gson.Gson;
import com.google.gson.internal.PreJava9DateFormatProvider;

import io.datarouter.gson.GsonTool;
import io.datarouter.gson.typeadapter.Java8DateTypeAdapter;

@SuppressWarnings("deprecation")
public class DateTypeAdapterTests{

	@Test
	public void testJava9DateSerialization() throws ParseException{
		String java8Date = "Feb 13, 2019 10:40:25 PM";
		DateFormat java8Format = PreJava9DateFormatProvider.getUSDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT);
		Date date = java8Format.parse(java8Date);
		Assert.assertEquals(GsonTool.withoutEnums().fromJson('"' + java8Date + '"', Date.class), date);

		String java9Date = "Feb 13, 2019, 10:40:25 PM";
		Assert.assertEquals(GsonTool.withoutEnums().fromJson('"' + java9Date + '"', Date.class), date);

		String java20Date = "Feb 13, 2019, 10:40:25\u202fPM";
		Assert.assertEquals(GsonTool.withoutEnums().fromJson('"' + java20Date + '"', Date.class), date);
		Assert.assertEquals(GsonTool.withoutEnums().toJson(date), '"' + java9Date + '"');
	}

	@Test
	public void testJava8DateTypeAdapter(){
		Gson java8TypeAdapterAtTheEnd = GsonTool.withoutEnums().newBuilder()
				.registerTypeAdapter(Date.class, new Java8DateTypeAdapter())
				.create();
		String java8Date = "Feb 13, 2019 10:40:25 PM";
		String java20Date = "Feb 13, 2019, 10:40:25\u202fPM";
		Date date = GsonTool.withoutEnums().fromJson('"' + java20Date + '"', Date.class);
		Assert.assertEquals(java8TypeAdapterAtTheEnd.toJson(date), '"' + java8Date + '"');
		Assert.assertEquals(java8TypeAdapterAtTheEnd.fromJson('"' + java8Date + '"', Date.class), date);
	}

}
