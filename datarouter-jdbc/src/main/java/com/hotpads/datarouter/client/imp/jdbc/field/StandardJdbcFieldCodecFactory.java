package com.hotpads.datarouter.client.imp.jdbc.field;

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

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.StandardFieldType;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class StandardJdbcFieldCodecFactory implements JdbcFieldCodecFactory{
	private static final Logger logger = LoggerFactory.getLogger(StandardJdbcFieldCodecFactory.class);
	

	private final Map<Class<? extends Field<?>>,Class<? extends JdbcFieldCodec>> codecTypeByFieldType;
	
	
	//@Inject
	public StandardJdbcFieldCodecFactory(){
		this.codecTypeByFieldType = new HashMap<>();
		initMappings();
	}

	
	private void initMappings(){
		for(StandardJdbcFieldCodec codec : StandardJdbcFieldCodec.values()){
			addCodec(codec.getFieldType(), codec.getCodecType());
		}
	}
	
	public void addCodec(Class<? extends Field<?>> fieldType, Class<? extends JdbcFieldCodec> codecType){
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
		C codec = ReflectionTool.create(codecType);
		codec.setField(field);
//		logger.warn("created {} for {}={}", codecType.getSimpleName(), fieldType.getSimpleName(), field.getValue());
		return codec;
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
	
	@Guice(moduleFactory = DatarouterTestModuleFactory.class)
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
