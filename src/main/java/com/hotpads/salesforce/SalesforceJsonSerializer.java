package com.hotpads.salesforce;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.hotpads.salesforce.databean.SalesforceDatabean;
import com.hotpads.util.http.client.json.GsonJsonSerializer;

public class SalesforceJsonSerializer extends GsonJsonSerializer{

	static final int DATE_STRING_LENGTH = 10;
	
	public SalesforceJsonSerializer(Map<Class<? extends SalesforceDatabean>, List<String>> authorizedFields){
		super(new GsonBuilder()
		.registerTypeAdapter(Date.class, new SalesforceDateTypeAdapter())
		.addSerializationExclusionStrategy(new SalesforceExclusionStrategy(authorizedFields))
		.create());
	}
	
	private static class SalesforceExclusionStrategy implements ExclusionStrategy{

		private Map<Class<? extends SalesforceDatabean>, List<String>> authorizedFields;

		public SalesforceExclusionStrategy(Map<Class<? extends SalesforceDatabean>, List<String>> authorizedFields){
			this.authorizedFields = authorizedFields;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes f){
			if(f.getDeclaringClass().equals(SalesforceDatabean.class)){
				return true;
			}
			if(!authorizedFields.containsKey(f.getDeclaringClass())){
				return false;
			}
			return !authorizedFields.get(f.getDeclaringClass()).contains(f.getName());
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz){
			return false;
		}
		
	}

	private static class SalesforceDateTypeAdapter extends TypeAdapter<Date>{

		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		private SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		
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
				if(DATE_STRING_LENGTH == json.length()){
					return dateFormat.parse(json);					
				}else {
					return datetimeFormat.parse(json);					
				}
			}catch (Exception e){
				throw new JsonSyntaxException(json, e);
			}
		}

	}

}
