package com.hotpads.datarouter.client.imp.jdbc.field.codec.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.StandardJdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.StringJdbcFieldCodec;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.StandardFieldType;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

@Singleton
public class StandardJdbcFieldCodecFactory implements JdbcFieldCodecFactory{

	private final Map<Class<? extends Field<?>>,Class<? extends JdbcFieldCodec<?,?>>> codecTypeByFieldType;

	public StandardJdbcFieldCodecFactory(){
		this.codecTypeByFieldType = new HashMap<>();
		initMappings();
	}

	private void initMappings(){
		for(StandardJdbcFieldCodec codec : StandardJdbcFieldCodec.values()){
			addCodec(codec.getFieldType(), codec.getCodecType());
		}
	}

	public void addCodec(Class<? extends Field<?>> fieldType, Class<? extends JdbcFieldCodec<?,?>> codecType){
		codecTypeByFieldType.put(fieldType, codecType);
	}


	@Override
	public <T,F extends Field<T>> boolean hasCodec(Class<F> fieldType){
		return codecTypeByFieldType.containsKey(fieldType);
	}


	@Override
	public <T,F extends Field<T>,C extends JdbcFieldCodec<T,F>> C createCodec(F field){
		Class<F> fieldType = (Class<F>)field.getClass();
		Class<C> codecType = (Class<C>)codecTypeByFieldType.get(fieldType);
		if(codecType == null){
			throw new RuntimeException("no codec found for " + field.getClass());
		}
		try{
			for(Constructor<?> constructor : codecType.getDeclaredConstructors()){
				if(constructor.getParameterCount() == 1
						&& constructor.getParameterTypes()[0].isAssignableFrom(fieldType)){
					Object codec = constructor.newInstance(field);
					return codecType.cast(codec);
				}
			}
		}catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e){
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Can't create " + codecType);
	}


	@Override
	public List<JdbcFieldCodec<?,?>> createCodecs(Collection<Field<?>> fields){
		List<JdbcFieldCodec<?,?>> codecs = DrListTool.createArrayListWithSize(fields);
		for(Field<?> field : DrIterableTool.nullSafe(fields)){
			codecs.add(createCodec(field));
		}
		return codecs;
	}



	/************************ test *****************************/

	@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
	public static class JdbcCodecFactoryTests{
		private static final Logger logger = LoggerFactory.getLogger(JdbcCodecFactoryTests.class);

		@Inject
		private JdbcFieldCodecFactory codecFactory;

		@Test
		public void testStringCodec(){
			StringField field = new StringField("fName", "myValue", 23);
			StringJdbcFieldCodec codec = codecFactory.createCodec(field);
			Assert.assertSame(field, codec.getField());
		}
		@Test
		public void testRegistrations(){
			boolean hasAllCodecs = true;
			for(StandardFieldType standardFieldType : StandardFieldType.values()){
				Class<? extends Field> fieldType = standardFieldType.getFieldType();
				if(!codecFactory.hasCodec(fieldType)){
					logger.error("missing codec for {}", fieldType);
					hasAllCodecs = false;
				}
			}
			Assert.assertTrue(hasAllCodecs);
		}
	}

}
