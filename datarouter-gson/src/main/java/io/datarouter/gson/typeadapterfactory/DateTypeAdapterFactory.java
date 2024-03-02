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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.DefaultDateTypeAdapter;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.datarouter.instrumentation.metric.Metrics;

public class DateTypeAdapterFactory implements TypeAdapterFactory{

	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken){
		if(typeToken.getRawType() != Date.class){
			return null;
		}

		TypeAdapter<Date> delegateAdapter = (TypeAdapter<Date>)gson.getDelegateAdapter(this, typeToken);
		return (TypeAdapter<T>)new DateTypeAdapter(delegateAdapter);
	}

	private static class DateTypeAdapter extends TypeAdapter<Date>{

		private static final ZoneId DATE_DEFAULT_ZONE_ID = ZoneId.systemDefault();

		private static final Formatter JAVA9_DATE_FORMATTER = new Formatter("java9", "MMM d, yyyy, h:mm:ss a");
		private static final List<Formatter> ALL_FORMATTERS = List.of(
				new Formatter("java8", "MMM d, yyyy h:mm:ss a"),
				JAVA9_DATE_FORMATTER,
				new Formatter("java20", "MMM d, yyyy, h:mm:ss\u202fa"));

		private final TypeAdapter<Date> delegateAdapter;

		public DateTypeAdapter(TypeAdapter<Date> delegateAdapter){
			this.delegateAdapter = delegateAdapter;
		}

		@Override
		public Date read(JsonReader in) throws IOException{
			if(in.peek() == JsonToken.NULL){
				in.nextNull();
				return null;
			}

			return deserializeToDate(in.nextString());
		}

		private synchronized Date deserializeToDate(String json) throws IOException{
			// Try our formatters
			for(Formatter formatter : ALL_FORMATTERS){
				try{
					Date date = Date.from(LocalDateTime.parse(json, formatter.formatter())
							.atZone(DATE_DEFAULT_ZONE_ID)
							.toInstant());

					inc("deserialize " + formatter.name());
					return date;
				}catch(DateTimeParseException ignored){
					// ignore
				}
			}

			// Fallback to delegate adapter
			Date date = delegateAdapter.read(new JsonTreeReader(new JsonPrimitive(json)));
			inc("deserialize delegate");
			return date;
		}

		@Override
		public void write(JsonWriter out, Date value) throws IOException{
			if(value == null){
				out.nullValue();
				return;
			}

			if(delegateAdapter instanceof DefaultDateTypeAdapter){
				// this means a custom date pattern was specified
				delegateAdapter.write(out, value);
				inc("serialize delegate");
				return;
			}

			//format to java9 for backwards and deployment compatibility
			String dateFormatAsString = value.toInstant()
					.atZone(DATE_DEFAULT_ZONE_ID)
					.toLocalDateTime()
					.format(JAVA9_DATE_FORMATTER.formatter());
			inc("serialize " + JAVA9_DATE_FORMATTER.name());
			out.value(dateFormatAsString);
		}

		private static void inc(String key){
			Metrics.count("CompatibleDateTypeAdapter " + key);
		}

		private record Formatter(
				String name,
				DateTimeFormatter formatter){

			private Formatter(String name, String pattern){
				this(name, DateTimeFormatter.ofPattern(pattern));
			}

		}

	}

}
