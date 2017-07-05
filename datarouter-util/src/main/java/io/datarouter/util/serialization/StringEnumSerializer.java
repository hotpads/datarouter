package io.datarouter.util.serialization;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.datarouter.util.enums.StringEnum;

public class StringEnumSerializer<T extends Enum<T> & StringEnum<T>> implements JsonSerializer<T>, JsonDeserializer<T>{

	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
		@SuppressWarnings("unchecked")
		Class<T> classOfT = (Class<T>) typeOfT;
		T enumValue = classOfT.getEnumConstants()[0];
		return enumValue.fromPersistentString(json.getAsString());
	}

	@Override
	public JsonElement serialize(T stringEnum, Type typeOfSrc, JsonSerializationContext context){
		return new JsonPrimitive(stringEnum.getPersistentString());
	}

}
