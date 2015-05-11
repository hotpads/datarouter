package com.hotpads.datarouter.client.imp.jdbc.field;

import java.util.HashMap;
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
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class JdbcCodecFactory{

	private final Map<Class<? extends Field<?>>,Class<? extends JdbcFieldCodec<?,?>>> codecTypeByFieldType;
	
	
	//@Inject
	public JdbcCodecFactory(){
		this.codecTypeByFieldType = new HashMap<>();
		initMappings();
	}

	
	private void initMappings(){
		for(StandardJdbcFieldCodec codec : StandardJdbcFieldCodec.values()){
			codecTypeByFieldType.put(codec.getFieldType(), codec.getCodecType());
		}
	}
	
	public boolean hasCodec(Class<? extends Field<?>> fieldType){
		return codecTypeByFieldType.containsKey(fieldType);
	}
	
	public <T,C extends JdbcFieldCodec<T,Field<T>>> C createCodec(Field<T> field){
		Class<C> codecType = (Class<C>)codecTypeByFieldType.get(field.getClass());
		C codec = ReflectionTool.create(codecType);
		codec.setField(field);
		return codec;
	}
	
	
	
	/************************ test *****************************/
	
	@Guice(moduleFactory = DatarouterTestModuleFactory.class)
	public static class JdbcCodecFactoryTests{
		private static final Logger logger = LoggerFactory.getLogger(JdbcCodecFactory.JdbcCodecFactoryTests.class);
		
		@Inject
		private JdbcCodecFactory codecFactory;
		
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
				Class<? extends Field<?>> fieldType = standardFieldType.getFieldType();
				if(!codecFactory.hasCodec(fieldType)){
					logger.error("missing codec for {}", fieldType);
					hasAllCodecs = false;
				}
			}
			Assert.assertTrue(hasAllCodecs);
		}
	}
	
}
