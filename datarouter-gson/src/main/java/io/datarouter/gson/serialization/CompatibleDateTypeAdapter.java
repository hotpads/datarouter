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
package io.datarouter.gson.serialization;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.TracerTool;

// only here for java 9 migration
// from com.google.gson.internal.bind.DateTypeAdapter
public class CompatibleDateTypeAdapter extends TypeAdapter<Date>{
	private static final Logger logger = LoggerFactory.getLogger(CompatibleDateTypeAdapter.class);

	private static final ThreadLocal<DateFormat> JAVA8_DATE_FORMAT = ThreadLocal.withInitial(
			() -> new SimpleDateFormat("MMM d, yyyy h:mm:ss a"));
	private static final ThreadLocal<DateFormat> JAVA9_DATE_FORMAT = ThreadLocal.withInitial(
			() -> new SimpleDateFormat("MMM d, yyyy, h:mm:ss a"));

	private boolean shouldLog = true;

	@Override
	public Date read(JsonReader in) throws IOException{
		if(in.peek() == JsonToken.NULL){
			in.nextNull();
			return null;
		}
		return deserializeToDate(in.nextString());
	}

	private Date deserializeToDate(String json){
		try{
			Date date = JAVA8_DATE_FORMAT.get().parse(json);
			inc("deserialize java8");
			return date;
		}catch(ParseException ignored){
			// ignore
		}
		try{
			Date date = JAVA9_DATE_FORMAT.get().parse(json);
			inc("deserialize java9");
			return date;
		}catch(ParseException ignored){
			// ignore
		}
		try{
			Date date = ISO8601Utils.parse(json, new ParsePosition(0));
			inc("deserialize ISO8601Utils");
			return date;
		}catch(ParseException e){
			throw new JsonSyntaxException(json, e);
		}

	}

	@Override
	public void write(JsonWriter out, Date value) throws IOException{
		if(value == null){
			out.nullValue();
			return;
		}
		String dateFormatAsString = JAVA8_DATE_FORMAT.get().format(value);
		inc("serialize java8");
		if(shouldLog){
			shouldLog = false;
			TracerTool.setForceSample();
			String traceId = TracerTool.getCurrentTraceparent()
					.map(Traceparent::toString)
					.orElse("");
			logger.warn(traceId, new Exception());
		}
		out.value(dateFormatAsString);
	}

	private static void inc(String key){
		Counters.inc("CompatibleDateTypeAdapter " + key);
	}

}
