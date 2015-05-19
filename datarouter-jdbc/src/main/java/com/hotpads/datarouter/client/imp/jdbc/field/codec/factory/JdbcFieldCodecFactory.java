package com.hotpads.datarouter.client.imp.jdbc.field.codec.factory;

import java.util.Collection;
import java.util.List;

import com.google.inject.ImplementedBy;
import com.hotpads.datarouter.client.imp.jdbc.field.JdbcFieldCodec;
import com.hotpads.datarouter.storage.field.Field;

//@ImplementedBy(StandardJdbcFieldCodecFactory.class) //having trouble overriding this
public interface JdbcFieldCodecFactory{

	<T,F extends Field<T>> boolean hasCodec(Class<F> fieldType);

	<T,F extends Field<T>,C extends JdbcFieldCodec<T,F>> C createCodec(F field);

	List<JdbcFieldCodec<?,?>> createCodecs(Collection<Field<?>> fields);

}