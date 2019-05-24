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
package io.datarouter.util.serialization;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.PreJava9DateFormatProvider;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

// only here for java 9 migration
// from com.google.gson.internal.bind.DateTypeAdapter
public class CompatibleDateTypeAdapter extends TypeAdapter<Date>{

	/**
	 * List of 1 or more different date formats used for de-serialization attempts. The first of them (default US
	 * format) is used for serialization as well.
	 */
	private final List<DateFormat> dateFormats = new ArrayList<>();

	public CompatibleDateTypeAdapter(){
		dateFormats.add(new SimpleDateFormat("MMM d, yyyy h:mm:ss a")); // java 8
		dateFormats.add(new SimpleDateFormat("MMM d, yyyy, h:mm:ss a")); // java 9
	}

	@Override
	public Date read(JsonReader in) throws IOException{
		if(in.peek() == JsonToken.NULL){
			in.nextNull();
			return null;
		}
		return deserializeToDate(in.nextString());
	}

	private synchronized Date deserializeToDate(String json){
		for(DateFormat dateFormat : dateFormats){
			try{
				return dateFormat.parse(json);
			}catch(ParseException ignored){
				// ignore
			}
		}
		try{
			return ISO8601Utils.parse(json, new ParsePosition(0));
		}catch(ParseException e){
			throw new JsonSyntaxException(json, e);
		}
	}

	@Override
	public synchronized void write(JsonWriter out, Date value) throws IOException{
		if(value == null){
			out.nullValue();
			return;
		}
		String dateFormatAsString = dateFormats.get(0).format(value);
		out.value(dateFormatAsString);
	}

	public static class CompatibleDateTypeAdapterTests{

		@Test
		public void testJava9DateSerialization() throws ParseException{
			String java8Date = "Feb 13, 2019 10:40:25 PM";
			String java9Date = "Feb 13, 2019, 10:40:25 PM";
			DateFormat java8Format = PreJava9DateFormatProvider.getUSDateTimeFormat(DateFormat.DEFAULT,
					DateFormat.DEFAULT);
			Date date = java8Format.parse(java8Date);
			Assert.assertEquals(GsonTool.GSON.fromJson('"' + java8Date + '"', Date.class), date);
			Assert.assertEquals(GsonTool.GSON.fromJson('"' + java9Date + '"', Date.class), date);
		}

	}

}
