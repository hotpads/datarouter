package com.hotpads.datarouter.client.imp.jdbc.field;

import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.java.ReflectionTool;

public enum StandardJdbcFieldCodec{

	STRING(StringJdbcFieldCodec.class),
	;
	
	
	private final Class<? extends Field<?>> fieldType;
	private final Class<? extends JdbcFieldCodec<?,?>> codecType;
	
	private StandardJdbcFieldCodec(Class<? extends JdbcFieldCodec<?,?>> codecType){
		this.codecType = codecType;
		this.fieldType = createCodec().getFieldType();
	}
	
	
	public Class<? extends Field<?>> getFieldType(){
		return fieldType;
	}
	
	public Class<? extends JdbcFieldCodec<?,?>> getCodecType(){
		return codecType;
	}
	
//	public <T> getFieldAndCodecTypes(){
//		
//	}
	
//	public <T,C extends JdbcFieldCodec<T>> C createCodec(){
//		return (C)ReflectionTool.create(codecType);
//	}
	
	public JdbcFieldCodec<?,?> createCodec(){
		return ReflectionTool.create(codecType);
	}
	
}
