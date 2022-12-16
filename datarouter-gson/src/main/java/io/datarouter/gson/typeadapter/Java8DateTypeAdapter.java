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
package io.datarouter.gson.typeadapter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

// TODO push down to where it's used
@Deprecated
public class Java8DateTypeAdapter
extends TypeAdapter<Date>{

	private static final ThreadLocal<DateFormat> JAVA8_DATE_FORMAT = ThreadLocal.withInitial(
			() -> new SimpleDateFormat("MMM d, yyyy h:mm:ss a"));

	@Override
	public void write(JsonWriter out, Date value) throws IOException{
		if(value == null){
			out.nullValue();
			return;
		}
		out.value(JAVA8_DATE_FORMAT.get().format(value));
	}

	@Override
	public Date read(JsonReader in) throws IOException{
		if(in.peek() == JsonToken.NULL){
			in.nextNull();
			return null;
		}
		try{
			return JAVA8_DATE_FORMAT.get().parse(in.nextString());
		}catch(ParseException | IOException e){
			throw new RuntimeException(e);
		}
	}

}
