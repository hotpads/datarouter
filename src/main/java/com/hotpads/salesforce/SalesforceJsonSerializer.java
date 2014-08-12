package com.hotpads.salesforce;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.hotpads.util.http.client.json.GsonJsonSerializer;

public class SalesforceJsonSerializer extends GsonJsonSerializer{

	public SalesforceJsonSerializer(){
		super(new GsonBuilder().registerTypeAdapter(Date.class, new SalesforceDateTypeAdapter()).create());
	}

	private static class SalesforceDateTypeAdapter extends TypeAdapter<Date>{

		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");

		@Override
		public void write(JsonWriter writer, Date date) throws IOException{
			if (date == null){
				writer.nullValue();
				return;
			}
			writer.value(dateFormat.format(date));
		}

		@Override
		public Date read(JsonReader reader) throws IOException{
			if (reader.peek() == JsonToken.NULL){
				reader.nextNull();
				return null;
			}
			String json = reader.nextString();
			try{
				return dateFormat.parse(json);
			}catch (Exception e){
				throw new JsonSyntaxException(json, e);
			}
		}

	}

}
