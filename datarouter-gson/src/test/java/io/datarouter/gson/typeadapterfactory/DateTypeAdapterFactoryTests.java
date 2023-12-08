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
package io.datarouter.gson.typeadapterfactory;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.datarouter.gson.GsonTool;

public class DateTypeAdapterFactoryTests{

	private static final String
			JAVA__8_JSON = "\"Feb 13, 2019 10:40:25 PM\"",
			JAVA__9_JSON = "\"Feb 13, 2019, 10:40:25 PM\"",
			JAVA_20_JSON = "\"Feb 13, 2019, 10:40:25\u202fPM\"";

	private static final ZonedDateTime ZDT = ZonedDateTime.of(2019, 2, 13, 22, 40, 25, 0, ZoneId.systemDefault());

	private static final Date DATE = Date.from(ZDT.toInstant());

	@Test
	public void testSerialization(){
		Assert.assertEquals(GsonTool.GSON.toJson(DATE), JAVA__9_JSON);
	}

	@Test
	public void testDeserialization(){
		Assert.assertEquals(GsonTool.GSON.fromJson(JAVA__8_JSON, Date.class), DATE);
		Assert.assertEquals(GsonTool.GSON.fromJson(JAVA__9_JSON, Date.class), DATE);
		Assert.assertEquals(GsonTool.GSON.fromJson(JAVA_20_JSON, Date.class), DATE);
	}

	@Test
	public void testSetDateFormat(){
		String customDateFormat = "EEE, d MMM yyyy HH:mm:ss z";
		String expectedFormatted = ZDT.format(DateTimeFormatter.ofPattern(customDateFormat));

		Gson gson = GsonTool.GSON.newBuilder()
				.setDateFormat(customDateFormat)
				.create();

		Assert.assertEquals(gson.toJson(DATE), "\"" + expectedFormatted + "\"");
		Assert.assertEquals(gson.fromJson('"' + expectedFormatted + '"', Date.class), DATE);
		Assert.assertEquals(gson.fromJson(JAVA__8_JSON, Date.class), DATE);
		Assert.assertEquals(gson.fromJson(JAVA__9_JSON, Date.class), DATE);
		Assert.assertEquals(gson.fromJson(JAVA_20_JSON, Date.class), DATE);
	}

	@Test
	public void testCustomDateAdapter(){
		var expected = new Date();

		Gson gson = GsonTool.GSON.newBuilder()
				.registerTypeAdapter(Date.class, new TypeAdapter<Date>(){
					@Override
					public Date read(JsonReader in) throws IOException{
						in.nextString();
						return expected;
					}

					@Override
					public void write(JsonWriter out, Date value) throws IOException{
						out.value("custom-value");
					}
				})
				.create();

		Assert.assertEquals(gson.fromJson("\"anything\"", Date.class), expected);
		Assert.assertEquals(gson.toJson(expected), "\"custom-value\"");
	}

}
