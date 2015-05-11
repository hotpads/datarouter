package com.hotpads.datarouter.client.imp.hibernate.util;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodecFactory;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldSet;
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class HibernateResultParser{
	
	@Inject
	private JdbcFieldCodecFactory fieldCodecFactory;

	public <F extends FieldSet<?>>F fieldSetFromHibernateResultUsingReflection(Class<F> cls,
			List<Field<?>> fields, Object sqlObject){
		F targetFieldSet = ReflectionTool.create(cls);
		Object[] cols = (Object[])sqlObject;
		int counter = 0;
		for(JdbcFieldCodec<?,?> codec : fieldCodecFactory.createCodecs(fields)){
			codec.fromHibernateResultUsingReflection(targetFieldSet, cols[counter]);
			++counter;
		}
		return targetFieldSet;
	}
}
