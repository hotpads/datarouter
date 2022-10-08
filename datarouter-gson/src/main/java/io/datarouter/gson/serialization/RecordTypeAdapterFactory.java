package io.datarouter.gson.serialization;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson support for Java 16+ record types. Taken from https://github.com/google/gson/issues/1794
 *
 * https://gist.github.com/knightzmc/cf26d9931d32c78c5d777cc719658639
 */
public class RecordTypeAdapterFactory implements TypeAdapterFactory{

	private static final Map<Class<?>,Object> PRIMITIVE_DEFAULTS = Map.of(
			byte.class, (byte)0,
			int.class, 0,
			long.class, 0L,
			short.class, (short)0,
			double.class, 0D,
			float.class, 0F,
			char.class, '\0',
			boolean.class, false);

	/**
	 * Get all names of a record component If annotated with {@link SerializedName} the list returned will be the
	 * primary name first, then any alternative names Otherwise, the component name will be returned.
	 */
	private List<String> getRecordComponentNames(RecordComponent recordComponent){
		List<String> names = new ArrayList<>();
		// The @SerializedName is compiled to be part of the componentName() method
		// The use of a loop is also deliberate, getAnnotation seemed to return null if Gson's package was relocated
		SerializedName serializedName = null;
		for(Annotation annotation : recordComponent.getAccessor().getAnnotations()){
			if(annotation.annotationType() == SerializedName.class){
				serializedName = (SerializedName)annotation;
				break;
			}
		}

		if(serializedName != null){
			names.add(serializedName.value());
			names.addAll(Arrays.asList(serializedName.alternate()));
		}else{
			names.add(recordComponent.getName());
		}
		var namesList = List.copyOf(names);
		return namesList;
	}

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type){
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>)type.getRawType();
		if(!clazz.isRecord()){
			return null;
		}
		TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

		return new TypeAdapter<>(){

			@Override
			public void write(JsonWriter out, T value) throws IOException{
				delegate.write(out, value);
			}

			@Override
			public T read(JsonReader reader) throws IOException{
				if(reader.peek() == JsonToken.NULL){
					reader.nextNull();
					return null;
				}
				RecordComponent[] recordComponents = clazz.getRecordComponents();
				Map<String,TypeToken<?>> typeMap = new HashMap<>();
				for(RecordComponent recordComponent : recordComponents){
					for(String name : getRecordComponentNames(recordComponent)){
						typeMap.put(name, TypeToken.get(recordComponent.getGenericType()));
					}
				}
				Map<String,Object> argsMap = new HashMap<>();
				reader.beginObject();
				while(reader.hasNext()){
					String name = reader.nextName();
					TypeToken<?> type = typeMap.get(name);
					if(type != null){
						argsMap.put(name, gson.getAdapter(type).read(reader));
					}else{
						gson.getAdapter(Object.class).read(reader);
					}
				}
				reader.endObject();

				Class<?>[] argTypes = new Class<?>[recordComponents.length];
				Object[] args = new Object[recordComponents.length];
				for(int i = 0; i < recordComponents.length; i++){
					argTypes[i] = recordComponents[i].getType();
					List<String> names = getRecordComponentNames(recordComponents[i]);
					Object value = null;
					TypeToken<?> type = null;
					// Find the first matching type and value
					for(String name : names){
						value = argsMap.get(name);
						type = typeMap.get(name);
						if(value != null && type != null){
							break;
						}
					}
					if(value == null
							&& type != null
							&& type.getRawType().isPrimitive()){
						value = PRIMITIVE_DEFAULTS.get(type.getRawType());
					}
					args[i] = value;
				}
				Constructor<T> constructor;
				try{
					constructor = clazz.getDeclaredConstructor(argTypes);
					constructor.setAccessible(true);
					return constructor.newInstance(args);
				}catch(NoSuchMethodException
						| InstantiationException
						| SecurityException
						| IllegalAccessException
						| IllegalArgumentException
						| InvocationTargetException e){
					throw new RuntimeException(e);
				}
			}
		};
	}

}
